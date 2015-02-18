
import java.util.Arrays;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class Tests {

	public static void main(String[] args) {
/*
		// Testing $IntArray$
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

		// Testing $HuffmanWaveletTree$
		if (!test_huffmanWaveletTree()) {
			System.err.println("HuffmanWaveletTree \t\t\t [ FAILED ]");
			System.exit(1);
		}
		else System.out.println("HuffmanWaveletTree \t\t\t [   OK   ]");

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

	private static final boolean test_substringIterator() {
		final int STRING_LENGTH = 1000;
		final int N_ITERATIONS = 100;
		final String suffix = "133333";
		int i, j, k, c;
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
			for (j=0; j<STRING_LENGTH-suffix.length(); j++) {
				c=random.nextInt(3);
				stringString+=""+c;
				string.push(c);
			}
			stringString+=suffix;
			for (j=0; j<suffix.length(); j++) string.push(suffix.charAt(j));
System.err.println("TEXT:");
string.print();
			// Trivial enumeration of the distinct substrings
			trueSubstrings = new HashSet<String>();
/*			for (j=0; j<STRING_LENGTH; j++) {
				for (k=j+1; k<=STRING_LENGTH; k++) trueSubstrings.add(stringString.substring(j,k));
			}
*/			trueSubstringsArray = new String[trueSubstrings.size()];
/*			trueSubstrings.toArray(trueSubstringsArray);
			Arrays.sort(trueSubstringsArray);
			System.out.println("Trivial enumeration completed: "+trueSubstringsArray.length+" distinct strings");
*/			// Running $SubstringIterator$ with one thread
			iteratorSubstrings = new HashSet<String>();
			iterator = new SubstringIterator(string,alphabet,4,512,1,new TestSubstring(4,2,STRING_LENGTH,Utils.log2(STRING_LENGTH)));
			iterator.run(1);
			System.out.println("SubstringIterator enumeration completed");
			// Tests
			if (iteratorSubstrings.size()!=trueSubstrings.size()) {
				System.out.println("Error in SubstringIterator with one thread: wrong number of distinct substrings.");
				return false;
			}
			iteratorSubstringsArray = new String[iteratorSubstrings.size()];
			iteratorSubstrings.toArray(iteratorSubstringsArray);
			iteratorSubstrings.clear();
			Arrays.sort(iteratorSubstringsArray);
			for (i=0; i<iteratorSubstringsArray.length; i++) {
				if (Arrays.binarySearch(trueSubstringsArray,iteratorSubstringsArray[i])<0) {
					System.out.println("Error in SubstringIterator with one thread: the enumerated substring "+iteratorSubstringsArray[i]+" does not exist in the string.");
					return false;
				}
			}
			for (i=0; i<trueSubstringsArray.length; i++) {
				if (Arrays.binarySearch(iteratorSubstringsArray,trueSubstringsArray[i])<0) {
					System.out.println("Error in SubstringIterator with one thread: substring "+trueSubstringsArray[i]+" in the string has not been enumerated.");
					return false;
				}
			}
			// Testing $SubstringIterator$ with two threads
/*			iteratorSubstrings.clear();
			iterator = new SubstringIterator(string,alphabet,4,10*1024*1024,2,new TestSubstring(4,2,STRING_LENGTH,Utils.log2(STRING_LENGTH)));
			iterator.run(2);
			if (iteratorSubstrings.size()!=trueSubstrings.size()) {
				System.out.println("Error in SubstringIterator with two threads");
				return false;
			}
			iteratorSubstringsArray = new String[iteratorSubstrings.size()];
			iteratorSubstrings.toArray(iteratorSubstringsArray);
			iteratorSubstrings.clear();
			Arrays.sort(iteratorSubstringsArray);
			for (i=0; i<iteratorSubstringsArray.length; i++) {
				if (Arrays.binarySearch(trueSubstringsArray,iteratorSubstringsArray[i])<0) {
					System.out.println("Error in SubstringIterator with two threads: the enumerated substring "+iteratorSubstringsArray[i]+" does not exist in the string.");
					return false;
				}
			}
			for (i=0; i<trueSubstringsArray.length; i++) {
				if (Arrays.binarySearch(iteratorSubstringsArray,trueSubstringsArray[i])<0) {
					System.out.println("Error in SubstringIterator with two threads: substring "+trueSubstringsArray[i]+" in the string has not been enumerated.");
					return false;
				}
			}*/
		}
		return true;
	}


	private static class TestSubstring extends Substring {
		public TestSubstring(int alphabetLength, int log2alphabetLength, int textLength, int log2textLength) {
			super(alphabetLength,log2alphabetLength,textLength,log2textLength);
		}

		protected Substring getInstance() {
			return new TestSubstring(alphabetLength,log2alphabetLength,textLength,log2textLength);
		}

		protected void visited(Stream stack) {
			if (length==1000) System.err.println("the text: ("+bwtIntervals[0][0]+","+bwtIntervals[0][1]+")");
			IntArray sequence = new IntArray((int)length,log2alphabetLength,true);
			Substring.getSequence(this,stack,sequence);
			String str = "";
			for (int i=0; i<length; i++) str+=""+sequence.getElementAt(i);
			iteratorSubstrings.add(str);
			System.err.println("length="+length+" string="+str+" ("+bwtIntervals[0][0]+","+bwtIntervals[0][1]+")");
			if (length>textLength) {
				System.err.println("ERROR: SUBSTRING LONGER THAN TEXT");
				System.exit(1);
			}
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
			stack.clear(); stack.setPosition(0L);
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
			//
			// ... to be implemented ...
			//
		}
		return true;
	}


	private static final boolean test_stream() {
		final int N_ELEMENTS = 100000;
		final int N_ITERATIONS = 100;
		final int MAX_INT = 100;
		int i, j, k, t, r, b, position, region, cell, offset;
		int[] numbers = new int[N_ELEMENTS];
		long nBits, read;
		Stream stream = new Stream(8);
		XorShiftStarRandom random = new XorShiftStarRandom();

		for (t=0; t<N_ITERATIONS; t++) {
			// Testing $push$
			stream.clear();
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
		final int STRING_LENGTH = 1000;
		final int N_ITERATIONS = 100;
		final int N_POSITIONS = 100;
		int i, j, c, cPrime, p, pPrime, maxCodeLength;
		int[] alphabet = new int[] {0,1,2,3};
		IntArray counts = new IntArray(4,Utils.log2(STRING_LENGTH),true);
		long[] ones;
		long[][] stack, output, trueOutput;
		IntArray string;
		HuffmanWaveletTree tree;
		XorShiftStarRandom random = new XorShiftStarRandom();

		string = new IntArray(STRING_LENGTH,2,false);
		// Test string:
		//string.push(0); string.push(0); string.push(1); string.push(3); string.push(2); string.push(2); string.push(2); string.push(1); string.push(2); string.push(1); string.push(0); string.push(3); string.push(0); string.push(2); string.push(1); string.push(2); string.push(3); string.push(0); string.push(0); string.push(2);
		//counts.setElementAt(0,6); counts.setElementAt(1,4); counts.setElementAt(2,7); counts.setElementAt(3,3);
		stack = new long[3][1+N_POSITIONS];
		output = new long[4][N_POSITIONS];
		ones = new long[N_POSITIONS];
		trueOutput = new long[4][N_POSITIONS];
		for (i=0; i<N_ITERATIONS; i++) {
			string.clear();
			for (j=0; j<4; j++) counts.setElementAt(j,0);
			for (j=0; j<STRING_LENGTH; j++) {
				c=random.nextInt(4);
				counts.incrementElementAt(c);
				string.push(c);
			}
			tree = new HuffmanWaveletTree(string,alphabet,counts);
			maxCodeLength=tree.maxCodeLength;
			for (p=0; p<N_POSITIONS; p++) stack[0][1+p]=random.nextInt(STRING_LENGTH);
			tree.multirank(N_POSITIONS,stack,output,ones);
			for (p=0; p<N_POSITIONS; p++) {
				for (c=0; c<4; c++) trueOutput[c][p]=0;
				for (j=0; j<stack[0][1+p]; j++) trueOutput[string.getElementAt(j)][p]++;
			}
			for (c=0; c<4; c++) {
				for (p=0; p<N_POSITIONS; p++) {
					if (output[c][p]!=trueOutput[c][p]) {
						System.err.println("Error in multirank, character="+c+" position="+stack[0][1+p]+": (pos,true,estimated)");
						for (cPrime=0; cPrime<4; cPrime++) {
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
		final int STRING_LENGTH = 10000;
		final int N_ITERATIONS = 100;
		final int BLOCK_SIZE = 100;
		final String mySuffix = "133333";
		int i, j, dollarPosition, suffix;
		int[] alphabet = new int[] {0,1,2,3};
		IntArray string, bwt, suffixes;
		XorShiftStarRandom random = new XorShiftStarRandom();

		bwt = new IntArray(STRING_LENGTH,2,false);
		suffixes = new IntArray(STRING_LENGTH,Utils.log2(STRING_LENGTH),false);
		for (i=0; i<STRING_LENGTH; i++) suffixes.push(i);
		for (i=0; i<N_ITERATIONS; i++) {
			string = new IntArray(STRING_LENGTH,2,false);
			for (j=0; j<STRING_LENGTH-mySuffix.length(); j++) string.push(random.nextInt(4));
			for (j=0; j<mySuffix.length(); j++) string.push(mySuffix.charAt(j));
			bwt.clear();
			dollarPosition=Suffixes.blockwiseBWT(string,alphabet,4,2,BLOCK_SIZE,2,bwt,null,null,null,null,null);
			Suffixes.sort(suffixes,string,random);
			for (j=0; j<STRING_LENGTH; j++) {
				suffix=suffixes.getElementAt(j);
				if (suffix==0) {
					if (dollarPosition!=j) {
						System.err.println("Error in dollar position="+dollarPosition+" (computed|correct)");
						printBWTandSuffixes(bwt,dollarPosition,suffixes,string);
						return false;
					}
				}
				else if (string.getElementAt(suffix-1)!=bwt.getElementAt(j)) {
					System.err.println("Error in BWT characters: (computed|correct)");
					printBWTandSuffixes(bwt,dollarPosition,suffixes,string);
					return false;
				}
			}
		}
		return true;
	}


	private static final void printBWTandSuffixes(IntArray bwt, int dollarPosition, IntArray suffixes, IntArray string) {
		int i, suffix;
		String bwtString, correctString;
		for (i=0; i<string.length(); i++) {
			suffix=suffixes.getElementAt(i);
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

		// Building a new string
		string = new IntArray(STRING_LENGTH,2,false);
		for (i=0; i<STRING_LENGTH; i++) string.push(random.nextInt(4));
		splitters = new IntArray(N_SPLITTERS,Utils.log2(STRING_LENGTH),false);
		cache=null;

		// Testing
		for (i=0; i<N_ITERATIONS; i++) {
			splitters.clear();
			for (j=0; j<N_SPLITTERS; j++) splitters.push(random.nextInt(STRING_LENGTH));
			Suffixes.sort(splitters,string,random);
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

		// Building a new string
		string = new IntArray(STRING_LENGTH,2,false);
		for (i=0; i<STRING_LENGTH; i++) string.push(random.nextInt(4));
		splitters = new IntArray(N_SPLITTERS,Utils.log2(STRING_LENGTH),false);
		cache=null;

		// Testing
		for (i=0; i<N_ITERATIONS; i++) {
			splitters.clear();
			for (j=0; j<N_SPLITTERS; j++) splitters.push(random.nextInt(STRING_LENGTH));
			Suffixes.sort(splitters,string,random);
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
		int STRING_LENGTH = 10000;
		final int N_INTERVALS = 100;
		boolean inInterval, inOut;
		int i, j, h, low, high;
		IntArray string, lcpLow, lcpHigh, out;
		XorShiftStarRandom random = new XorShiftStarRandom();

		// Building a new string
		string = new IntArray(STRING_LENGTH,2,false);
		for (i=0; i<STRING_LENGTH; i++) string.push(random.nextInt(4));
		lcpLow = new IntArray(Suffixes.DISTINGUISHING_PREFIX+1,Utils.log2(STRING_LENGTH),false);
		lcpHigh = new IntArray(Suffixes.DISTINGUISHING_PREFIX+1,Utils.log2(STRING_LENGTH),false);
		out = new IntArray(STRING_LENGTH-2,Utils.log2(STRING_LENGTH),false);

		// Testing
		for (i=0; i<N_INTERVALS; i++) {
			low=random.nextInt(STRING_LENGTH);
			do { high=random.nextInt(STRING_LENGTH); } while ((string.lcp(high,low,true)&Utils.MSB_INT_ONE)==0x00000000);
			lcpLow.clear(); Suffixes.buildLCPArray(low,string,lcpLow);
			lcpHigh.clear(); Suffixes.buildLCPArray(high,string,lcpHigh);

			out.clear(); Suffixes.intervalOfSuffixes(low,high,lcpLow,lcpHigh,string,out);
			if (!checkSuffixesInOut(low,high,out,string)) return false;
			out.clear(); Suffixes.intervalOfSuffixes(-1,high,lcpLow,lcpHigh,string,out);
			if (!checkSuffixesInOut(-1,high,out,string)) return false;
			out.clear(); Suffixes.intervalOfSuffixes(low,-1,lcpLow,lcpHigh,string,out);
			if (!checkSuffixesInOut(low,-1,out,string)) return false;
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
		int STRING_LENGTH = 10000;
		final int N_SUFFIXES = 100;
		int i, j, k, h, tmp, length, suffix, sign, predictedSign, predictedLCP, lcp, lcpArray;
		IntArray string, out;
		XorShiftStarRandom random = new XorShiftStarRandom();

		// Building a new string
		string = new IntArray(STRING_LENGTH,2,false);
		for (j=0; j<STRING_LENGTH; j++) string.push(random.nextInt(4));
		out = new IntArray(STRING_LENGTH,Utils.log2(STRING_LENGTH),false);
		final int SELECT_SIGN = Utils.MSB_INT_ONE>>>(32-out.bitsPerInt);
		final int SELECT_LENGTH = 0xFFFFFFFF>>>(32-out.bitsPerInt+1);

		// Testing LCP values
		for (i=0; i<N_SUFFIXES; i++) {
			suffix=random.nextInt(STRING_LENGTH-Suffixes.DISTINGUISHING_PREFIX+1);
			out.clear();
			Suffixes.buildLCPArray(suffix,string,out);
			for (j=1; j<=Suffixes.DISTINGUISHING_PREFIX; j++) {
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


	private static final boolean test_sort() {
		final int N_RANDOM_STRINGS = 10;
		final int STRING_LENGTH = 10000;
		int i, j;
		IntArray string, suffixes_quicksort, suffixes_inssort, suffixes_heapsort;
		long[] cache_quicksort, cache_inssort, cache_heapsort;
		XorShiftStarRandom random = new XorShiftStarRandom();

		for (i=0; i<N_RANDOM_STRINGS; i++) {
			// Building a new string
			string = new IntArray(STRING_LENGTH,2,false);
			for (j=0; j<STRING_LENGTH; j++) string.push(random.nextInt(4));
			// cggt acgc tc
			//string.push(1); string.push(2); string.push(2); string.push(3);
			//string.push(0); string.push(1); string.push(2); string.push(1);
			//string.push(3); string.push(1);

			// quicksort
			suffixes_quicksort = new IntArray(STRING_LENGTH,Utils.log2(STRING_LENGTH));
			for (j=0; j<STRING_LENGTH; j++) suffixes_quicksort.push(j);
			cache_quicksort = new long[STRING_LENGTH];
			for (j=0; j<STRING_LENGTH; j++) cache_quicksort[j]=string.load63(j<<string.log2BitsPerInt);
			Suffixes.quicksort(suffixes_quicksort,0,STRING_LENGTH,0,0,string,cache_quicksort,6*STRING_LENGTH,40,random);

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