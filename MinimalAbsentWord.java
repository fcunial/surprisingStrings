/**
 * Instructs $SubstringIterator$ to visit only the minimal absent words of a string.
 * Being dependent on $SubstringIterator$ and on $RightMaximalSubstring$, this class must
 * be adapted to the case of large alphabet.
 */
public class MinimalAbsentWord extends MaximalRepeat {

	protected int[][] minimalAbsent;
	protected int lastMinimalAbsent;

	/**
	 * Artificial no-argument constructor, used just to avoid compile-time errors.
	 * See the no-argument constructor of $Substring$ for details.
	 */
	protected MinimalAbsentWord() { }


	protected MinimalAbsentWord(int alphabetLength, int log2alphabetLength, long bwtLength, int log2bwtLength) {
		super(alphabetLength,log2alphabetLength,bwtLength,log2bwtLength);
		minimalAbsent = new int[alphabetLength*alphabetLength][2];
	}


	protected Substring getInstance(int alphabetLength, int log2alphabetLength, long bwtLength, int log2bwtLength) {
		return new MinimalAbsentWord(alphabetLength,log2alphabetLength,bwtLength,log2bwtLength);
	}


	protected void visited(Stream stack, RigidStream characterStack, SimpleStream pointerStack, Substring[] leftExtensions) {
		super.visited(stack,characterStack,pointerStack,leftExtensions);
		if (leftContext<2) return;

		int i, j;
		lastMinimalAbsent=-1;
		for (i=1; i<alphabetLength+1; i++) {  // Discarding $#$
			if (leftExtensions[i].frequency()==0) continue;
			for (j=1; j<alphabetLength+1; j++) {
				if (bwtIntervals[j][1]<bwtIntervals[j][0]) continue;
				if (leftExtensions[i].bwtIntervals[j][1]>=leftExtensions[i].bwtIntervals[j][0]) continue;
				lastMinimalAbsent++;
				minimalAbsent[lastMinimalAbsent][0]=i-1;
				minimalAbsent[lastMinimalAbsent][1]=j-1;
			}
		}
	}

}