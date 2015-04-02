import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Iterates in parallel over all the distinct substrings of positive length of a given
 * string $s$ using a BWT index, or equivalently following a depth-first search over the
 * trie of the reverse of $s$. The depth-first search logic is programmable, i.e. subclasses
 * of $Substring$ define which left-extensions of the current string should be explored.
 * In a shared-memory machine the search is automatically parallelized, and the user has
 * no control over work distribution -- this resembles general-purpose parallel frameworks
 * like e.g. \cite{finkel1987dib}. The original string $s$ can be safely deallocated after
 * this class has been constructed.
 *
 * Remark: The BWT is implemented as a sequence of Huffman-shaped wavelet trees, one per
 * BWT block. All BWT blocks have approximately the same size (determined by the memory
 * available during construction), so the total size of all wavelet trees is approximately
 * $|s|*H_k(s)+blockSize*alphabetLength^k$ bits for any given $k$, where $H_k(s)$ is the
 * $k$-th order entropy of $s$ \cite{karkkainen2011fixed}. This can be significantly
 * smaller than $|s|*\log_2(alphabetLength)$ if $s$ is compressible.
 *
 * Remark: In practice this fixed-block approach should be approximately as fast as a
 * single Huffman-shaped wavelet tree if $s$ is incompressible, and possibly faster if $s$
 * is compressible \cite{karkkainen2011fixed}.
 */
public class SubstringIterator {
	/**
	 * Constants
	 */
	private final int alphabetLength, log2alphabetLength;
	private int nBlocks;
	private Substring SUBSTRING_CLASS;  // Subclass of $Substring$ to be used during navigation
	private Constants constants;

	/**
	 * BWT index
	 */
	private HuffmanWaveletTree[] waveletTrees;
	private IntArray[] blockCounts;  // Number of occurrences of each character before the beginning of each block, excluding $\$$.
	private IntArray blockStarts;  // Starting position of each block
	private Rank9 blockBoundaries;  // Dense representation of a sparse bitvector, for speed.
	private long[] C;  // The $C$ array in backward search (excludes $\$$).

	/**
	 * $dollar[0]$: position of the dollar character in the BWT;
	 * $dollar[1]$: BWT block containing the dollar character;
	 * $dollar[2]$: position of the dollar character inside the BWT block $dollar[1]$.
	 */
	private long[] dollar;


	/**
	 * @param substringClass subclass of $Substring$ to be used during navigation.
	 */
	public SubstringIterator(IntArray string, int[] alphabet, int alphabetLength, Substring substringClass, Constants constants) {
		this.constants=constants;
		final int stringLength = string.length();
		final int log2stringLength = Utils.log2(stringLength);
		final int log2stringLengthPlusOne = Utils.log2(stringLength+1);
		this.alphabetLength=alphabetLength;
		log2alphabetLength=Utils.log2(alphabetLength);
		int blockSize = Suffixes.blockwiseBWT_getBlockSize(stringLength,log2stringLength,log2alphabetLength,constants);
		nBlocks=Utils.divideAndRoundUp(stringLength,blockSize);  // This value is just an upper bound: $Suffixes.blockwiseBWT$ will set the effective number of blocks.
		if (nBlocks<4) nBlocks=4;
		waveletTrees = new HuffmanWaveletTree[nBlocks];
		blockStarts = new IntArray(nBlocks,log2stringLengthPlusOne,false);
		dollar = new long[3];
		IntArray[] localBlockCounts = new IntArray[nBlocks];
		IntArray bitVector = new IntArray(stringLength+1,1,true);
		Suffixes.blockwiseBWT(string,alphabet,alphabetLength,log2alphabetLength,blockSize,null,waveletTrees,blockStarts,bitVector,localBlockCounts,dollar,constants);
		blockBoundaries = new Rank9(bitVector);
		bitVector.deallocate(); bitVector=null;
		nBlocks=blockStarts.length();  // Setting the effective number of blocks
		SUBSTRING_CLASS=substringClass;

		// Building $blockCounts$ and $C$
		int i, j, max;
		int[] characterCounts = new int[alphabetLength];
		blockCounts = new IntArray[nBlocks];
		blockCounts[0] = new IntArray(alphabetLength,1,true);
		for (i=1; i<nBlocks; i++) {
			max=0;
			for (j=0; j<alphabetLength; j++) {
				characterCounts[j]+=localBlockCounts[i-1].getElementAt(j);
				if (characterCounts[j]>max) max=characterCounts[j];
			}
			blockCounts[i] = new IntArray(alphabetLength,Utils.bitsToEncode(max));
			for (j=0; j<alphabetLength; j++) blockCounts[i].setElementAt(j,(int)characterCounts[j]);
		}
		for (j=0; j<alphabetLength; j++) characterCounts[j]+=localBlockCounts[nBlocks-1].getElementAt(j);
		C = new long[alphabetLength];
		C[0]=1;
		for (j=1; j<alphabetLength; j++) C[j]=C[j-1]+characterCounts[j-1];
	}


	/**
	 * Extends to the left the first substring $w$ from the top of $stack$ that has not
	 * been extended yet, popping out of $stack$ all the substrings met before $w$ that
	 * have already been extended. Extensions $aw$, $a \in \{\Sigma \cup \$\}$, such that
	 * their method $occurs$ returns true, are notified by calling their method $visited$,
	 * and they are pushed onto $stack$ if their method $shouldBeExtendedLeft$ returns
	 * true.
	 *
	 * @param stack the stream pointer is assumed to be at the first bit of the serialized
	 * substring at the top of $stack$;
	 * @param w non-null temporary, reused container representing the string at the top of
	 * $stack$;
	 * @param leftExtensions $alphabetLength+1$ non-null temporary, reused containers
	 * representing $aw$ for all $a \in \Sigma$; $\$$ is assigned element 0, and all other
	 * characters are shifted forward by one;
	 * @param positions $w.nIntervals*2$ non-null temporary, reused containers of the
	 * interval positions of $w$;
	 * @param multirankStack temporary, reused space with $1+w.nIntervals*2$ columns and
	 * $alphabetLength-1$ rows used by $HuffmanWaveletTree.multirank$;
	 * @param multirankOutput temporary, reused space with $alphabetLength+1$ rows and
	 * $w.nIntervals*2$ columns used by $HuffmanWaveletTree.multirank$;
	 * @param multirankOnes temporary, reused space with $w.nIntervals*2$ cells used by
	 * $HuffmanWaveletTree.multirank$;
	 * @param out cell 0: the (possibly negative) variation in the total number of strings
	 * present in $stack$, induced by this call to $extendLeft$;
	 * cell 1: the variation $-1 \leq \delta < alphabetLength+1$ in the number of
	 * \emph{non-extended} strings $v$ in $stack$, induced by this call to $extendLeft$;
	 * cell 2: as in cell 1, but only for strings with $|v| \leq maxStringLengthToReport$.
	 */
	private final void extendLeft(Stream stack, Substring w, Substring[] leftExtensions, Position[] positions, long[][] multirankStack, long[][] multirankOutput, long[] multirankOnes, int maxStringLengthToReport, int[] out) {
		final boolean isShort;
		boolean pushed;
		int i, j, c, p, windowFirst, windowSize, block, previousBlock, nPositions;
		long pos, previous;
		Substring extension;

		// Reading the top of $stack$
		out[0]=0; out[1]=0; out[2]=0;
		w.readFast(stack);
		while (w.hasBeenExtended || w.hasBeenStolen) {
			previous=w.stackPointers[1];
			w.pop(stack);
			stack.setPosition(previous);
			out[0]--;
			if (previous==0) return;
			w.readFast(stack);
		}

		// Putting the positions of $w.bwtIntervals$ in block order, and sequentially
		// inside each block. Since this iterator is generic, we do not assume the
		// positions in $w.bwtIntervals$ to be already sorted.
		for (i=0; i<w.nIntervals; i++) {
			p=i<<1; pos=w.bwtIntervals[i][0];
			positions[p].position=pos; positions[p].row=i; positions[p].column=0;
			positions[p].block=(int)blockBoundaries.rank(pos+1)-1;
			p=(i<<1)+1; pos=w.bwtIntervals[i][1]+1;
			positions[p].position=pos; positions[p].row=i; positions[p].column=1;
			positions[p].block=(int)blockBoundaries.rank(pos+1)-1;
		}
		Arrays.sort(positions);
		nPositions=w.nIntervals<<1;

		// Ranking all positions in the same block using exactly one $multirank$ call
		windowFirst=0; windowSize=1;
		previousBlock=positions[windowFirst].block;
		multirankStack[0][1]=positions[windowFirst].position-blockStarts.getElementAt(previousBlock);
		for (p=1; p<nPositions; p++) {
			block=positions[p].block;
			if (block==previousBlock) {
				windowSize++;
				multirankStack[0][windowSize]=positions[p].position-blockStarts.getElementAt(block);
			}
			else {
				for (i=0; i<multirankOutput.length; i++) {
					for (j=0; j<multirankOutput[i].length; j++) multirankOutput[i][j]=0;
				}
				handleLeftExtensionsByDollar(positions,windowFirst,windowSize,previousBlock,leftExtensions,multirankStack);
				if (waveletTrees[previousBlock]!=null) {
					// There can be exactly one block with null elements in $waveletTrees$:
					// it corresponds to a splitter at the position of $\$$ in the BWT,
					// preceded by another splitter.
					waveletTrees[previousBlock].multirank(alphabetLength,windowSize,multirankStack,multirankOutput,multirankOnes);
				}
				for (c=0; c<alphabetLength; c++) {
					for (i=0; i<windowSize; i++) leftExtensions[c+1].bwtIntervals[positions[windowFirst+i].row][positions[windowFirst+i].column]=C[c]+(blockCounts[previousBlock].getElementAt(c)+multirankOutput[c][i])+(positions[windowFirst+i].column==0?0:-1);
				}
				windowFirst=p; windowSize=1; previousBlock=block;
				multirankStack[0][1]=positions[p].position-blockStarts.getElementAt(block);
			}
		}
		// Last block
		handleLeftExtensionsByDollar(positions,windowFirst,windowSize,previousBlock,leftExtensions,multirankStack);
		for (i=0; i<multirankOutput.length; i++) {
			for (j=0; j<multirankOutput[i].length; j++) multirankOutput[i][j]=0;
		}
		if (waveletTrees[previousBlock]!=null) waveletTrees[previousBlock].multirank(alphabetLength,windowSize,multirankStack,multirankOutput,multirankOnes);
		for (c=0; c<alphabetLength; c++) {
			for (i=0; i<windowSize; i++) leftExtensions[c+1].bwtIntervals[positions[windowFirst+i].row][positions[windowFirst+i].column]=C[c]+(blockCounts[previousBlock].getElementAt(c)+multirankOutput[c][i])+(positions[windowFirst+i].column==0?0:-1);
		}

		// Signalling to the left-extensions of $w$, and pushing them onto $stack$.
		isShort=w.length+1<=maxStringLengthToReport;
		extension=null; pushed=false;
		previous=w.stackPointers[0];
		for (c=0; c<alphabetLength+1; c++) {
			extension=leftExtensions[c];
			if (extension.occurs()) {
				extension.init(w,c-1);
				extension.visited(stack);
				if (extension.shouldBeExtendedLeft()) {
					extension.stackPointers[1]=previous;
					extension.push(stack);
					previous=extension.stackPointers[0];
					out[0]++; out[1]++; pushed=true;
					if (isShort) out[2]++;
				}
			}
		}
		w.markAsExtended(stack);
		out[1]--;
		if (w.length<=maxStringLengthToReport) out[2]--;
		stack.setPosition(pushed?previous:w.stackPointers[0]);
	}


	private static class Position implements Comparable {
		protected long position;
		protected int block, row, column;

		public int compareTo(Object other) {
			Position otherPosition = (Position)other;
			if (block<otherPosition.block) return -1;
			if (block>otherPosition.block) return 1;
			if (position<otherPosition.position) return -1;
			if (position>otherPosition.position) return 1;
			return 0;
		}
	}


	/**
	 * Handles the left-extension by $\$$ in $extendLeft$.
	 */
	private final void handleLeftExtensionsByDollar(Position[] positions, int firstPosition, int nPositions, int block, Substring[] leftExtensions, long[][] multirankStack) {
		int i;
		if (block>dollar[1]) {
			for (i=1; i<=nPositions; i++) {
				if (positions[firstPosition+i-1].column==0) leftExtensions[0].bwtIntervals[positions[firstPosition+i-1].row][positions[firstPosition+i-1].column]=1;
				else leftExtensions[0].bwtIntervals[positions[firstPosition+i-1].row][positions[firstPosition+i-1].column]=0;
			}
		}
		else if (block<dollar[1]) {
			for (i=1; i<=nPositions; i++) {
				if (positions[firstPosition+i-1].column==0) leftExtensions[0].bwtIntervals[positions[firstPosition+i-1].row][positions[firstPosition+i-1].column]=0;
				else leftExtensions[0].bwtIntervals[positions[firstPosition+i-1].row][positions[firstPosition+i-1].column]=-1;
			}
		}
		else {
			for (i=1; i<=nPositions && positions[firstPosition+i-1].position<=dollar[0]; i++) {
				if (positions[firstPosition+i-1].column==0) leftExtensions[0].bwtIntervals[positions[firstPosition+i-1].row][positions[firstPosition+i-1].column]=0;
				else leftExtensions[0].bwtIntervals[positions[firstPosition+i-1].row][positions[firstPosition+i-1].column]=-1;
			}
			for (; i<=nPositions; i++) {
				if (positions[firstPosition+i-1].column==0) leftExtensions[0].bwtIntervals[positions[firstPosition+i-1].row][positions[firstPosition+i-1].column]=1;
				else leftExtensions[0].bwtIntervals[positions[firstPosition+i-1].row][positions[firstPosition+i-1].column]=0;
				multirankStack[0][i]--;  // This wavelet tree does not contain the position of $\$$
			}
		}
	}


	/**
	 * @param nThreads maximum number of threads to be used during traversal.
	 */
	public void run() {
		int i, nCharacters;
		long previous;
		SubstringIteratorThread[] threads = new SubstringIteratorThread[constants.N_THREADS];
		AtomicInteger donorGenerator = new AtomicInteger();
		CountDownLatch latch = new CountDownLatch(constants.N_THREADS);
		for (i=0; i<constants.N_THREADS; i++) threads[i] = new SubstringIteratorThread(threads,i,donorGenerator,latch);

		// Initializing the stack of $threads[0]$ with $\epsilon$, the distinct characters
		// in the text, and $\$$. The distinct characters in the text might be a proper
		// subset of the full alphabet. $\epsilon$ is pushed in order to detect when the
		// stack becomes empty by issuing $stack.getPosition()>0$ (we cannot store
		// negative numbers in the stack). Thus, a stack always contains at least one
		// string, except for the stacks of threads different from $threads[0]$
		// immediately after their creation.
		Substring epsilon = SUBSTRING_CLASS.getInstance();
		epsilon.hasBeenExtended=true;
		epsilon.push(threads[0].stack);
		epsilon.deallocate(); epsilon=null;
		Substring[] lengthOneSubstrings = new Substring[alphabetLength+1];
		nCharacters=SUBSTRING_CLASS.getLengthOneSubstrings(C,lengthOneSubstrings);
		lengthOneSubstrings[0].visited(threads[0].stack);  // $\$$, not pushed on the stack but visited.
		previous=0L;
		for (i=1; i<nCharacters; i++) {  // Other characters
			lengthOneSubstrings[i].visited(threads[0].stack);
			lengthOneSubstrings[i].stackPointers[1]=previous;
			lengthOneSubstrings[i].stackPointers[2]=0L;
			lengthOneSubstrings[i].push(threads[0].stack);
			previous=lengthOneSubstrings[i].stackPointers[0];
		}
		threads[0].stack.setPosition(lengthOneSubstrings[nCharacters-1].stackPointers[0]);
		threads[0].nStrings=nCharacters;
		threads[0].nStringsNotExtended=nCharacters-1;
		threads[0].nShortStringsNotExtended=nCharacters-1;
		for (i=0; i<nCharacters; i++) {
			lengthOneSubstrings[i].deallocate();
			lengthOneSubstrings[i]=null;
		}
		lengthOneSubstrings=null;

		// Launching all threads
		for (i=0; i<constants.N_THREADS; i++) threads[i].start();
		try { latch.await(); }
		catch(InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}


	/**
	 * Explores a partition of the trie of the reverse of $s$ in depth-first order, by
	 * repeatedly invoking $extendLeft$. For load-balancing, the thread uses the
	 * work-stealing strategy described in \cite{rao1987parallel}.
	 *
	 * Remark: A thread with empty stack scans the list of threads for a donor
	 * sequentially, using an atomic donor pointer shared by all threads as described in
	 * \cite{kumar1987parallel}.
	 *
	 * Remark: The current work-stealing strategy requires splitting a prefix of the
	 * stack of the donor. This allows to trade load balancing (which, in the general case
	 * of an irregular trie, improves with the length of the prefix) with the speed of the
	 * work-balancing code (which degrades with the length of the prefix).
	 *
	 * Remark: A simple lower bound on the size of the donor stack up to the prefix
	 * threshold tries to limit cases in which splitting the stack is slower than having
	 * it processed by the donor itself. We use this simple strategy because this engine
	 * is general-purpose, so we cannot estimate exploration time (we might add a
	 * corresponding method to $Substring$ and force subclasses to extend it: we leave
	 * this to future extensions).
	 *
	 * Remark: Another possible improvement described in \cite{rao1987parallel} consists
	 * in avoiding the synchronization with the donor thread if the donor is working
	 * deeply enough with respect to the split prefix. Once again, we can't implement this
	 * approach in a general-purpose engine, because we cannot estimate expansion time.
	 *
	 * Remark: We choose not to precompute a large, static set of fine-grained, fixed-size
	 * workpackets, as described in \cite{reinefeld1994work}, because we want to use as
	 * little space as possible -- voluntarily paying the smaller space with the workload
	 * imbalance and communication overheads that come from dynamic workpackets of
	 * variable size.
	 */
	protected class SubstringIteratorThread extends Thread {
		/**
		 * Number of stack pointers used by $Substring$ objects
		 */
		private final int MIN_POINTERS;

		/**
		 * Number of stack pointers used by $substringClass$ objects
		 */
		private final int N_POINTERS;

		/**
		 * Thread variables
		 */
		protected Stream stack;
		protected int nStrings;  // Total number of strings in $stack$
		protected int nStringsNotExtended;  // Number of strings in $stack$ that have not been extended
		protected int nShortStringsNotExtended;  // Number of strings in $stack$ that have not been extended, and that have length $<=MAX_STRING_LENGTH_FOR_SPLIT$.
		private boolean isAlive;  // Flags a dead thread
		private SubstringIteratorThread[] threads;  // Pointers to all threads
		private final int nThreads;  // Number of threads in $threads$
		private final int threadID;  // Position of this thread in $threads$
		private AtomicInteger donorGenerator;  // Global generator of donor pointers
		private CountDownLatch latch;  // Global barrier

		/*
		 * $stealWork$-related variables
		 */
		private SubstringIteratorThread donor;
		private Stream donorStack;
		private long donorStackLength;  // In bits
		private long newStack_previousSubstringAddress;
		private Stream translatorFrom, translatorTo;  // Translate pointers of extended strings in $donorStack$ ($translatorFrom[i]$) to pointers of extended strings in the new (receiver) $stack$ ($translatorTo[i]$).
		private long translator_last;  // Last element used in $translator*$
		private XorShiftStarRandom random;


		public SubstringIteratorThread(SubstringIteratorThread[] threads, int threadID, AtomicInteger donorGenerator, CountDownLatch latch) {
			MIN_POINTERS=Substring.MIN_POINTERS;
			N_POINTERS=SUBSTRING_CLASS.nPointers;
			this.threads=threads;
			nThreads=threads.length;
			this.threadID=threadID;
			this.donorGenerator=donorGenerator;
			this.latch=latch;
			stack = new Stream(constants.LONGS_PER_REGION);
			random = new XorShiftStarRandom();
			translatorFrom = new Stream(constants.LONGS_PER_REGION);
			translatorTo = new Stream(constants.LONGS_PER_REGION);
		}


		private final void deallocate() {
			stack.deallocate(); stack=null;
			threads=null;
			donor=null;
			donorStack=null;
			translatorFrom.deallocate(); translatorFrom=null;
			translatorTo.deallocate(); translatorTo=null;
			random=null;
		}


		public void run() {
			int i;
			Substring[] leftExtensions = new Substring[alphabetLength+1];
			for (i=0; i<alphabetLength+1; i++) leftExtensions[i]=SUBSTRING_CLASS.getInstance();
			final int maxPositions = SUBSTRING_CLASS.nIntervals<<1;
			Position[] positions = new Position[maxPositions];
			for (i=0; i<maxPositions; i++) positions[i] = new Position();
			long[][] multirankStack = new long[alphabetLength-1][1+maxPositions];
			long[][] multirankOutput = new long[alphabetLength][maxPositions];
			long[] multirankOnes = new long[maxPositions];
			int[] extendLeftOutput = new int[3];
			Substring w = SUBSTRING_CLASS.getInstance();

			isAlive=true;
			if (constants.N_THREADS>1 && stack.getPosition()==0) stealWork();
			while (stack.getPosition()>0) {
				// Exhausting the current stack
				while (true) {
					synchronized(this) {
						if (stack.getPosition()>0) {
							extendLeft(stack,w,leftExtensions,positions,multirankStack,multirankOutput,multirankOnes,constants.MAX_STRING_LENGTH_FOR_SPLIT,extendLeftOutput);
							nStrings+=extendLeftOutput[0];
							nStringsNotExtended+=extendLeftOutput[1];
							nShortStringsNotExtended+=extendLeftOutput[2];
						}
						else break;
					}
				}
				// Trying to get a new stack
				if (constants.N_THREADS>1) stealWork();
			}
			// Terminating if unable to steal work
			isAlive=false;
			latch.countDown();
			deallocate();
		}


		/**
		 * Remark: there is no need to get a lock on $this$ while running $stealWork$.
		 * Remark: the procedure avoids reallocating memory.
		 */
		private final void stealWork() {
			int i, d;
			long copied, toBeCopied;
			Substring w = SUBSTRING_CLASS.getInstance();

			for (i=0; i<constants.N_STEALING_ATTEMPTS; i++) {
				do {
					d=donorGenerator.getAndIncrement()%nThreads;
					donor=threads[d];
				}
				while (d==threadID);
				if (!donor.isAlive || donor.nShortStringsNotExtended<constants.DONOR_STACK_LOWERBOUND) continue;  // Not synchronized: this is just a guess.
				synchronized(donor) {
					if (!donor.isAlive || donor.nShortStringsNotExtended<constants.DONOR_STACK_LOWERBOUND) continue;  // Checking again before stealing
					donorStack=donor.stack;
					donorStackLength=donorStack.length();
					stack.clear(false);  // Avoids reallocation
					nStrings=0;
					nStringsNotExtended=0;
					nShortStringsNotExtended=0;
					newStack_previousSubstringAddress=0;
					translatorFrom.clear(false);  // Avoids reallocation
					translatorTo.clear(false);  // Avoids reallocation
					translator_last=-1;
					toBeCopied=donor.nShortStringsNotExtended>>1;
					copied=0;
					long backupPointer = donorStack.getPosition();
					donorStack.setPosition(0);
					while (copied<toBeCopied) {
						w.read(donorStack);
						if (!w.hasBeenExtended && !w.hasBeenStolen) copied++;
						if (!w.hasBeenStolen) copy(w);
					}
					stack.setPosition(newStack_previousSubstringAddress);
					donorStack.setPosition(backupPointer);
					return;
				}
			}
		}


		/**
		 * Extended strings in the donor stack are copied to the new receiver stack.
		 * Non-extended strings in the donor stack are copied to the new receiver stack
		 * and marked as stolen in the donor stack, so they will be automatically popped
		 * out and discarded by $extendLeft$.
		 */
		private final void copy(Substring w) {
			if (w.hasBeenExtended) {
				translator_last++;
				translatorFrom.push(w.stackPointers[0],64);
			}
			else {
				w.markAsStolen(donorStack);
				donor.nStringsNotExtended--;
				donor.nShortStringsNotExtended--;
			}
			for (int i=MIN_POINTERS-1; i<N_POINTERS; i++) {
				if (w.stackPointers[i]==0) w.stackPointers[i]=0;
				else {
					translatorTo.setPosition(translatorFrom.binarySearch(0,w.hasBeenExtended?translator_last:translator_last+1,w.stackPointers[i],64,6));
					w.stackPointers[i]=translatorTo.read(64);
				}
			}
			w.stackPointers[1]=newStack_previousSubstringAddress;
			w.push(stack);
			nStrings++;
			if (w.hasBeenExtended) translatorTo.push(w.stackPointers[0],64);
			else {
				nStringsNotExtended++;
				nShortStringsNotExtended++;
			}
			newStack_previousSubstringAddress=w.stackPointers[0];
		}

	}  // SubstringIteratorThread

}