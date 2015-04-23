/**
 * Instructs $SubstringIterator$ to visit only the right-maximal substrings $w$ of a
 * string. $intervals[c]$ contains the interval of $wc$ for all $c \in \Sigma \cup \{#\}$,
 * in lexicographic order (see \cite{belazzougui2014linear}). This choice is not suitable
 * for large alphabets.
 */
public class RightMaximalSubstring extends Substring {

	protected int rightContext;


	/**
	 * Artificial no-argument constructor, used just to avoid compile-time errors.
	 * See the no-argument constructor of $Substring$ for details.
	 */
	protected RightMaximalSubstring() { }


	protected RightMaximalSubstring(int alphabetLength, int log2alphabetLength, long bwtLength, int log2bwtLength) {
		this.alphabetLength=alphabetLength;
		this.log2alphabetLength=log2alphabetLength;
		this.bwtLength=bwtLength;
		this.log2bwtLength=log2bwtLength;
		MAX_INTERVALS=alphabetLength+1;
		BITS_TO_ENCODE_MAX_INTERVALS=Utils.bitsToEncode(MAX_INTERVALS);
		BWT_INTERVALS_ARE_SORTED=true;
		bwtIntervals = new long[MAX_INTERVALS][2];
		MAX_POINTERS=MIN_POINTERS;
		BITS_TO_ENCODE_MAX_POINTERS=Utils.bitsToEncode(MAX_POINTERS);
		stackPointers = new long[MAX_POINTERS];
	}


	protected Substring getInstance(int alphabetLength, int log2alphabetLength, long bwtLength, int log2bwtLength) {
		return new RightMaximalSubstring(alphabetLength,log2alphabetLength,bwtLength,log2bwtLength);
	}


	protected Substring getEpsilon(long[] C) {
		Substring out = getInstance();
		out.length=0;
		out.nIntervals=alphabetLength+1;
		out.nPointers=MIN_POINTERS;

		// $#$
		out.bwtIntervals[0][0]=0;
		out.bwtIntervals[0][1]=0;

		// Other characters
		for (int i=0; i<alphabetLength-1; i++) {
			out.bwtIntervals[i+1][0]=C[i];
			out.bwtIntervals[i+1][1]=C[i+1]-1;
		}
		out.bwtIntervals[alphabetLength][0]=C[alphabetLength-1];
		out.bwtIntervals[alphabetLength][1]=bwtLength-1;

		return out;
	}


	protected final void computeRightContext() {
		rightContext=0;
		for (int c=0; c<nIntervals; c++) {
			if (bwtIntervals[c][1]>=bwtIntervals[c][0]) rightContext++;
		}
	}


	protected void init(Substring suffix, int firstCharacter, Stream stack, RigidStream characterStack, SimpleStream pointerStack, long[] buffer) {
		super.init(suffix,firstCharacter,stack,characterStack,pointerStack,buffer);
		computeRightContext();
	}


	protected boolean shouldBeExtendedLeft() {
		return rightContext>1;
	}


	protected long frequency() {
		return bwtIntervals[alphabetLength][1]>=bwtIntervals[0][0]?bwtIntervals[alphabetLength][1]-bwtIntervals[0][0]+1:0;
	}

}