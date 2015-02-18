import java.util.Arrays;

/**
 * Simplistic implementation of a Huffman-shaped wavelet tree with pointers, using
 * Sebastiano Vigna's $Rank9$ data structure \cite{Broadword implementation of rank select
 * queries} to support rank operations inside a node.
 *
 * Remark: We try to optimize time rather than space. We allow construction to use
 * $|s|(H_0(s)+1)$ bits of additional space, where $s$ is the input string, i.e. we don't
 * implement in-place algorithms like \cite{On wavelet tree construction}.
 *
 * Remark: We assume that the alphabet is small. In this case, the Huffman-shaped wavelet
 * tree with pointers is among the best solutions both in space and in time for general
 * strings \cite{Practical rank-select queries over arbitrary sequences}, and it should
 * behave even better for blocks of the BWT. We don't implement the wavelet matrix
 * \cite{The wavelet matrix} because it is useful in practice only for large alphabets.
 */
public class HuffmanWaveletTree {

	private final int alphabetLength, log2AlphabetLength;

	/**
	 * Tree topology and rank data structures. A nonnegative value in $leftChild[i]$,
	 * $rightChild[i]$ and $*Parent[i]$ is the position of the corresponding internal node
	 * in the arrays. A negative value $c$ identifies symbol $-1-c$ in the
	 * lexicographically sorted alphabet.
	 */
	private int[] leftChild, rightChild, nodeParent, leafParent;
	protected Rank9[] rankDataStructures;

	/**
	 * Maximum number of bits in the Huffman code
	 */
	public final int maxCodeLength;


	/**
	 * @param alphabet symbols that occur in $string$, sorted lexicographically;
	 * @param counts number of occurrences in $string$ of each symbol in $alphabet$.
	 */
	public HuffmanWaveletTree(IntArray string, int[] alphabet, IntArray counts) {
		final long stringLength, suffixArrayLength;
		int i, j, k, node, code, length;
		long il;
		boolean[][] codes;
		int[] codeLengths;
		long[] nBits;
		float[] frequencies;
		IntArray[] bitVectors;

		// Building tree topology and bit vectors
		alphabetLength=alphabet.length;
		log2AlphabetLength=Utils.log2(alphabetLength);
		codes = new boolean[alphabetLength][alphabetLength-1];
		codeLengths = new int[alphabetLength];
		frequencies = new float[alphabetLength];
		stringLength=string.length();
		for (i=0; i<alphabetLength; i++) frequencies[i]=((float)counts.getElementAt(i))/stringLength;
		maxCodeLength=buildHuffmanCodes(alphabet,frequencies,codes,codeLengths);
		frequencies=null;
		bitVectors = new IntArray[alphabetLength-1];
		nBits = new long[alphabetLength-1];
		for (i=0; i<alphabetLength; i++) {
			node=leafParent[i];
			while (node!=-1) {
				nBits[node]+=counts.getElementAt(i);
				node=nodeParent[node];
			}
		}
		for (i=0; i<alphabetLength-1; i++) bitVectors[i] = new IntArray((int)nBits[i],1);
		nBits=null;

		// Pushing bits from $string$
		for (il=0; il<stringLength; il++) {
			j=string.getElementAt((int)il);
			length=codeLengths[j];
			node=alphabetLength-2;
			for (k=length-1; k>=0; k--) {
				if (codes[j][k]) {
					bitVectors[node].pushFromRight(1);  // $Rank9$ stores bits from right to left
					node=rightChild[node];
				}
				else {
					bitVectors[node].pushFromRight(0);  // $Rank9$ stores bits from right to left
					node=leftChild[node];
				}
			}
		}
		codes=null; codeLengths=null;
		rankDataStructures = new Rank9[alphabetLength-1];
		for (i=0; i<alphabetLength-1; i++) {
			rankDataStructures[i] = new Rank9(bitVectors[i]);
			bitVectors[i].deallocate();  // Removes a link to the underlying array, but does not destroy the array itself.
			bitVectors[i]=null;
		}
		bitVectors=null;
	}


	/**
	 * @param alphabet symbols in the alphabet, sorted lexicographically;
	 * @param frequencies relative frequency of each symbol in $alphabet$; they are
	 * assumed to sum to one;
	 * @param codes output array that stores the Huffman code corresponding to each
	 * element of $symbols$ at the end of the procedure, encoded as a *reversed* sequence
	 * of booleans; all cells of $codes$ are assumed to be FALSE at the beginning;
	 * representing a code as a sequence of booleans increases space (which is irrelevant
	 * under the assumption that $alphabet$ is small), but makes access to bits faster;
	 * @param codeLengths output array that stores the length in bits of each element
	 * of $codes$ at the end of the procedure; all cells of $codeLengths$ are assumed to
	 * be zero at the beginning;
	 * @return the maximum number in $codeLengths$.
	 */
	private final int buildHuffmanCodes(int[] alphabet, float[] frequencies, boolean[][] codes, int[] codeLengths) {
		int i, j, leafQueueFront, nodeQueueFront, nodeQueueBack, address, node, child, maxCodeLength;
		float nodeFrequency;
		int[] stack, sortedAlphabet;
		float[] sortedFrequencies, nodeFrequencies;
		long[] tmpArray;

		// Sorting alphabet by frequency
		tmpArray = new long[alphabetLength];
		for (i=0; i<alphabetLength; i++) tmpArray[i]=(((long)Float.floatToIntBits(frequencies[i]))<<32)|alphabet[i];
		Arrays.sort(tmpArray);
		sortedAlphabet = new int[alphabetLength];
		sortedFrequencies = new float[alphabetLength];
		for (i=0; i<alphabetLength; i++) {
			sortedFrequencies[i]=Float.intBitsToFloat((int)((tmpArray[i]&Utils.shiftOnesLeft[32])>>>32));
			sortedAlphabet[i]=(int)(tmpArray[i]&Utils.shiftOnesRight[32]);
		}
		tmpArray=null;

		// Building tree topology
		leftChild = new int[alphabetLength-1];
		rightChild = new int[alphabetLength-1];
		nodeParent = new int[alphabetLength-1];
		for (i=0; i<alphabetLength-1; i++) nodeParent[i]=-1;
		leafParent = new int[alphabetLength];
		for (i=0; i<alphabetLength; i++) leafParent[i]=-1;
		nodeFrequencies = new float[alphabetLength-1];
		for (i=0; i<alphabetLength-1; i++) nodeFrequencies[i]=1f;
		nodeQueueFront=0; nodeQueueBack=0; leafQueueFront=0;
		while (alphabetLength-leafQueueFront+nodeQueueBack-nodeQueueFront>1) {
			nodeFrequency=0f;
			if (leafQueueFront<alphabetLength && sortedFrequencies[leafQueueFront]<nodeFrequencies[nodeQueueFront]) {
				address=Arrays.binarySearch(alphabet,sortedAlphabet[leafQueueFront]);
				leftChild[nodeQueueBack]=-1-address;
				leafParent[address]=nodeQueueBack;
				nodeFrequency=sortedFrequencies[leafQueueFront];
				leafQueueFront++;
			}
			else {
				address=nodeQueueFront;
				leftChild[nodeQueueBack]=address;
				nodeParent[address]=nodeQueueBack;
				nodeFrequency=nodeFrequencies[nodeQueueFront];
				nodeQueueFront++;
			}
			if (leafQueueFront<alphabetLength && sortedFrequencies[leafQueueFront]<nodeFrequencies[nodeQueueFront]) {
				address=Arrays.binarySearch(alphabet,sortedAlphabet[leafQueueFront]);
				rightChild[nodeQueueBack]=-1-address;
				leafParent[address]=nodeQueueBack;
				nodeFrequency+=sortedFrequencies[leafQueueFront];
				leafQueueFront++;
			}
			else {
				address=nodeQueueFront;
				rightChild[nodeQueueBack]=address;
				nodeParent[address]=nodeQueueBack;
				nodeFrequency+=nodeFrequencies[nodeQueueFront];
				nodeQueueFront++;
			}
			nodeFrequencies[nodeQueueBack]=nodeFrequency;
			nodeQueueBack++;
		}
		sortedAlphabet=null;
		sortedFrequencies=null;
		nodeFrequencies=null;

		// Assigning codes
		for (i=0; i<alphabetLength; i++) {
			codeLengths[i]=0;
			node=leafParent[i];
			child=-1-i;
			while (node!=-1) {
				if (child==rightChild[node]) codes[i][codeLengths[i]]=true;
				codeLengths[i]++;
				child=node;
				node=nodeParent[node];
			}
		}

		// Returning the length of the longest code
		maxCodeLength=0;
		for (i=0; i<alphabetLength; i++) {
			if (codeLengths[i]>maxCodeLength) maxCodeLength=codeLengths[i];
		}
		return maxCodeLength;
	}


	/**
	 * Computes the number of occurrences of every symbol in the alphabet before each
	 * position of a list of distinct positions relative to $string$. The procedure
	 * touches all nodes of the wavelet tree. All input positions are ranked at each node,
	 * to limit cache misses.
	 *
	 * @param nPositions number of positions in $string$ to rank;
	 * @param stack a temporary matrix with at least $alphabetLength-1$ rows and
	 * $1+nPositions$ columns; the procedure assumes that the positions to be ranked are
	 * written in increasing order in row 0, starting from index 1 (not zero); the content
	 * of this matrix is altered by the procedure;
	 * @param output output matrix with at least $alphabetLength$ rows and $nPositions$
	 * columns; the alphabet is assumed to be sorted lexicographically;
	 * @param ones a temporary array with at least $nPositions$ cells; the
	 * content of this array is altered by the procedure.
	 */
	public final void multirank(int nPositions, long[][] stack, long[][] output, long[] ones) {
		int i, currentBlock, lastBlock, node, address;
		stack[0][0]=alphabetLength-2;
		currentBlock=0; lastBlock=0;
		while (currentBlock<=lastBlock) {
			node=(int)stack[currentBlock][0];
			for (i=0; i<nPositions; i++) ones[i]=rankDataStructures[node].rank(stack[currentBlock][1+i]);
			address=leftChild[node];
			if (address<0) {
				for (i=0; i<nPositions; i++) output[-1-address][i]=stack[currentBlock][1+i]-ones[i];
			}
			else {
				lastBlock++;
				stack[lastBlock][0]=address;
				for (i=0; i<nPositions; i++) stack[lastBlock][1+i]=stack[currentBlock][1+i]-ones[i];
			}
			address=rightChild[node];
			if (address<0) System.arraycopy(ones,0,output[-1-address],0,nPositions);
			else {
				lastBlock++;
				stack[lastBlock][0]=address;
				System.arraycopy(ones,0,stack[lastBlock],1,nPositions);
			}
			currentBlock++;
		}
	}

}