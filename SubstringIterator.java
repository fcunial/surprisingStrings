import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Iterates in parallel over all the distinct substrings of positive length of a given
 * string $s$ using an FM-index, i.e. following a depth-first search over the trie of
 * the reverse of $s$. The depth-first search logic is programmable, i.e. subclasses of
 * $Substring$ define which left-extensions of the current string should be explored.
 * In a shared-memory machine the search is automatically parallelized, and the user has
 * no control over work distribution -- this resembles general-purpose parallel frameworks
 * like e.g. \cite{DIB - A distributed implementation of backtracking}.
 * The original string $s$ can be safely deallocated after this class has been constructed.
 *
 * Remark: The FM-index is implemented as a sequence of Huffman-shaped wavelet trees,
 * one per block of the BWT of $s$. All BWT blocks have approximately the same size
 * (determined by the memory available during construction), so the total size of
 * all wavelet trees is approximately $|s|*H_k(s)+blockSize*alphabetLength^k$ bits for any
 * given $k$, where $H_k(s)$ is the $k$-th order entropy of $s$
 * \cite{Fixed_block_compression_boosting_in_FM-indexes}. This can be significantly
 * smaller than $|s|*\log_2(alphabetLength)$ if $s$ is compressible.
 *
 * Remark: In practice this fixed-block approach should be approximately as fast as a
 * single Huffman-shaped wavelet tree if $s$ is incompressible, and possibly faster if $s$
 * is compressible \cite{Fixed_block_compression_boosting_in_FM-indexes}.
 */
public class SubstringIterator {
	/**
	 * Constants
	 */
	private final int alphabetLength, log2alphabetLength;
	private int nBlocks;
	private Substring substringClass;  // Subclass of $Substring$ to be used during navigation

	/**
	 * FM-index
	 */
	private HuffmanWaveletTree[] waveletTrees;
	private IntArray[] blockCounts;  // Number of occurrences of each character before the beginning of each block
	private IntArray blockStarts;  // Starting position of each block
	private Rank9 blockBoundaries;  // Dense representation of a sparse bitvector, for speed.
	private long[] C;  // The $C$ array in backward-search

	/**
	 * $dollar[0]$: position of the dollar character in the $BWT$;
	 * $dollar[1]$: BWT block containing the dollar character;
	 * $dollar[2]$: position of the dollar character inside the BWT block $dollar[1]$.
	 */
	private long[] dollar;


	/**
	 * @param maxMemory maximum number of bytes to be used during construction (in
	 * addition to those used by $string$);
	 * @param nThreads maximum number of threads to be used during construction;
	 * @param substringClass subclass of $Substring$ to be used during navigation.
	 */
	public SubstringIterator(IntArray string, int[] alphabet, int alphabetLength, long maxMemory, int nThreads, Substring substringClass) {
		final int stringLength = string.length();
		final int log2stringLength = Utils.log2(stringLength);
		this.alphabetLength=alphabetLength;
		log2alphabetLength=Utils.log2(alphabetLength);
		final int blockSize = Suffixes.blockwiseBWT_getBlockSize(maxMemory<<3,nThreads,stringLength,log2stringLength,log2alphabetLength);
		nBlocks=Utils.divideAndRoundUp(stringLength,blockSize);  // This value is just an upper bound: $Suffixes.blockwiseBWT$ will set the effective number of blocks.
		if (nBlocks<4) nBlocks=4;
		waveletTrees = new HuffmanWaveletTree[nBlocks];
		blockStarts = new IntArray(nBlocks,log2stringLength,true);
		dollar = new long[3];
		IntArray[] localBlockCounts = new IntArray[nBlocks];
		IntArray bitVector = new IntArray(stringLength,1,true);
		Suffixes.blockwiseBWT(string,alphabet,alphabetLength,log2alphabetLength,blockSize,nThreads,null,waveletTrees,blockStarts,bitVector,localBlockCounts,dollar);
		blockBoundaries = new Rank9(bitVector);
		bitVector.deallocate(); bitVector=null;
		nBlocks=(int)blockBoundaries.count()+1;  // Setting the effective number of blocks
		this.substringClass=substringClass;

		// Building $blockCounts$ and $C$ in small space
		int i, j;
		long max;
		long[] characterCounts = new long[alphabetLength];
		blockCounts = new IntArray[nBlocks];
		blockCounts[0] = new IntArray(alphabetLength,log2alphabetLength,true);
		for (i=1; i<nBlocks; i++) {
			max=0;
			for (j=0; j<alphabetLength; j++) {
				characterCounts[j]+=localBlockCounts[i-1].getElementAt(j);
				if (characterCounts[j]>max) max=characterCounts[j];
			}
			blockCounts[i] = new IntArray(alphabetLength,Utils.log2(max));
			for (j=0; j<alphabetLength; j++) blockCounts[i].setElementAt(j,(int)characterCounts[j]);
		}
		for (j=0; j<alphabetLength; j++) characterCounts[j]+=localBlockCounts[nBlocks-1].getElementAt(j);
		characterCounts[string.getElementAt(stringLength-1)]++;  // The last character of $string$ does not appear in its BWT
		C = new long[alphabetLength];
		for (j=1; j<alphabetLength; j++) C[j]=C[j-1]+characterCounts[j-1];




System.err.println();
for (i=0; i<nBlocks; i++) System.err.print("blockStarts["+i+"]="+blockStarts.getElementAt(i)+" ");
System.err.println();

	}


	/**
	 * Extends to the left the first substring $w$ from the top of $stack$ that has not
	 * been extended yet, popping out of $stack$ all the substrings met before $w$ that
	 * have already been extended. Extensions $aw$, $a \in \Sigma$, such that their method
	 * $occurs$ returns true, are notified by calling their method $visited$, and they are
	 * pushed onto $stack$ if their method $shouldBeExtendedLeft$ returns true.
	 *
	 * @param stack the stream pointer is assumed to be at the first bit of the serialized
	 * substring at the top of $stack$;
	 * @param w non-null temporary, reused container representing the string at the top of
	 * $stack$;
	 * @param leftExtensions $alphabetLength$ non-null temporary, reused containers
	 * representing $aw$ for all $a \in \Sigma$;
	 * @param positions $w.nIntervals*2$ non-null temporary, reused containers of the
	 * interval positions of $w$;
	 * @param multirankStack temporary, reused space with $1+w.nIntervals*2$ columns and
	 * $alphabetLength-1$ rows used by $HuffmanWaveletTree.multirank$;
	 * @param multirankOutput temporary, reused space with $alphabetLength$ rows and
	 * $w.nIntervals*2$ columns used by $HuffmanWaveletTree.multirank$;
	 * @param multirankOnes temporary, reused space with $w.nIntervals*2$ cells used by
	 * $HuffmanWaveletTree.multirank$;
	 * @return the variation $-1 \leq \delta < alphabetLength$ in the number of
	 * \emph{non-extended} strings $v$ in $stack$, with $|v| \leq maxStringLengthToReport$,
	 * induced by this call to $extendLeft$.
	 */
	private final int extendLeft(Stream stack, Substring w, Substring[] leftExtensions, Position[] positions, long[][] multirankStack, long[][] multirankOutput, long[] multirankOnes, int maxStringLengthToReport) {
		final boolean reportExtension;
		int i, j, c, p, windowFirst, windowSize, tree, previousTree, nPositions, nExtensions, out;
		long pos, previous;
		Substring extension;

		// Reading the top of $stack$
		w.readFast(stack);
		while (w.hasBeenExtended || (w.bwtIntervals[0][0]==dollar[0]&&w.bwtIntervals[0][1]==dollar[0])) {
			previous=w.stackPointers[1];
			w.pop(stack);
			stack.setPosition(previous);
			if (previous==0) return 0;
			w.readFast(stack);
		}
		out=0;
System.err.println("extending ("+w.bwtIntervals[0][0]+","+w.bwtIntervals[0][1]+"), dollar="+dollar[0]+"...");

		// Putting the positions of $w.bwtIntervals$ in tree order, and sequentially inside
		// each tree.
		for (i=0; i<w.nIntervals; i++) {
			p=i<<1; pos=w.bwtIntervals[i][0];
			positions[p].position=pos; positions[p].row=i; positions[p].column=0;
			positions[p].tree=(int)blockBoundaries.rank(pos+1);
			p=(i<<1)+1; pos=w.bwtIntervals[i][1]+1;
			positions[p].position=pos; positions[p].row=i; positions[p].column=1;
			positions[p].tree=(int)blockBoundaries.rank(pos+1);
		}
		Arrays.sort(positions);
		nPositions=w.nIntervals<<1;

		// Ranking all positions in the same tree using exactly one $multirank$ call
		windowFirst=0; windowSize=1;
		previousTree=positions[windowFirst].tree;
		multirankStack[0][1]=positions[windowFirst].position-blockStarts.getElementAt(previousTree);
		for (p=1; p<nPositions; p++) {
			tree=positions[p].tree;
			if (tree==previousTree) {
				windowSize++;
				multirankStack[0][windowSize]=positions[p].position-blockStarts.getElementAt(tree);
			}
			else {
				// Correcting for dollar, if necessary.
				if (previousTree==dollar[1]) {
					for (i=windowSize; i>=1 && multirankStack[0][i]>dollar[2]; i--) multirankStack[0][i]--;
				}
				waveletTrees[previousTree].multirank(windowSize,multirankStack,multirankOutput,multirankOnes);
				for (c=0; c<alphabetLength; c++) {
					for (i=0; i<windowSize; i++) leftExtensions[c].bwtIntervals[positions[windowFirst+i].row][positions[windowFirst+i].column]=C[c]+(blockCounts[previousTree].getElementAt(c)+multirankOutput[c][i])+(positions[windowFirst+i].column==0?0:-1);
				}
				windowFirst=p; windowSize=1; previousTree=tree;
				multirankStack[0][1]=positions[p].position-blockStarts.getElementAt(tree);
			}
		}
		// Last tree
		if (previousTree==dollar[1]) {
			for (i=windowSize; i>=1 && multirankStack[0][i]>dollar[2]; i--) multirankStack[0][i]--;
		}
		waveletTrees[previousTree].multirank(windowSize,multirankStack,multirankOutput,multirankOnes);
		for (c=0; c<alphabetLength; c++) {
			for (i=0; i<windowSize; i++) leftExtensions[c].bwtIntervals[positions[windowFirst+i].row][positions[windowFirst+i].column]=C[c]+(blockCounts[previousTree].getElementAt(c)+multirankOutput[c][i])+(positions[windowFirst+i].column==0?0:-1);
		}

		// Signalling to the left-extensions of $w$, and pushing them onto $stack$.
		reportExtension=w.length+1<=maxStringLengthToReport;
		nExtensions=0; extension=null;
		previous=w.stackPointers[0];
		for (c=0; c<alphabetLength; c++) {
			extension=leftExtensions[c];
			if (extension.occurs()) {
				extension.init(w,c);
				extension.visited(stack);
				if (extension.shouldBeExtendedLeft()) {
					extension.stackPointers[1]=previous;
					extension.push(stack);
					nExtensions++;
					previous=extension.stackPointers[0];
					if (reportExtension) out++;
				}
			}
		}
		w.markAsExtended(stack);
		if (w.length<=maxStringLengthToReport) out--;
		stack.setPosition(nExtensions>0?previous:w.stackPointers[0]);
		return out;
	}


	private static class Position implements Comparable {
		protected long position;
		protected int tree, row, column;

		public int compareTo(Object other) {
			Position otherPosition = (Position)other;
			if (tree<otherPosition.tree) return -1;
			if (tree>otherPosition.tree) return 1;
			if (position<otherPosition.position) return -1;
			if (position>otherPosition.position) return 1;
			return 0;
		}
	}


	/**
	 * @param nThreads maximum number of threads to be used during traversal.
	 */
	public void run(int nThreads) {
		int i;
		long previous;
		SubstringIteratorThread[] threads = new SubstringIteratorThread[nThreads];
		AtomicInteger donorGenerator = new AtomicInteger();
		CountDownLatch latch = new CountDownLatch(nThreads);
		for (i=0; i<nThreads; i++) threads[i] = new SubstringIteratorThread(threads,i,substringClass,donorGenerator,latch);

		// Initializing the stack of $threads[0]$
		Substring epsilon = substringClass.getInstance();
		epsilon.hasBeenExtended=true;
		epsilon.push(threads[0].stack);
		epsilon.deallocate(); epsilon=null;
		Substring[] lengthOneSubstrings = new Substring[alphabetLength];
		substringClass.getLengthOneSubstrings(C,lengthOneSubstrings);
		previous=0L;
		for (i=0; i<alphabetLength; i++) {

System.err.println("lengthOneSubstring["+i+"]=("+lengthOneSubstrings[i].bwtIntervals[0][0]+","+lengthOneSubstrings[i].bwtIntervals[0][1]+")");


			lengthOneSubstrings[i].stackPointers[1]=previous;
			lengthOneSubstrings[i].stackPointers[2]=0L;
			lengthOneSubstrings[i].push(threads[0].stack);
			previous=lengthOneSubstrings[i].stackPointers[0];
		}
		threads[0].stack.setPosition(lengthOneSubstrings[alphabetLength-1].stackPointers[0]);
		threads[0].nStringsNotExtended=alphabetLength;
		for (i=0; i<alphabetLength; i++) {
			lengthOneSubstrings[i].deallocate();
			lengthOneSubstrings[i]=null;
		}
		lengthOneSubstrings=null;

		// Launching all threads
		for (i=0; i<nThreads; i++) threads[i].start();
		try { latch.await(); }
		catch(InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}


	/**
	 * Explores a partition of the trie of the reverse of $s$ in depth-first order, by
	 * repeatedly invoking $extendLeft$. For load-balancing, uses the work-stealing
	 * strategy described in \cite{Parallel depth first search part I - implementation}.
	 *
	 * Remark: A thread with empty stack scans the list of threads for a donor
	 * sequentially, using an atomic donor pointer shared by all threads as described in
	 * \cite{Parallel depth first search part II - analysis}.
	 *
	 * Remark: The current work-stealing strategy requires splitting a prefix of the
	 * stack of the donor. This allows to trade load balancing (which, in the general case
	 * of an irregular trie, improves with the length of the prefix) with the speed of the
	 * work-balancing code (which degrades with the length of the prefix).
	 *
	 * Remark: A simple lower bound on the size of the donor stack up to the prefix
	 * threshold tries to limit cases in which splitting the stack is slower than having
	 * it processed by the donor itself. We use this simple strategy because this engine
	 * is general-purpose, so we can't estimate exploration time (we might add a
	 * corresponding method to $Substring$ and force subclasses to extend it: we leave
	 * this to future extensions).
	 *
	 * Remark: Another possible improvement described in \cite{Parallel depth first search
	 * part I - implementation} consists in avoiding the synchronization with the donor
	 * thread if the donor is working very deeply with respect to the split prefix. Once
	 * again, we can't implement this approach in a general-purpose engine, because we
	 * can't estimate expansion time.
	 *
	 * Remark: We choose not to precompute a large, static set of fine-grained, fixed-size
	 * workpackets, as described in \cite{Work-load balancing in highly parallel
	 * depth-first search}, because we want to use as little space as possible --
	 * voluntarily paying the smaller space with the workload imbalance and communication
	 * overheads that come from dynamic workpackets of variable size.
	 */
	private class SubstringIteratorThread extends Thread {
		/**
		 * Number of longs allocated to each region of the stack (a power of two).
		 * Balances between space and time. Must be tuned empirically.
		 */
		protected static final int LONGS_PER_REGION = 8;

		/**
		 * Number of work-stealing attempts performed by each thread before terminating.
		 * Must be tuned empirically.
		 */
		protected static final int N_STEALING_ATTEMPTS = 8;

		/**
		 * Only strings of length at most $MAX_STRING_LENGTH_FOR_SPLIT$ are stolen from
		 * the donor. Must be tuned empirically.
		 */
		protected static final int MAX_STRING_LENGTH_FOR_SPLIT = 32;

		/**
		 * The stack of the donor thread is split iff it contains at least
		 * $DONOR_STACK_LOWERBOUND$ strings of length at most $MAX_STRING_LENGTH_FOR_SPLIT$
		 * that have not yet been extended. Must be tuned empirically.
		 */
		protected static final int DONOR_STACK_LOWERBOUND = 2;

		/**
		 * Work-stealing-related variables
		 */
		private boolean isAlive;  // Flags a dead thread
		protected int nStringsNotExtended;  // Number of strings in $substringStack$ that have yet to be extended, and that have length $<=MAX_STRING_LENGTH_FOR_SPLIT$.
		private SubstringIteratorThread[] threads;  // Pointers to all threads
		private final int nThreads;  // Number of threads in $threads$
		private final int threadID;  // Position of this thread in $threads$
		private AtomicInteger donorGenerator;  // Global generator of donor pointers
		private CountDownLatch latch;  // Global barrier

		/**
		 * $extendLeft$-related variables
		 */
		protected Stream stack;
		private Substring substringClass;  // Subclass of $Substring$ to be used throughout
		private Substring w;  // The substring at the top of $stack$
		private Substring[] leftExtensions;
		private Position[] positions;
		private long[][] multirankStack, multirankOutput;
		private long[] multirankOnes;


		/**
		 * @param nThreads assumed to be a power of two.
		 */
		public SubstringIteratorThread(SubstringIteratorThread[] threads, int threadID, Substring substringClass, AtomicInteger donorGenerator, CountDownLatch latch) {
			int i;
			// $extendLeft$-related variables
			stack = new Stream(LONGS_PER_REGION);
			this.substringClass=substringClass;
			w=substringClass.getInstance();
			leftExtensions = new Substring[alphabetLength];
			for (i=0; i<alphabetLength; i++) leftExtensions[i]=substringClass.getInstance();
			final int maxPositions = substringClass.nIntervals<<1;
			positions = new Position[maxPositions];
			for (i=0; i<maxPositions; i++) positions[i] = new Position();
			multirankStack = new long[alphabetLength-1][1+maxPositions];
			multirankOutput = new long[alphabetLength][maxPositions];
			multirankOnes = new long[maxPositions];
			// Work-stealing-related variables
			this.threads=threads;
			nThreads=threads.length;
			this.threadID=threadID;
			this.donorGenerator=donorGenerator;
			this.latch=latch;
		}


		private final void deallocate() {
			int i, nPositions;
			threads=null;
			donorGenerator=null;
			latch=null;
			stack.deallocate(); stack=null;
			substringClass.deallocate(); substringClass=null;
			w.deallocate(); w=null;
			for (i=0; i<alphabetLength; i++) {
				leftExtensions[i].deallocate();
				leftExtensions[i]=null;
			}
			leftExtensions=null;
			nPositions=positions.length;
			for (i=0; i<nPositions; i++) positions[i]=null;
			positions=null;
			multirankStack=null;
			multirankOutput=null;
			multirankOnes=null;
		}


		public void run() {
			long[][] addressTranslator = new long[3][MAX_STRING_LENGTH_FOR_SPLIT+1];
			XorShiftStarRandom random = new XorShiftStarRandom();

			isAlive=true;
			if (nThreads>1 && stack.getPosition()==0) stealWork(addressTranslator,random);
			while (stack.getPosition()>0) {
				// Exhausting the current stack
				while (true) {
					synchronized(this) {
						if (stack.getPosition()>0) nStringsNotExtended+=extendLeft(stack,w,leftExtensions,positions,multirankStack,multirankOutput,multirankOnes,MAX_STRING_LENGTH_FOR_SPLIT);
						else break;
					}
				}
				// Trying to get a new stack
				if (nThreads>1) stealWork(addressTranslator,random);
			}
			// Terminating if unable to steal work
			isAlive=false;
			latch.countDown();
			deallocate();
		}


		/**
		 * Remark: there is no need to get a lock on $this$ while running $stealWork$.
		 *
		 * @param addressTranslator temporary, reused matrix with 3 rows and
		 * $MAX_STRING_LENGTH_FOR_SPLIT+1$ columns; translates pointers in the donor stack
		 * to pointers in the new donor and new receiver stacks;
		 * @param random used for assigning substrings to donor and receiver.
		 */
		private final void stealWork(long[][] addressTranslator, XorShiftStarRandom random) {
			final int threadMask = 0xFFFFFFFF>>>(32-Utils.log2(nThreads));  // Selects the $Utils.log2(nThreads)$ least significant bits of an int
			int i, j, d, addressTranslator_last;
			int newReceiver_nStringsNotExtended, newDonor_nStringsNotExtended;
			long oldPointer;
			long donorStackLength;  // In bits
			long newDonor_previousSubstringAddress, newReceiver_previousSubstringAddress;
			Stream donorStack, newDonorStack, newReceiverStack;
			Substring w = substringClass.getInstance();
			SubstringIteratorThread donor;

			for (i=0; i<N_STEALING_ATTEMPTS; i++) {
				do {
					d=donorGenerator.getAndIncrement()&threadMask;
					donor=threads[d];
				}
				while (d==threadID);
				if (donor.isAlive&&donor.nStringsNotExtended>=DONOR_STACK_LOWERBOUND) {  // Not synchronized: this is just a guess.
					synchronized(donor) {
						if (!donor.isAlive||donor.nStringsNotExtended<DONOR_STACK_LOWERBOUND) continue;  // Checking again before stealing
						donorStack=donor.stack;
						donorStackLength=donorStack.length();
						newDonorStack = new Stream(donorStack.LONGS_PER_REGION);
						newReceiverStack = new Stream(donorStack.LONGS_PER_REGION);
						newDonor_previousSubstringAddress=0; newDonor_nStringsNotExtended=0;
						newReceiver_previousSubstringAddress=0; newReceiver_nStringsNotExtended=0;
						addressTranslator_last=-1;
						donorStack.setPosition(0);
						while (donorStack.getPosition()<donorStackLength) {
							w.read(donorStack);
							if (w.length>MAX_STRING_LENGTH_FOR_SPLIT) {
								// Copying the rest of $donorStack$ to $newDonorStack$ and quitting
								while (true) {
									w.stackPointers[1]=newDonor_previousSubstringAddress;
									for (j=2; j<w.nPointers; j++) {
										oldPointer=w.stackPointers[j];
										if (oldPointer<=addressTranslator[0][addressTranslator_last]) w.stackPointers[j]=addressTranslator[1][Arrays.binarySearch(addressTranslator[0],0,addressTranslator_last+1,oldPointer)];
										else w.stackPointers[j]=addressTranslator[1][addressTranslator_last]+oldPointer-addressTranslator[0][addressTranslator_last];
									}
									w.push(newDonorStack);
									newDonor_previousSubstringAddress=w.stackPointers[0];
									if (donorStack.getPosition()<donorStackLength) w.read(donorStack);
									else break;
								}
								break;
							}
							if (w.hasBeenExtended) {
								addressTranslator_last++;
								addressTranslator[0][addressTranslator_last]=w.stackPointers[0];
								// New donor stack
								w.stackPointers[1]=newDonor_previousSubstringAddress;
								for (j=2; j<w.nPointers; j++) w.stackPointers[j]=addressTranslator[1][Arrays.binarySearch(addressTranslator[0],0,addressTranslator_last,w.stackPointers[j])];
								w.push(newDonorStack);
								addressTranslator[1][addressTranslator_last]=w.stackPointers[0];
								newDonor_previousSubstringAddress=w.stackPointers[0];
								// New receiver stack
								w.stackPointers[1]=newReceiver_previousSubstringAddress;
								for (j=2; j<w.nPointers; j++) w.stackPointers[j]=addressTranslator[2][Arrays.binarySearch(addressTranslator[0],0,addressTranslator_last,w.stackPointers[j])];
								w.push(newReceiverStack);
								addressTranslator[2][addressTranslator_last]=w.stackPointers[0];
								newReceiver_previousSubstringAddress=w.stackPointers[0];
							}
							else {
								if (random.nextBoolean()) {  // Dividing non-extended strings in approximately two equal parts
									// New donor stack
									w.stackPointers[1]=newDonor_previousSubstringAddress;
									for (j=2; j<w.nPointers; j++) w.stackPointers[j]=addressTranslator[1][Arrays.binarySearch(addressTranslator[0],0,addressTranslator_last+1,w.stackPointers[j])];
									w.push(newDonorStack);
									newDonor_previousSubstringAddress=w.stackPointers[0];
									newDonor_nStringsNotExtended++;
								}
								else {
									// New receiver stack
									w.stackPointers[1]=newReceiver_previousSubstringAddress;
									for (j=2; j<w.nPointers; j++) w.stackPointers[j]=addressTranslator[2][Arrays.binarySearch(addressTranslator[0],0,addressTranslator_last+1,w.stackPointers[j])];
									w.push(newReceiverStack);
									newReceiver_previousSubstringAddress=w.stackPointers[0];
									newReceiver_nStringsNotExtended++;
								}
							}
						}
						donor.stack.deallocate(); donor.stack=newDonorStack;
						donor.stack.setPosition(newDonor_previousSubstringAddress);
						donor.nStringsNotExtended=newDonor_nStringsNotExtended;
						stack.deallocate(); stack=newReceiverStack;
						stack.setPosition(newReceiver_previousSubstringAddress);
						nStringsNotExtended=newReceiver_nStringsNotExtended;
					}
					return;
				}
			}
		}

	}

}