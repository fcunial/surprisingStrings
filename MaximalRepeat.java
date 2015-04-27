/**
 * Instructs $SubstringIterator$ to visit only the maximal repeats of a string.
 * Being dependent on $SubstringIterator$ and on $RightMaximalSubstring$, this class must
 * be adapted to the case of large alphabet.
 */
public class MaximalRepeat extends RightMaximalSubstring {

	protected int leftContext;

	/**
	 * Artificial no-argument constructor, used just to avoid compile-time errors.
	 * See the no-argument constructor of $Substring$ for details.
	 */
	protected MaximalRepeat() { }


	protected MaximalRepeat(int alphabetLength, int log2alphabetLength, long bwtLength, int log2bwtLength) {
		super(alphabetLength,log2alphabetLength,bwtLength,log2bwtLength);
	}


	protected Substring getInstance(int alphabetLength, int log2alphabetLength, long bwtLength, int log2bwtLength) {
		return new MaximalRepeat(alphabetLength,log2alphabetLength,bwtLength,log2bwtLength);
	}


	protected final void computeLeftContext(Substring[] leftExtensions) {
		leftContext=0;
		for (int i=0; i<alphabetLength+1; i++) {
			if (leftExtensions[i].frequency()>0) leftContext++;
		}
	}


	protected void visited(Stream stack, RigidStream characterStack, SimpleStream pointerStack, Substring[] leftExtensions) {
		// Strings on which $visited$ is called are right-maximal.
		computeLeftContext(leftExtensions);
		if (leftContext<2) return;
	}

}