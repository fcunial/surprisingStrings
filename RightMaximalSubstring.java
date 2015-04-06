/**
 * Instructs $SubstringIterator$ to visit only the right-maximal substrings $w$ of a
 * string. $intervals[c]$ contains the interval of $wc$ for all $c \in \Sigma \cup \{#\}$,
 * in lexicographic order: see \cite{belazzougui2014linear}.
 */
public class RightMaximalSubstring extends Substring {

	protected boolean isRightMaximal;


	/**
	 * Artificial no-argument constructor, used just to avoid compile-time errors.
	 * See the no-argument constructor of $Substring$ for details.
	 */
	protected RightMaximalSubstring() { }


	protected RightMaximalSubstring(int alphabetLength, int log2alphabetLength, long textLength, int log2textLength) {
		this.alphabetLength=alphabetLength;
		this.log2alphabetLength=log2alphabetLength;
		this.textLength=textLength;
		this.log2textLength=log2textLength;
		nIntervals=alphabetLength+1;
		bwtIntervals = new long[nIntervals][2];
		bwtIntervalsAreSorted=true;
		nPointers=3;
		stackPointers = new long[nPointers];
	}


	protected static Substring getInstance(int alphabetLength, int log2alphabetLength, long textLength, int log2textLength) {
		return new RightMaximalSubstring(alphabetLength,log2alphabetLength,textLength,log2textLength);
	}


	protected Substring getEpsilon(long[] C) {
		RightMaximalSubstring out = new RightMaximalSubstring(alphabetLength,log2alphabetLength,textLength,log2textLength);
		out.length=0;

		// $#$
		out.bwtIntervals[0][0]=0;
		out.bwtIntervals[0][1]=0;

		// Other characters
		for (int i=0; i<alphabetLength-1; i++) {
			out.bwtIntervals[i+1][0]=C[i];
			out.bwtIntervals[i+1][1]=C[i+1]-1;
		}
		out.bwtIntervals[alphabetLength][0]=C[alphabetLength-1];
		out.bwtIntervals[alphabetLength][1]=textLength;

		return out;
	}


	protected void init(Substring suffix, int firstCharacter) {
		super.init(suffix,firstCharacter);
		int rightContext = 0;
		for (int c=0; c<alphabetLength; c++) {
			if (bwtIntervals[c][1]>=bwtIntervals[c][0]) rightContext++;
		}
		isRightMaximal=rightContext>1;
	}


	protected boolean shouldBeExtendedLeft() {
		return isRightMaximal;
	}


	protected boolean occurs() {
		return bwtIntervals[alphabetLength][1]>=bwtIntervals[0][0];
	}

}