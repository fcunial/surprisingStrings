
import java.util.Arrays;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class Tests {

	public static void main(String[] args) {
/*		// Testing $IntArray$
		if (!test_swap()) {
			System.err.println("IntArray.swap \t\t\t [ FAILED ]");
			System.exit(1);
		}
		else System.out.println("IntArray.swap \t\t\t [   OK   ]");
		if (!test_vecswap()) {
			System.err.println("IntArray.vecswap \t\t\t [ FAILED ]");
			System.exit(1);
		}
		else System.out.println("IntArray.vecswap \t\t\t [   OK   ]");
		if (!test_heapSort()) {
			System.err.println("IntArray.heapSort \t\t\t [ FAILED ]");
			System.exit(1);
		}
		else System.out.println("IntArray.heapSort \t\t\t [   OK   ]");
		if (!test_pasteAtPointer()) {
			System.err.println("IntArray.pasteAtPointer \t\t\t [ FAILED ]");
			System.exit(1);
		}
		else System.out.println("IntArray.pasteAtPointer \t\t\t [   OK   ]");
		if (!test_incrementElementAt()) {
			System.err.println("IntArray.incrementElementAt \t\t\t [ FAILED ]");
			System.exit(1);
		}
		else System.out.println("IntArray.incrementElementAt \t\t\t [   OK   ]");
		if (!test_pushFromRight()) {
			System.err.println("IntArray.pushFromRight \t\t\t [ FAILED ]");
			System.exit(1);
		}
		else System.out.println("IntArray.pushFromRight \t\t\t [   OK   ]");

		// Testing $Rank9$
		if (!test_rank9()) {
			System.err.println("Rank9.rank \t\t\t [ FAILED ]");
			System.exit(1);
		}
		else System.out.println("Rank9.rank \t\t\t [   OK   ]");

		// Testing $HuffmanWaveletTree$
		if (!test_huffmanWaveletTree()) {
			System.err.println("HuffmanWaveletTree \t\t\t [ FAILED ]");
			System.exit(1);
		}
		else System.out.println("HuffmanWaveletTree \t\t\t [   OK   ]");

		// Testing $Suffixes$
		if (!test_sort()) {
			System.err.println("Suffixes.sort \t\t\t [ FAILED ]");
			System.exit(1);
		}
		else System.out.println("Suffixes.sort \t\t\t [   OK   ]");
		if (!test_buildLCPArray()) {
			System.err.println("Suffixes.buildLCPArray \t\t\t [ FAILED ]");
			System.exit(1);
		}
		else System.out.println("Suffixes.buildLCPArray \t\t\t [   OK   ]");
		if (!test_intervalOfSuffixes()) {
			System.err.println("Suffixes.intervalOfSuffixes \t\t\t [ FAILED ]");
			System.exit(1);
		}
		else System.out.println("Suffixes.intervalOfSuffixes \t\t\t [   OK   ]");
		if (!test_buildBinarySearchCache()) {
			System.err.println("Suffixes.buildBinarySearchCache \t\t\t [ FAILED ]");
			System.exit(1);
		}
		else System.out.println("Suffixes.buildBinarySearchCache \t\t\t [   OK   ]");
		if (!test_assignSuffixToBlock()) {
			System.err.println("Suffixes.assignSuffixToBlock \t\t\t [ FAILED ]");
			System.exit(1);
		}
		else System.out.println("Suffixes.assignSuffixToBlock \t\t\t [   OK   ]");
		if (!test_blockwiseBWT()) {
			System.err.println("Suffixes.blockwiseBWT \t\t\t [ FAILED ]");
			System.exit(1);
		}
		else System.out.println("Suffixes.blockwiseBWT \t\t\t [   OK   ]");

		// Testing $Stream$
		if (!test_stream()) {
			System.err.println("Stream \t\t\t [ FAILED ]");
			System.exit(1);
		}
		else System.out.println("Stream \t\t\t [   OK   ]");

		// Testing $Substring$
		if (!test_substring()) {
			System.err.println("Substring \t\t\t [ FAILED ]");
			System.exit(1);
		}
		else System.out.println("Substring \t\t\t [   OK   ]");
*/
		// Testing $SubstringIterator$
		if (!test_substringIterator()) {
			System.err.println("SubstringIterator \t\t\t [ FAILED ]");
			System.exit(1);
		}
		else System.out.println("SubstringIterator \t\t\t [   OK   ]");
	}




	private static HashSet<String> iteratorSubstrings;



	/**
	 * Set $blockSize=2;$ in the constructor of $SubstringIterator$ when testing with
	 * small strings.
	 */
	private static final boolean test_substringIterator() {
		final int STRING_LENGTH = 100;
		final int N_ITERATIONS = 100;
		Constants constants = new Constants();
		int i, j, k, c, dollarPosition;
		int[] alphabet = new int[] {0,1,2,3};
		String stringString = new String();
		String[] trueSubstringsArray, iteratorSubstringsArray;
		IntArray string;
		XorShiftStarRandom random = new XorShiftStarRandom();
		SubstringIterator iterator;
		HashSet<String> trueSubstrings;

		string = new IntArray(STRING_LENGTH,2,false);
		for (i=0; i<N_ITERATIONS; i++) {
			stringString="";
			string.clear();
 			for (j=0; j<STRING_LENGTH; j++) {
 				c=random.nextInt(3);
 				stringString+=""+c;
 				string.push(c);
 			}
//System.out.print("String: "); string.print();
//System.out.println();

			// Trivial enumeration of all distinct substrings
			trueSubstrings = new HashSet<String>();
			for (j=0; j<STRING_LENGTH; j++) {
				for (k=j+1; k<=STRING_LENGTH; k++) trueSubstrings.add(stringString.substring(j,k));
			}
			for (k=1; k<=STRING_LENGTH; k++) trueSubstrings.add("$"+stringString.substring(0,k));
			trueSubstrings.add("$");
			trueSubstringsArray = new String[trueSubstrings.size()];
			trueSubstrings.toArray(trueSubstringsArray);
			Arrays.sort(trueSubstringsArray);
//System.out.println("Trivial enumeration completed: "+trueSubstringsArray.length+" distinct strings:");
//for (int x=0; x<trueSubstringsArray.length; x++) System.out.println(trueSubstringsArray[x]);

			// Building BWT for reporting
			IntArray bwt = new IntArray(STRING_LENGTH+1,2,true);
			dollarPosition=Suffixes.blockwiseBWT(string,alphabet,4,2,2,bwt,null,null,null,null,null,constants);
//System.out.println("BWT: "); bwt.print(); System.out.println();

/*			// Running $SubstringIterator$ with one thread
			constants.N_THREADS=1;
			constants.MAX_MEMORY=10;
			iteratorSubstrings = new HashSet<String>();
			iterator = new SubstringIterator(string,alphabet,4,new TestSubstring(4,2,STRING_LENGTH,Utils.log2(STRING_LENGTH),stringString),constants);
			iterator.run();
			iteratorSubstringsArray = new String[iteratorSubstrings.size()];
			iteratorSubstrings.toArray(iteratorSubstringsArray);
			Arrays.sort(iteratorSubstringsArray);
//System.out.println("SubstringIterator enumeration completed: ");
//for (int x=0; x<iteratorSubstringsArray.length; x++) System.out.println(iteratorSubstringsArray[x]);
			// Tests
			if (iteratorSubstrings.size()!=trueSubstrings.size()) {
				System.out.println("Error in SubstringIterator with one thread: correct total substrings="+trueSubstrings.size()+" enumerated="+iteratorSubstrings.size());
				System.out.println("string: "+stringString);
				return false;
			}

			for (int x=0; x<iteratorSubstringsArray.length; x++) {
				if (Arrays.binarySearch(trueSubstringsArray,iteratorSubstringsArray[x])<0) {
					System.out.println("Error in SubstringIterator with one thread: the enumerated substring "+iteratorSubstringsArray[x]+" does not exist.");
					System.out.println("string: "+stringString);
					return false;
				}
			}
			for (int x=0; x<trueSubstringsArray.length; x++) {
				if (Arrays.binarySearch(iteratorSubstringsArray,trueSubstringsArray[x])<0) {
					System.out.println("Error in SubstringIterator with one thread: substring "+trueSubstringsArray[x]+" in the string has not been enumerated.");
					System.out.println("string: "+stringString);
					return false;
				}
			}
*/
			// Running $SubstringIterator$ with multiple threads
			constants.N_THREADS=2;
			constants.MAX_MEMORY=10;
			iteratorSubstrings = new HashSet<String>();
			iterator = new SubstringIterator(string,alphabet,4,new TestSubstring(4,2,STRING_LENGTH,Utils.log2(STRING_LENGTH),stringString),constants);
			System.out.print("(");
			iterator.run();
			System.out.print(")");
			iteratorSubstringsArray = new String[iteratorSubstrings.size()];
			iteratorSubstrings.toArray(iteratorSubstringsArray);
			Arrays.sort(iteratorSubstringsArray);
//System.out.println("SubstringIterator enumeration completed: ");
//for (int x=0; x<iteratorSubstringsArray.length; x++) System.out.println(iteratorSubstringsArray[x]);
			// Tests
			if (iteratorSubstrings.size()!=trueSubstrings.size()) {
				System.out.println("Error in SubstringIterator with two threads: correct total substrings="+trueSubstrings.size()+" enumerated="+iteratorSubstrings.size());
				System.out.println("string: "+stringString);
				return false;
			}
			for (int x=0; x<iteratorSubstringsArray.length; x++) {
				if (Arrays.binarySearch(trueSubstringsArray,iteratorSubstringsArray[x])<0) {
					System.out.println("Error in SubstringIterator with two threads: the enumerated substring "+iteratorSubstringsArray[x]+" does not exist.");
					System.out.println("string: "+stringString);
					return false;
				}
			}
			for (int x=0; x<trueSubstringsArray.length; x++) {
				if (Arrays.binarySearch(iteratorSubstringsArray,trueSubstringsArray[x])<0) {
					System.out.println("Error in SubstringIterator with two threads: substring "+trueSubstringsArray[x]+" in the string has not been enumerated.");
					System.out.println("string: "+stringString);
					return false;
				}
			}
		}

		return true;
	}


	private static class TestSubstring extends Substring {
		private String text;

		public TestSubstring(int alphabetLength, int log2alphabetLength, int textLength, int log2textLength, String text) {
			super(alphabetLength,log2alphabetLength,textLength,log2textLength);
			this.text=text;
		}

		protected Substring getInstance() {
			return new TestSubstring(alphabetLength,log2alphabetLength,textLength,log2textLength,text);
		}

		protected void visited(Stream stack) {
			if (length>textLength+1) {
				System.err.println("ERROR: GENERATED A SUBSTRING LONGER THAN THE TEXT PLUS ONE: (length="+length+")");
				System.err.println("text: "+text);
				System.exit(1);
			}
			boolean startsWithDollar;
			String str;
			IntArray sequence = new IntArray((int)length,log2alphabetLength,false);
			startsWithDollar=Substring.getSequence(this,stack,sequence);
			if (startsWithDollar) str="$";
			else str="";
			for (int i=0; i<(startsWithDollar?length-1:length); i++) str+=""+sequence.getElementAt(i);
			synchronized(iteratorSubstrings) { iteratorSubstrings.add(str); }
		}
	}


	private static final boolean test_substring() {
		final int TEXT_LENGTH = 1000;
		final int N_ELEMENTS = 10000;
		final int N_ITERATIONS = 100;
		final int N_TESTS = 100;
		int i, j, t, index;
		long previous;
		Substring[] substrings = new Substring[N_ELEMENTS];
		Stream stack = new Stream(512);
		Substring w = new Substring(4,2,TEXT_LENGTH,Utils.log2(TEXT_LENGTH));
		XorShiftStarRandom random = new XorShiftStarRandom();

		for (t=0; t<N_ITERATIONS; t++) {
			// Pushing random $Substring$ objects on the stack
			stack.clear(true); stack.setPosition(0L);
			substrings[0] = new Substring(4,2,TEXT_LENGTH,Utils.log2(TEXT_LENGTH));
			substrings[0].bwtIntervals[0][0]=0L;
			substrings[0].bwtIntervals[0][1]=TEXT_LENGTH-1;
			substrings[0].push(stack);
			previous=0L;
			for (i=1; i<N_ELEMENTS; i++) {
				substrings[i] = new Substring(4,2,TEXT_LENGTH,Utils.log2(TEXT_LENGTH));
				substrings[i].bwtIntervals[0][0]=random.nextInt(TEXT_LENGTH);
				do { substrings[i].bwtIntervals[0][1]=random.nextInt(TEXT_LENGTH); }
				while (substrings[i].bwtIntervals[0][1]<substrings[i].bwtIntervals[0][0]);
				substrings[i].stackPointers[1]=previous;
				substrings[i].init(substrings[random.nextInt(i)],random.nextInt(4));
				substrings[i].push(stack);
				previous=substrings[i].stackPointers[0];
			}

			// Testing $read$
			for (i=0; i<N_TESTS; i++) {
				index=random.nextInt(N_ELEMENTS);
				stack.setPosition(substrings[index].stackPointers[0]);
				w.read(stack);
				if (!w.occurs()||!w.equals(substrings[index])) {
					System.err.println("Error in read");
					return false;
				}
				stack.setPosition(substrings[index].stackPointers[0]);
				w.readFast(stack);
				if (!w.occurs()||!w.equals(substrings[index])) {
					System.err.println("Error in readFast");
					return false;
				}
			}

			// Testing $pop$
			for (i=0; i<N_TESTS; i++) {
				index=random.nextInt(N_ELEMENTS);
				stack.setPosition(substrings[N_ELEMENTS-1].stackPointers[0]);
				for (j=N_ELEMENTS-1; j>index; j--) substrings[j].pop(stack);
				if (stack.getPosition()!=substrings[index].stackPointers[0]) {
					System.err.println("Error in pop");
					return false;
				}
				w.read(stack);
				if (!w.equals(substrings[index])) {
					System.err.println("Error in pop");
					return false;
				}
				// Pushing back the remaining substrings
				for (j=index+1; j<N_ELEMENTS; j++) substrings[j].push(stack);
			}

			// Testing $skip$
			for (i=0; i<N_TESTS; i++) {
				index=random.nextInt(N_ELEMENTS-1);
				stack.setPosition(substrings[index].stackPointers[0]);
				substrings[index].skip(stack);
				if (stack.getPosition()!=substrings[index+1].stackPointers[0]) {
					System.err.println("Error in skip. Reported position: "+stack.getPosition()+" correct position: "+substrings[index+1].stackPointers[0]);
					return false;
				}
			}
		}
		return true;
	}


	private static final boolean test_stream() {
		final int N_ELEMENTS = 10000;
		final int N_ITERATIONS = 100;
		final int MAX_INT = 100;
		int i, j, k, t, r, b, position, region, cell, offset;
		int[] numbers = new int[N_ELEMENTS];
		long nBits, read;
		Stream stream = new Stream(8);
		XorShiftStarRandom random = new XorShiftStarRandom();

		for (t=0; t<N_ITERATIONS; t++) {
			// Testing $push$
			stream.clear(true);
			nBits=0;
			for (i=0; i<N_ELEMENTS; i++) {
				r=random.nextInt(MAX_INT);
				numbers[i]=r;
				stream.push(r,Utils.bitsToEncode(r));
				nBits+=Utils.bitsToEncode(r);
			}
			if (stream.length()!=nBits) {
				System.err.println("Error in Stream.push: pushed "+stream.length()+" bits rather than "+nBits);
				return false;
			}
			stream.setPosition(0L);
			for (i=0; i<N_ELEMENTS; i++) {
				read=stream.read(Utils.bitsToEncode(numbers[i]));
				if (read!=numbers[i]) {
					System.err.println("Error in Stream.push: pushed "+read+" rather than "+numbers[i]);
					return false;
				}
			}

			// Testing $pop$
			nBits=stream.length();
			j=random.nextInt(N_ELEMENTS);
			for (i=N_ELEMENTS-1; i>=j; i--) {
				stream.pop(Utils.bitsToEncode(numbers[i]));
				nBits-=Utils.bitsToEncode(numbers[i]);
			}
			if (stream.length()!=nBits) {
				System.err.println("Error in Stream.pop: wrong number of bits popped.");
				return false;
			}
			stream.setPosition(0);
			for (i=0; i<j; i++) {
				read=stream.read(Utils.bitsToEncode(numbers[i]));
				if (read!=numbers[i]) {
					System.err.println("Error in Stream.read: wrong bit sequences popped.");
					return false;
				}
			}
			for (i=j; i<N_ELEMENTS; i++) stream.push(numbers[i],Utils.bitsToEncode(numbers[i]));  // Pushing popped values back

			// Testing $setPosition$ and $getPosition$
			j=random.nextInt(N_ELEMENTS);
			nBits=0;
			for (i=0; i<j; i++) nBits+=Utils.bitsToEncode(numbers[i]);
			stream.setPosition(nBits);
			if (stream.getPosition()!=nBits) {
				System.err.println("Error in Stream.setPosition: wrong position");
				return false;
			}
			read=stream.read(Utils.bitsToEncode(numbers[j]));
			if (read!=numbers[j]) {
				System.err.println("Error in Stream.setPosition: read "+read+" rather than "+numbers[j]+".");
				return false;
			}

			// Testing $setBit$
			position=(int)stream.getPosition();
			j=random.nextInt((int)stream.length());
			stream.setPosition(j);
			region=stream.pointerRegion;
			cell=stream.pointerCell;
			offset=stream.pointerOffset;
			stream.setPosition(position);
			stream.setBit(j);
			if ((stream.regions[region][cell]&Utils.oneSelectors1[64-offset-1])==0L) {
				String str = Long.toBinaryString(stream.regions[region][cell]);
				while (str.length()<64) str="0"+str;
				System.err.println("Error in Stream.setBit: cell="+str+" offset="+offset+" position%64="+(j%64));
				return false;
			}

			// Testing $binarySearch$ with 64-bit numbers
			for (int x=0; x<N_ELEMENTS; x++) numbers[x]=x;
			stream.clear(true);
			for (int x=0; x<N_ELEMENTS; x++) stream.push(numbers[x],64);
			for (int x=0; x<N_ELEMENTS/2; x++) {
				int p = random.nextInt(N_ELEMENTS);
				int value = numbers[p];
				long returnedP = stream.binarySearch(0,N_ELEMENTS,value,64,6);
				if (returnedP/64!=p) {
					System.err.println("Error in Stream.binarySearch: returnedPosition="+(returnedP/64)+" real position="+p);
					return false;
				}
			}

			/*System.out.print("Testing $skip$... ");
			k=(int)(Math.random()*N_ELEMENTS/4);
			j=(int)(Math.random()*(N_ELEMENTS-k))+1;
			nBits=0;
			for (i=1; i<j; i++) nBits+=Utils.bitsToEncode(i);
			stack.setPosition(nBits);
			if (stack.getPosition()!=nBits) { System.err.println("ERROR!"); System.exit(1); }
			for (i=j; i<j+k; i++) {
				stack.skip(Utils.bitsToEncode(i));
				nBits+=Utils.bitsToEncode(i);
			}
			if (stack.getPosition()!=nBits) { System.err.println("ERROR!"); System.exit(1); }*/

		}
		return true;
	}


	private static final boolean test_huffmanWaveletTree() {
		final int STRING_LENGTH = 10000;
		final int N_ITERATIONS = 100;
		final int N_POSITIONS = 1000;
		int i, j, c, cPrime, p, pPrime, maxCodeLength;
		int[] fullAlphabet = new int[] {0,1,2,3};
		int[] alphabet = new int[] {0,2,3};
		IntArray counts = new IntArray(alphabet.length,Utils.log2(STRING_LENGTH),true);
		long[] ones;
		long[][] stack, output, trueOutput;
		IntArray string;
		HuffmanWaveletTree tree;
		XorShiftStarRandom random = new XorShiftStarRandom();

		string = new IntArray(STRING_LENGTH,2,false);
		// Test string: 03300 20032
		//string.push(0); string.push(3); string.push(3); string.push(0); string.push(0);
		//string.push(2); string.push(0); string.push(0); string.push(3); string.push(2);
		//counts.setElementAt(0,5); counts.setElementAt(1,2); counts.setElementAt(2,3);
		stack = new long[fullAlphabet.length-1][1+N_POSITIONS];
		output = new long[fullAlphabet.length][N_POSITIONS];
		ones = new long[N_POSITIONS];
		trueOutput = new long[fullAlphabet.length][N_POSITIONS];
		for (i=0; i<N_ITERATIONS; i++) {
			string.clear();
			for (j=0; j<alphabet.length; j++) counts.setElementAt(j,0);
			for (j=0; j<STRING_LENGTH; j++) {
				c=random.nextInt(alphabet.length);
				counts.incrementElementAt(c);
				string.push(alphabet[c]);
			}
			tree = new HuffmanWaveletTree(string,alphabet,counts);

			// Testing $access$
			for (j=0; j<STRING_LENGTH; j++) {
				if (tree.access(j)!=string.getElementAt(j)) {
					System.err.println("The strings differ at position "+j+":");
					System.err.print("true string: "); string.print(); System.err.println();
					System.err.print("  wt string: ");
					for (int x=0; x<STRING_LENGTH; x++) System.out.print(tree.access(j)+"");
					System.err.println();
					return false;
				}
			}

			// Testing $multirank$
			maxCodeLength=tree.maxCodeLength;
			for (p=0; p<N_POSITIONS; p++) stack[0][1+p]=random.nextInt(STRING_LENGTH);
			for (int x=0; x<output.length; x++) {
				for (int y=0; y<output[x].length; y++) output[x][y]=0;
			}
			tree.multirank(fullAlphabet.length,N_POSITIONS,stack,output,ones);
			for (p=0; p<N_POSITIONS; p++) {
				for (c=0; c<fullAlphabet.length; c++) trueOutput[c][p]=0;
				for (j=0; j<stack[0][1+p]; j++) trueOutput[string.getElementAt(j)][p]++;
			}
			for (c=0; c<fullAlphabet.length; c++) {
				for (p=0; p<N_POSITIONS; p++) {
					if (output[c][p]!=trueOutput[c][p]) {
						System.err.println("Error in multirank, character="+c+" position="+stack[0][1+p]+": (pos,true,estimated)");
						for (cPrime=0; cPrime<fullAlphabet.length; cPrime++) {
							System.out.print("char "+cPrime+": ");
							for (pPrime=0; pPrime<N_POSITIONS; pPrime++) System.out.print("("+stack[0][1+pPrime]+","+trueOutput[cPrime][pPrime]+","+output[cPrime][pPrime]+") ");
							System.out.println();
						}
						return false;
					}
				}
			}
		}
		return true;
	}


	private static final boolean test_blockwiseBWT() {
		final int STRING_LENGTH = 10;
		final int N_ITERATIONS = 100000;
		final int BLOCK_SIZE = 2;
		final int N_THREADS = 2;
		int N_BLOCKS = Utils.divideAndRoundUp(STRING_LENGTH,BLOCK_SIZE);
		if (N_BLOCKS<4) N_BLOCKS=4;
		//System.err.println("N_BLOCKS="+N_BLOCKS);
		int i, j, dollarPosition1, dollarPosition2, suffix;
		int[] alphabet = new int[] {0,1,2,3};
		long[] dollar;
		IntArray string, bwt, suffixes, blockStarts, bitVector;
		XorShiftStarRandom random = new XorShiftStarRandom();
		Rank9 blockBoundaries;
		HuffmanWaveletTree[] waveletTrees;
		IntArray[] localBlockCounts;
		suffixes = new IntArray(STRING_LENGTH,Utils.log2(STRING_LENGTH),false);
		for (i=0; i<STRING_LENGTH; i++) suffixes.push(i);
		Constants constants = new Constants();

		for (i=0; i<N_ITERATIONS; i++) {
			string = new IntArray(STRING_LENGTH,2,false);
			for (j=0; j<STRING_LENGTH; j++) string.push(random.nextInt(4));
			bwt = new IntArray(STRING_LENGTH+1,2,true);

			// Checking $SortBWTBlockThread$
			dollarPosition1=Suffixes.blockwiseBWT(string,alphabet,4,2,BLOCK_SIZE,bwt,null,null,null,null,null,constants);
			Suffixes.sort(suffixes,string,random,constants);
			for (j=0; j<STRING_LENGTH; j++) {
				suffix=suffixes.getElementAt(j);
				if (suffix==0) {
					if (dollarPosition1!=j+1) {
						System.err.println("Error in dollar position="+dollarPosition1+" (computed|correct)");
						printBWTandSuffixes(bwt,dollarPosition1,suffixes,string);
						return false;
					}
				}
				else if (string.getElementAt(suffix-1)!=bwt.getElementAt(j+1)) {
					System.err.println("Error in BWT characters: (computed|correct)");
					printBWTandSuffixes(bwt,dollarPosition1,suffixes,string);
					return false;
				}
			}

			// Checking $WaveletBWTBlockThread$
			bitVector = new IntArray(STRING_LENGTH+1,1,true);
			waveletTrees = new HuffmanWaveletTree[N_BLOCKS];
			blockStarts = new IntArray(N_BLOCKS,Utils.log2(STRING_LENGTH),true);
			dollar = new long[3];
			localBlockCounts = new IntArray[N_BLOCKS];
			dollarPosition2=Suffixes.blockwiseBWT(string,alphabet,4,2,BLOCK_SIZE,null,waveletTrees,blockStarts,bitVector,localBlockCounts,dollar,constants);
			blockBoundaries = new Rank9(bitVector);
			boolean isOK = true;
			for (int x=0; x<STRING_LENGTH+1; x++) {
				if (x<dollarPosition2) {
					int y = (int)blockBoundaries.rank(x+1)-1;
					int z = waveletTrees[y].access(x-blockStarts.getElementAt(y));
					if (z!=bwt.getElementAt(x)) {
						System.err.println("Error 1: wavelet tree BWT differs from real BWT: ");
						isOK=false;
						break;
					}
				}
				else if (x>dollarPosition2) {
					int y = (int)blockBoundaries.rank(x+1)-1;
					int z = waveletTrees[y].access((y==dollar[1]?x-1:x)-blockStarts.getElementAt(y));
					if (z!=bwt.getElementAt(x)) {
						System.err.println("Error 2: wavelet tree BWT differs from real BWT");
						isOK=false;
						break;
					}
				}
				else {
					int y = (int)blockBoundaries.rank(dollarPosition2+1)-1;
					if (dollarPosition1!=dollar[0] || y!=dollar[1] || dollarPosition1-blockStarts.getElementAt(y)!=dollar[2]) {
						System.err.println("Error 3: dollar position in the wavelet tree BWT differs from the real dollar position");
						System.err.println("dollar[0]="+dollar[0]+" dollar[1]="+dollar[1]+" dollar[2]="+dollar[2]);
						isOK=false;
						break;
					}
				}
			}
			if (!isOK) {
				System.out.println("Real BWT: (dollar="+dollarPosition1+")");
				for (int l=0; l<STRING_LENGTH+1; l++) System.out.print(l==dollarPosition1?"$":bwt.getElementAt(l));
				System.out.println();
				System.out.println("Wavelet tree: (dollar="+dollarPosition2+")");
				for (int l=0; l<STRING_LENGTH+1; l++) {
					if (l==dollarPosition2) System.out.print("$");
					else if (l<dollarPosition2) {
						int b = (int)blockBoundaries.rank(l+1)-1;
						System.out.print(waveletTrees[b].access(l-blockStarts.getElementAt(b)));
					}
					else {
						int b = (int)blockBoundaries.rank(l+1)-1;
						System.out.print(waveletTrees[b].access((b==dollar[1]?l-1:l)-blockStarts.getElementAt(b)));
					}
				}
				System.out.println();
				return false;
			}
			if (blockStarts.length()!=blockBoundaries.numOnes) {
				System.out.println("Error: blockStarts has "+blockStarts.length()+" entries, but bitVector has "+blockBoundaries.numOnes+" ones");
				System.out.print("blockStarts: "); blockStarts.print(); System.out.println();
				System.out.print("bitVector: "); bitVector.printBits(); System.out.println();
				return false;
			}
			for (int x=1; x<blockStarts.length(); x++) {
				if (blockStarts.getElementAt(x)<blockStarts.getElementAt(x-1)) {
					System.out.println("Error in blockStarts: not monotonically increasing"); blockStarts.print(); System.out.println();
					return false;
				}
			}
			for (int x=0; x<blockStarts.length(); x++) {
				if (blockStarts.getElementAt(x)>STRING_LENGTH) {
					System.out.println("Error: blockStarts contains a value >= the length of the BWT");
					System.out.print("blockStarts: "); blockStarts.print(); System.out.println();
					System.out.print("bitVector: "); bitVector.printBits(); System.out.println();
					return false;
				}
				if (blockBoundaries.rank(blockStarts.getElementAt(x)+1)!=x+1) {
					System.out.println("Error: bits in blockStarts and in bitVector not aligned");
					System.out.print("blockStarts: "); blockStarts.print(); System.out.println();
					System.out.print("bitVector: "); bitVector.printBits(); System.out.println();
					return false;
				}
			}
			int nNulls = 0;
			for (int x=0; x<blockStarts.length(); x++) {
				if (waveletTrees[x]==null) nNulls++;
			}
			if (nNulls>1) {
				System.out.println("Error: waveletTrees contains "+nNulls+" null elements");
				return false;
			}
			nNulls=0;
			for (int x=0; x<blockStarts.length(); x++) {
				if (localBlockCounts[x]==null) nNulls++;
			}
			if (nNulls>1) {
				System.out.println("Error: localBlockCounts contains "+nNulls+" null elements");
				return false;
			}
			for (int x=0; x<blockStarts.length(); x++) {
				int y=blockStarts.getElementAt(x);
				int[] counts = new int[4];
				while (y<bwt.length() && (int)blockBoundaries.rank(y+1)-1==x) {
					if (y!=dollarPosition1) counts[bwt.getElementAt(y)]++;
					y++;
				}
				for (y=0; y<4; y++) {
					if (counts[y]!=localBlockCounts[x].getElementAt(y)) {
						System.out.println("Error: localBlockCounts["+x+"] contains wrong counts:");
						System.out.print("localBlockCounts["+x+"]: "); localBlockCounts[x].print(); System.out.println();
						System.out.print("true count: ");
						for (int z=0; z<4; z++) System.out.print(counts[z]+" ");
						System.out.println();
						return false;
					}
				}
			}
		}
		return true;
	}


	private static final void printBWTandSuffixes(IntArray bwt, int dollarPosition, IntArray suffixes, IntArray string) {
		int i, suffix;
		String bwtString, correctString;
		System.err.println("0: "+bwt.getElementAt(0)+" "+string.getElementAt(string.length()-1)+" "+(bwt.getElementAt(0)==string.getElementAt(string.length()-1)?"":"<<<"));
		for (i=1; i<bwt.length(); i++) {
			suffix=suffixes.getElementAt(i-1);
			bwtString=i==dollarPosition?"$":bwt.getElementAt(i)+"";
			correctString=suffix==0?"$":string.getElementAt(suffix-1)+"";
			System.err.println(i+": "+bwtString+" "+correctString+" "+(bwtString.equalsIgnoreCase(correctString)?"":"<<<"));
		}
	}


	private static final boolean test_pasteAtPointer() {
		final int STRING_LENGTH = 10000;
		final int N_ITERATIONS = 1000;
		final int MAX_BLOCK_LENGTH = 100;
		int i, j, value, position, blockLength;
		int[] array, blockArray;
		IntArray string, block;
		XorShiftStarRandom random = new XorShiftStarRandom();

		// Building a new string
		string = new IntArray(STRING_LENGTH,2,false);
		array = new int[STRING_LENGTH];
		block = new IntArray(10*MAX_BLOCK_LENGTH,2,false);
		blockArray = new int[10*MAX_BLOCK_LENGTH];
		for (i=0; i<STRING_LENGTH; i++) {
			value=random.nextInt(4);
			string.push(value);
			array[i]=value;
		}

		// Testing
		for (i=0; i<N_ITERATIONS; i++) {
			blockLength=1+random.nextInt(MAX_BLOCK_LENGTH-1);
			position=random.nextInt(STRING_LENGTH-blockLength+1);
			string.setPointer(position);
			block.clear(string.pointerOffset);
			for (j=0; j<blockLength; j++) {
				value=random.nextInt(4);
				block.push(value);
				blockArray[j]=value;
			}



/*			System.err.println("Block to paste at position "+position+":");
			for (j=0; j<blockLength; j++) System.err.print(blockArray[j]);
			System.err.println();
			System.err.println("Before pasteAtPointer:");
			for (j=0; j<STRING_LENGTH; j++) System.err.print(string.getElementAt(j));
			System.err.println();
*/


			string.pasteAtPointer(block);


/*			System.err.println("After pasteAtPointer:");
			for (j=0; j<STRING_LENGTH; j++) System.err.print(string.getElementAt(j));
			System.err.println();
*/

			for (j=0; j<STRING_LENGTH; j++) {
				if ((j<position||j>=position+blockLength) && string.getElementAt(j)!=array[j]) return false;
				if (j>=position && j<position+blockLength && string.getElementAt(j)!=blockArray[j-position]) return false;
			}
			for (j=0; j<blockLength; j++) array[position+j]=blockArray[j];
		}
		return true;
	}


	private static final boolean test_assignSuffixToBlock() {
		final int STRING_LENGTH = 10000;
		final int N_ITERATIONS = 100;
		final int N_SPLITTERS = 100;
		boolean lowOK, highOK, inInterval;
		int i, j, h, suffix, block;
		IntArray string, splitters, cache;
		XorShiftStarRandom random = new XorShiftStarRandom();
		Constants constants = new Constants();

		// Building a new string
		string = new IntArray(STRING_LENGTH,2,false);
		for (i=0; i<STRING_LENGTH; i++) string.push(random.nextInt(4));
		splitters = new IntArray(N_SPLITTERS,Utils.log2(STRING_LENGTH),false);
		cache=null;

		// Testing
		for (i=0; i<N_ITERATIONS; i++) {
			splitters.clear();
			for (j=0; j<N_SPLITTERS; j++) splitters.push(random.nextInt(STRING_LENGTH));
			Suffixes.sort(splitters,string,random,constants);
			if (cache!=null) cache.clear(); cache=Suffixes.buildBinarySearchCache(splitters,string);
			for (suffix=0; suffix<STRING_LENGTH; suffix++) {
				if (splitters.linearSearch(suffix)>=0) continue;
				block=Suffixes.assignSuffixToBlock(suffix,splitters,cache,string);
				if (block==0) inInterval=(string.lcp(splitters.getElementAt(0),suffix,true)&Utils.MSB_INT_ONE)!=0x00000000;
				else if (block==N_SPLITTERS) inInterval=(string.lcp(suffix,splitters.getElementAt(N_SPLITTERS-1),true)&Utils.MSB_INT_ONE)!=0x00000000;
				else {
					lowOK=(string.lcp(suffix,splitters.getElementAt(block-1),true)&Utils.MSB_INT_ONE)!=0x00000000;
					highOK=(string.lcp(splitters.getElementAt(block),suffix,true)&Utils.MSB_INT_ONE)!=0x00000000;
					inInterval=lowOK&&highOK;
				}
				if (!inInterval) {
					System.err.println("Error: string["+suffix+"] does not belong to block ("+
									    (block==0?"\\infty":"string["+splitters.getElementAt(block-1)+",]")+
									    ", "+
									    (block==N_SPLITTERS?"\\infty":"string["+splitters.getElementAt(block)+",]")+
									    "), but the algorithm thinks so.");
					if (block>0) {
						System.err.println("string["+splitters.getElementAt(block-1)+",]:");
						for (h=splitters.getElementAt(block-1); h<STRING_LENGTH; h++) System.err.print(string.getElementAt(h));
						System.err.println();
					}
					System.err.println("string["+suffix+",]:");
					for (h=suffix; h<STRING_LENGTH; h++) System.err.print(string.getElementAt(h));
					System.err.println();
					if (block<N_SPLITTERS) {
						System.err.println("string["+splitters.getElementAt(block)+",]:");
						for (h=splitters.getElementAt(block); h<STRING_LENGTH; h++) System.err.print(string.getElementAt(h));
						System.err.println();
					}
					return false;
				}
			}
		}
		return true;
	}


	private static final boolean test_buildBinarySearchCache() {
		final int STRING_LENGTH = 10000;
		final int N_ITERATIONS = 10;
		final int N_SPLITTERS = 1000;
		boolean lowOK, highOK, inInterval;
		int i, j, h, suffix, block;
		IntArray string, splitters, cache;
		XorShiftStarRandom random = new XorShiftStarRandom();
		Constants constants = new Constants();

		// Building a new string
		string = new IntArray(STRING_LENGTH,2,false);
		for (i=0; i<STRING_LENGTH; i++) string.push(random.nextInt(4));
		splitters = new IntArray(N_SPLITTERS,Utils.log2(STRING_LENGTH),false);
		cache=null;

		// Testing
		for (i=0; i<N_ITERATIONS; i++) {
			splitters.clear();
			for (j=0; j<N_SPLITTERS; j++) splitters.push(random.nextInt(STRING_LENGTH));
			Suffixes.sort(splitters,string,random,constants);
			if (cache!=null) cache.clear();
			cache=Suffixes.buildBinarySearchCache(splitters,string);
			if (cache.length()!=2*(splitters.length()-2)) {
				System.err.println("Length error");
				return false;
			}
			if (!checkBinarySearchCache(0,N_SPLITTERS-1,splitters,cache,string)) return false;
		}
		return true;
	}


	private static final boolean checkBinarySearchCache(int left, int right, IntArray splitters, IntArray cache, IntArray string) {
		int mid = (left+right)>>1;
		int midLeftLCP = string.lcp(splitters.getElementAt(mid),splitters.getElementAt(left),false);
		if (cache.getElementAt(2*(mid-1))!=midLeftLCP) return false;
		int midRightLCP = string.lcp(splitters.getElementAt(mid),splitters.getElementAt(right),false);
		if (cache.getElementAt(2*(mid-1)+1)!=midRightLCP) return false;
		boolean out = true;
		if (mid-left>1) out&=checkBinarySearchCache(left,mid,splitters,cache,string);
		if (right-mid>1) out&=checkBinarySearchCache(mid,right,splitters,cache,string);
		return out;
	}


	private static final boolean test_intervalOfSuffixes() {
		int STRING_LENGTH = 10;
		final int N_INTERVALS = 10;
		boolean inInterval, inOut;
		int i, j, h, low, high;
		IntArray string, lcpLow, lcpHigh, out;
		XorShiftStarRandom random = new XorShiftStarRandom();
		Constants constants = new Constants();

		// Building a new string
		string = new IntArray(STRING_LENGTH,2,false);
		for (i=0; i<STRING_LENGTH; i++) string.push(random.nextInt(4));
		lcpLow = new IntArray(constants.DISTINGUISHING_PREFIX+1,Utils.log2(STRING_LENGTH<<1),false);
		lcpHigh = new IntArray(constants.DISTINGUISHING_PREFIX+1,Utils.log2(STRING_LENGTH<<1),false);
		out = new IntArray(STRING_LENGTH-2,Utils.log2(STRING_LENGTH),false);

		// Testing
		for (i=0; i<N_INTERVALS; i++) {
			low=random.nextInt(STRING_LENGTH);
			do { high=random.nextInt(STRING_LENGTH); } while (high==low);
			lcpLow.clear(); Suffixes.buildLCPArray(low,string,lcpLow,constants);
			lcpHigh.clear(); Suffixes.buildLCPArray(high,string,lcpHigh,constants);

			out.clear(); Suffixes.intervalOfSuffixes(low,high,lcpLow,lcpHigh,string,out);
			if (!checkSuffixesInOut(low,high,out,string)) return false;
			out.clear(); Suffixes.intervalOfSuffixes(-1,low,null,lcpLow,string,out);
			if (!checkSuffixesInOut(-1,low,out,string)) return false;
			out.clear(); Suffixes.intervalOfSuffixes(high,-1,lcpHigh,null,string,out);
			if (!checkSuffixesInOut(high,-1,out,string)) return false;
		}
		return true;
	}


	private static final boolean checkSuffixesInOut(int low, int high, IntArray out, IntArray string) {
		final int STRING_LENGTH = string.length();
		boolean lowOK, highOK, inInterval, inOut;
		int j, h;

		for (j=0; j<STRING_LENGTH; j++) {
			lowOK=low<0?true:(string.lcp(j,low,true)&Utils.MSB_INT_ONE)!=0x00000000;
			highOK=high<0?true:(string.lcp(high,j,true)&Utils.MSB_INT_ONE)!=0x00000000;
			inInterval=lowOK&&highOK;
			inOut=out.linearSearch(j)>=0;
			if (inInterval&&!inOut) {
				System.err.println("Error: string["+low+"]<string["+j+",]<string["+high+",], but "+j+" does not appear in the output");
				if (low>=0) {
					System.err.println("string["+low+",]:");
					for (h=low; h<STRING_LENGTH; h++) System.err.print(string.getElementAt(h));
					System.err.println();
				}
				System.err.println("string["+j+",]:");
				for (h=j; h<STRING_LENGTH; h++) System.err.print(string.getElementAt(h));
				System.err.println();
				if (high>=0) {
					System.err.println("string["+high+",]:");
					for (h=high; h<STRING_LENGTH; h++) System.err.print(string.getElementAt(h));
					System.err.println();
				}
				return false;
			}
			else if (!inInterval&&inOut) {
				System.err.println("Error: string["+j+",] does not belong to interval (string["+low+",],string["+high+",]), but "+j+" appears in the output");
				if (low>=0) {
					System.err.println("string["+low+",]:");
					for (h=low; h<STRING_LENGTH; h++) System.err.print(string.getElementAt(h));
					System.err.println();
				}
				System.err.println("string["+j+",]:");
				for (h=j; h<STRING_LENGTH; h++) System.err.print(string.getElementAt(h));
				System.err.println();
				if (high>=0) {
					System.err.println("string["+high+",]:");
					for (h=high; h<STRING_LENGTH; h++) System.err.print(string.getElementAt(h));
					System.err.println();
				}
				return false;
			}
		}
		return true;
	}



	private static final boolean test_buildLCPArray() {
		int STRING_LENGTH = 16;  // These values came from a failure with string 1^{9}2 and similar, now fixed. Works for larger values, as well.
		final int N_SUFFIXES = 10;
		int i, j, k, h, tmp, length, suffix, sign, predictedSign, predictedLCP, lcp, lcpArray;
		IntArray string, out;
		XorShiftStarRandom random = new XorShiftStarRandom();
		Constants constants = new Constants();

		// Building a new string
		string = new IntArray(STRING_LENGTH,2,false);
		for (j=0; j<STRING_LENGTH; j++) string.push(random.nextInt(4));
		out = new IntArray(STRING_LENGTH,Utils.log2(STRING_LENGTH<<1),false);
		final int SELECT_SIGN = Utils.MSB_INT_ONE>>>(32-out.bitsPerInt);
		final int SELECT_LENGTH = 0xFFFFFFFF>>>(32-out.bitsPerInt+1);

		// Testing LCP values
		for (i=0; i<N_SUFFIXES; i++) {
			suffix=random.nextInt(STRING_LENGTH-constants.DISTINGUISHING_PREFIX+1);
			out.clear();
			Suffixes.buildLCPArray(suffix,string,out,constants);
			for (j=1; j<=constants.DISTINGUISHING_PREFIX; j++) {
				lcp=string.lcp(suffix,suffix+j,true);
				length=lcp&Utils.MSB_INT_ZERO;
				sign=lcp&Utils.MSB_INT_ONE;
				lcpArray=out.getElementAt(j);
				if ((lcpArray&SELECT_LENGTH)!=length) {
					System.out.println("Length error: lcp="+Integer.toBinaryString(lcp)+" lcpArray="+Integer.toBinaryString(lcpArray));
					return false;
				}
				if (((lcpArray&SELECT_SIGN)==0&&sign!=0) || ((lcpArray&SELECT_SIGN)!=0&&sign==0)) {
					System.out.println("Sign error: lcp="+Integer.toBinaryString(lcp)+" lcpArray="+Integer.toBinaryString(lcpArray)+" suffix="+suffix+" j="+j);
					for (h=suffix; h<STRING_LENGTH; h++) System.out.print(string.getElementAt(h));
					System.out.println();
					return false;
				}
			}
		}
		return true;
	}


	private static final boolean test_heapSort() {
		final int N_ITERATIONS = 100;
		final int ARRAY_LENGTH = 10000;
		final int MAX_VALUE = 1000;
		int i, j, k;
		IntArray intArray;
		int[] array;
		XorShiftStarRandom random = new XorShiftStarRandom();

		for (i=0; i<N_ITERATIONS; i++) {
			intArray = new IntArray(ARRAY_LENGTH,Utils.log2(MAX_VALUE),false);
			array = new int[ARRAY_LENGTH];
			for (j=0; j<ARRAY_LENGTH; j++) {
				k=random.nextInt(MAX_VALUE);
				intArray.push(k);
				array[j]=k;
			}
			intArray.heapSort(0,ARRAY_LENGTH);
			Arrays.sort(array);
			for (j=0; j<ARRAY_LENGTH; j++) {
				if (intArray.getElementAt(j)!=array[j]) return false;
			}
		}
		return true;
	}


	private static final boolean test_swap() {
		final int N_RANDOM_STRINGS = 10;
		final int STRING_LENGTH = 100;
		final int N_RANDOM_SWAPS = 100;
		int i, j, tmp, from, to;
		IntArray string;
		XorShiftStarRandom random = new XorShiftStarRandom();

		for (i=0; i<N_RANDOM_STRINGS; i++) {
			// Building a new string
			string = new IntArray(STRING_LENGTH,2,false);
			int[] stringArray = new int[STRING_LENGTH];
			for (j=0; j<STRING_LENGTH; j++) {
				tmp=random.nextInt(4);
				string.push(tmp);
				stringArray[j]=tmp;
			}

			// Random swaps
			for (j=0; j<N_RANDOM_SWAPS; j++) {
				from=random.nextInt(STRING_LENGTH);
				do { to=random.nextInt(STRING_LENGTH); } while (to!=from);
				string.swap(from,to);
				tmp=stringArray[from]; stringArray[from]=stringArray[to]; stringArray[to]=tmp;
			}

			// Testing equivalence
			for (j=0; j<STRING_LENGTH; j++) {
				if (string.getElementAt(j)!=stringArray[j]) return false;
			}
		}
		return true;
	}


	private static final boolean test_vecswap() {
		final int N_RANDOM_STRINGS = 10;
		final int STRING_LENGTH = 100;
		final int N_RANDOM_SWAPS = 100;
		int i, j, k, n, tmp, from1, from2;
		IntArray string;
		XorShiftStarRandom random = new XorShiftStarRandom();

		for (i=0; i<N_RANDOM_STRINGS; i++) {
			// Building a new string
			string = new IntArray(STRING_LENGTH,2,false);
			int[] stringArray = new int[STRING_LENGTH];
			for (j=0; j<STRING_LENGTH; j++) {
				tmp=random.nextInt(4);
				string.push(tmp);
				stringArray[j]=tmp;
			}

			// Random swaps
			for (j=0; j<N_RANDOM_SWAPS; j++) {
				n=random.nextInt(STRING_LENGTH/4);
				do { from1=random.nextInt(STRING_LENGTH); } while (from1>STRING_LENGTH-n);
				do { from2=random.nextInt(STRING_LENGTH); } while ((from2>=from1 && from2<from1+n) || (from1>=from2 && from1<from2+n) || (from2>STRING_LENGTH-n));
				string.vecswap(from1,from2,n);
				int[] vector = new int[n];
				for (k=0; k<n; k++) vector[k]=stringArray[from1+k];
				for (k=0; k<n; k++) stringArray[from1+k]=stringArray[from2+k];
				for (k=0; k<n; k++) stringArray[from2+k]=vector[k];
			}

			// Testing equivalence
			for (j=0; j<STRING_LENGTH; j++) {
				if (string.getElementAt(j)!=stringArray[j]) return false;
			}
		}
		return true;
	}


	private static final boolean test_incrementElementAt() {
		final int N_RANDOM_ARRAYS = 100;
		final int N_RANDOM_INCREMENTS = 100000;
		final int ARRAY_LENGTH = 100;
		final int MAX_VALUE = 100;
		int i, j, k, tmp;
		IntArray array;
		XorShiftStarRandom random = new XorShiftStarRandom();

		for (i=0; i<N_RANDOM_ARRAYS; i++) {
			// Building a new array
			array = new IntArray(ARRAY_LENGTH,32,false);
			int[] realArray = new int[ARRAY_LENGTH];
			for (j=0; j<ARRAY_LENGTH; j++) {
				tmp=random.nextInt(MAX_VALUE);
				array.push(tmp);
				realArray[j]=tmp;
			}

			// Random increments
			for (j=0; j<N_RANDOM_INCREMENTS; j++) {
				k=random.nextInt(ARRAY_LENGTH);
				array.incrementElementAt(k);
				realArray[k]++;
			}

			// Testing equivalence
			for (j=0; j<ARRAY_LENGTH; j++) {
				if (array.getElementAt(j)!=realArray[j]) return false;
			}
		}
		return true;
	}


	private static final boolean test_pushFromRight() {
		final int N_RANDOM_ARRAYS = 1000;
		final int ARRAY_LENGTH = 10000;
		final int MAX_VALUE = 100;
		int i, j, k, tmp;
		IntArray array;
		XorShiftStarRandom random = new XorShiftStarRandom();

		for (i=0; i<N_RANDOM_ARRAYS; i++) {
			// Building a new array
			array = new IntArray(ARRAY_LENGTH,16,false);
			int[] realArray = new int[ARRAY_LENGTH];
			for (j=0; j<ARRAY_LENGTH; j++) {
				tmp=random.nextInt(MAX_VALUE);
				array.pushFromRight(tmp);
				realArray[j]=tmp;
			}

			// Testing equivalence
			for (j=0; j<ARRAY_LENGTH; j+=4) {
				for (k=0; k<4; k++) {
					if (array.getElementAt(j+k)!=realArray[j+3-k]) return false;
				}
			}
		}
		return true;
	}


	private static final boolean test_rank9() {
		final int N_RANDOM_ARRAYS = 1000;
		final int ARRAY_LENGTH = 10000;
		int i, j, tmp;
		IntArray array;
		XorShiftStarRandom random = new XorShiftStarRandom();

		for (i=0; i<N_RANDOM_ARRAYS; i++) {
			// Building a new array
			array = new IntArray(ARRAY_LENGTH,1,false);
			int[] ranks = new int[ARRAY_LENGTH];
			for (j=0; j<ARRAY_LENGTH; j++) {
				tmp=random.nextInt(1);
				array.pushFromRight(tmp);
				if (j<ARRAY_LENGTH-1) ranks[j+1]=ranks[j]+(tmp==1?1:0);
			}
			Rank9 rankDS = new Rank9(array);

			// Testing equivalence
			for (j=0; j<ARRAY_LENGTH; j++) {
				if (rankDS.rank(j)!=ranks[j]) return false;
			}
		}
		return true;
	}


	private static final boolean test_sort() {
		final int N_RANDOM_STRINGS = 100;
		final int STRING_LENGTH = 1000;
		int i, j;
		IntArray string, suffixes_quicksort, suffixes_inssort, suffixes_heapsort;
		long[] cache_quicksort, cache_inssort, cache_heapsort;
		XorShiftStarRandom random = new XorShiftStarRandom();

		for (i=0; i<N_RANDOM_STRINGS; i++) {
			// Building a new string
			//System.out.println("-- -- -- -- --");
			string = new IntArray(STRING_LENGTH,2,false);
			for (j=0; j<STRING_LENGTH; j++) string.push(random.nextInt(4));
			// ccgt ataa aa
			//string.push(1); string.push(1); string.push(2); string.push(3);
			//string.push(0); string.push(3); string.push(0); string.push(0);
			//string.push(0); string.push(0);

			// quicksort
			suffixes_quicksort = new IntArray(STRING_LENGTH,Utils.log2(STRING_LENGTH));
			for (j=0; j<STRING_LENGTH; j++) suffixes_quicksort.push(j);
			cache_quicksort = new long[STRING_LENGTH];
			for (j=0; j<STRING_LENGTH; j++) cache_quicksort[j]=string.load63(j<<string.log2BitsPerInt);
			Suffixes.quicksort(suffixes_quicksort,0,STRING_LENGTH,0,0,string,cache_quicksort,    Integer.MAX_VALUE,1,random);  //Suffixes.QUICKSORT_HEAPSORT_SCALE*Utils.log2(STRING_LENGTH), Suffixes.STOP_QUICKSORT_AT_SIZE, random);

			// insertionSort
			suffixes_inssort = new IntArray(STRING_LENGTH,Utils.log2(STRING_LENGTH));
			for (j=0; j<STRING_LENGTH; j++) suffixes_inssort.push(j);
			cache_inssort = new long[STRING_LENGTH];
			for (j=0; j<STRING_LENGTH; j++) cache_inssort[j]=string.load63(j<<string.log2BitsPerInt);
			Suffixes.insertionSort(suffixes_inssort,0,STRING_LENGTH,string,cache_inssort);

			// heapSort
			suffixes_heapsort = new IntArray(STRING_LENGTH,Utils.log2(STRING_LENGTH));
			for (j=0; j<STRING_LENGTH; j++) suffixes_heapsort.push(j);
			cache_heapsort = new long[STRING_LENGTH];
			for (j=0; j<STRING_LENGTH; j++) cache_heapsort[j]=string.load63(j<<string.log2BitsPerInt);
			Suffixes.heapSort(suffixes_heapsort,0,STRING_LENGTH,string,cache_heapsort);

			// Testing equivalence
			for (j=0; j<STRING_LENGTH; j++) {
				if (suffixes_inssort.getElementAt(j)!=suffixes_heapsort.getElementAt(j)) {
					System.err.println("MISMATCH inssort-heapsort");
					System.err.println("inssort:");
					suffixes_inssort.printAsSuffixes(string);
					System.err.println("heapsort:");
					suffixes_heapsort.printAsSuffixes(string);
					return false;
				}
				if (suffixes_inssort.getElementAt(j)!=suffixes_quicksort.getElementAt(j)) {
					System.err.println("MISMATCH inssort-quicksort");
					System.err.println("inssort:");
					suffixes_inssort.printAsSuffixes(string);
					System.err.println("quicksort:");
					suffixes_quicksort.printAsSuffixes(string);
					return false;
				}
			}
		}
		return true;
	}

}