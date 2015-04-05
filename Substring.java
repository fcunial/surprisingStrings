/**
 * Representation of a substring $v$ of a text $s$ as understood by $SubstringIterator$.
 * For $SubstringIterator$, a substring $v$ of $s$ has the following features: (1) A set
 * of substring intervals in $BWT_s$, including but not limited to the interval of $v$.
 * These intervals are maintained by $SubstringIterator$ itself. (2) The ability to be
 * pushed to and popped from a stack, as well as a set of pointers to the stack. (3) The
 * ability to receive a signal when $v$ is explored for the first time by
 * $SubstringIterator$. (4) An additional set of variables, unknown to
 * $SubstringIterator$, that depend only on $suf(v)$, i.e. on the string that satisfies
 * $v = a \cdot suf(v), a \in \Sigma$. It is the responsibility of $v$ to automatically
 * update these features given $suf(v)$.
 *
 * Remark: This object contains just the first character of $v$. The remaining characters
 * must be reconstructed by navigating the stack suffix by suffix.
 *
 * Remark: This object and its subclasses are designed to be employed as reusable data
 * containers, i.e. just as facilities to load data from a bit stream, manipulate it, and
 * push it back to the stream, similar to JavaBeans. A typical program needs only a
 * limited number of instances of this object at any given time.
 */
public class Substring {

	protected static final int MAX_BITS_PER_POINTER = 64;  // Maximum number of bits to encode a stack pointer in $serialized(v)$
	protected static final int MAX_BITS_PER_LENGTH = 64;  // Maximum number of bits to encode a substring length in $serialized(v)$
	protected static final int MIN_POINTERS = 3;  // Minimum number of pointers in $stackPointers$
	protected final int alphabetLength, log2alphabetLength, log2textLength;
	protected final int nIntervals;  // Number of rows in $bwtIntervals$
	protected final int nPointers;  // Number of elements in $stackPointers$
	protected final long textLength;

	/**
	 * Intervals of substrings (possibly different from $v$) in $BWT_s$, used to implement
	 * functions on $v$. This base class uses just $bwtIntervals[0]=(i_v,j_v)_s$.
	 */
	protected long[][] bwtIntervals;

	/**
	 * Pointers to substrings in a depth-first stack. This base class uses just:
	 *
	 * $stackPointers[0]$=index of the first bit of $serialized(v)$ in the stack;
	 * $stackPointers[1]$=index of the first bit of the previous serialized substring in
	 * the stack;
	 * $stackPointers[2]$=index of the first bit of $serialized(suf(v))$ in the stack.
	 *
	 * Subclasses can maintain more pointers to implement additional functions, but
	 * all such additional pointers must refer to the first bit of some $serialized(w)$
	 * such that $w.hasBeenExtended=true$.
	 */
	protected long[] stackPointers;
	private int log2address;  // $Utils.log2(stackPointers[0])$

	/**
	 * $|v|$
	 */
	protected long length;

	/**
	 * The first character of $v$
	 */
	protected int firstCharacter;

	/**
	 * TRUE iff $a \cdot v$ has been already visited by $SubstringIterator$, for all
	 * $a \in \Sigma$.
	 */
	protected boolean hasBeenExtended;

	/**
	 * TRUE iff $v$ has been stolen by a $SubstringIteratorThread$
	 */
	protected boolean hasBeenStolen;


	protected Substring(int alphabetLength, int log2alphabetLength, long textLength, int log2textLength) {
		this.alphabetLength=alphabetLength;
		this.log2alphabetLength=log2alphabetLength;
		this.textLength=textLength;
		this.log2textLength=log2textLength;
		nIntervals=1;
		bwtIntervals = new long[nIntervals][2];
		nPointers=3;
		stackPointers = new long[nPointers];
	}


	protected void deallocate() {
		for (int i=0; i<nIntervals; i++) bwtIntervals[i]=null;
		bwtIntervals=null;
		stackPointers=null;
	}


	public boolean equals(Object other) {
		int i;
		Substring otherSubstring = (Substring)other;
		if (nIntervals!=otherSubstring.nIntervals) return false;
		for (i=0; i<nIntervals; i++) {
			if (bwtIntervals[i][0]!=otherSubstring.bwtIntervals[i][0] || bwtIntervals[i][1]!=otherSubstring.bwtIntervals[i][1]) return false;
		}
		if (nPointers!=otherSubstring.nPointers) return false;
/*		for (i=0; i<nPointers; i++) {
			if (stackPointers[i]!=otherSubstring.stackPointers[i]) return false;
		}
*/		if (length!=otherSubstring.length) return false;
		if (firstCharacter!=otherSubstring.firstCharacter) return false;
		return true;
	}


	/**
	 * Factory of new $Substring$ objects and of $Substring$'s subclasses. Used to
	 * implement basic polymorphism in $SubstringIterator$ without going through the
	 * verbosity of the $java.lang.reflect$ apparatus.
	 */
	protected Substring getInstance() {
		return new Substring(alphabetLength,log2alphabetLength,textLength,log2textLength);
	}


	/**
	 * Stores in $characters$ a $Substring$ instance corresponding to each character in
	 * the \emph{effective} alphabet of the text, and to $#$, in lexicographic order.
	 *
	 * @param C the $C$ array of backward search;
	 * @param characters empty array with at least $alphabetLength+1$ cells;
	 * @return the number of cells in $characters$ effectively filled. This number might
	 * be smaller than $alphabetLength+1$ if the effective alphabet of the text is smaller
	 * than the entire alphabet.
	 */
	protected int getLengthOneSubstrings(long[] C, Substring[] characters) {
		int i, j;
		Substring w;

		// Character $\$$
		w=getInstance();
		w.bwtIntervals[0][0]=0;
		w.bwtIntervals[0][1]=0;
		w.length=1;
		w.firstCharacter=-1;
		characters[0]=w;

		// Other characters
		j=1;
		for (i=0; i<alphabetLength-1; i++) {
			if (C[i+1]-C[i]>0) {
				w=getInstance();
				w.bwtIntervals[0][0]=C[i];
				w.bwtIntervals[0][1]=C[i+1]-1;
				w.length=1;
				w.firstCharacter=i;
				characters[j++]=w;
			}
		}
		if (textLength+1-C[alphabetLength-1]>0) {
			w=getInstance();
			w.bwtIntervals[0][0]=C[alphabetLength-1];
			w.bwtIntervals[0][1]=textLength;
			w.length=1;
			w.firstCharacter=alphabetLength-1;
			characters[j++]=w;
		}
		return j;
	}


	/**
	 * Pushes to $sequence$ the sequence of characters of $substring$, which is assumed to
	 * be stored in $stack$. $substring$ is assumed to have already been deserialized;
	 * $sequence$ is assumed to be large enough to contain $substring.length$.
	 *
	 * @return TRUE iff $substring.firstCharacter==-1$. This character is not appended to
	 * $sequence$.
	 */
	public static final boolean getSequence(Substring substring, Stream stack, IntArray sequence) {
		final long backupPosition = stack.getPosition();
		final long length = substring.length;
		boolean out = false;
		Substring w = new Substring(substring.alphabetLength,substring.log2alphabetLength,substring.textLength,substring.log2textLength);
		w.firstCharacter=substring.firstCharacter;
		w.stackPointers[2]=substring.stackPointers[2];
		sequence.clear();

		// First character
		if (w.firstCharacter==-1) out=true;
		else sequence.push(w.firstCharacter);

		// Other characters
		for (long i=1; i<length; i++) {
			stack.setPosition(w.stackPointers[2]);
			w.read(stack);
			sequence.push(w.firstCharacter);
		}

		stack.setPosition(backupPosition);
		w.deallocate(); w=null;
		return out;
	}


	public String toString() {
		return "[address="+stackPointers[0]+"|previous="+stackPointers[1]+"|suffix="+stackPointers[2]+"|length="+length+"]";
	}


	/**
	 * Initializes string $v$ from the current setting of $bwtIntervals$ and from $suf(v)$.
	 *
	 * @param firstCharacter -1 indicates $\$$.
	 */
	protected void init(Substring suffix, int firstCharacter) {
		stackPointers[2]=suffix.stackPointers[0];
		length=suffix.length+1;
		this.firstCharacter=firstCharacter;
	}


	/**
	 * @return true iff the left-extensions of $v$ should be explored by
	 * $SubstringIterator$.
	 */
	protected boolean shouldBeExtendedLeft() {
		return firstCharacter>-1;  // Not extending to the left substrings that start by $#$
	}


	/**
	 * Signal produced by $SubstringIterator$ when it visits $v$
	 */
	protected void visited(Stream stack) { }


	/**
	 * @return true iff $v$ occurs in $s$
	 */
	protected boolean occurs() {
		return bwtIntervals[0][0]>=0 && bwtIntervals[0][0]<=textLength &&
			   bwtIntervals[0][1]>=0 && bwtIntervals[0][1]<=textLength &&
			   bwtIntervals[0][1]>=bwtIntervals[0][0];
	}




	// ---------------------------- STACK MANAGEMENT -------------------------------------

	/**
	 * Appends to $stack$ the serialized representation of $v$.
	 * This method overwrites $stackPointers[0]$.
	 */
	protected void push(Stream stack) {
		stackPointers[0]=stack.length();
		log2address=stackPointers[0]==0?MAX_BITS_PER_POINTER:Utils.log2(stackPointers[0]);
		stack.push(stackPointers[1],log2address);
		stack.push(hasBeenExtended?1:0,1);
		stack.push(hasBeenStolen?1:0,1);
		int i;
		for (i=2; i<nPointers; i++) stack.push(stackPointers[i],log2address);
		for (i=0; i<nIntervals; i++) {
			stack.push(bwtIntervals[i][0],log2textLength);
			stack.push(bwtIntervals[i][1],log2textLength);
		}
		stack.push(length,log2textLength);
		stack.push(firstCharacter,log2alphabetLength);
	}


	/**
	 * @return an \emph{upper bound} on the number of bits required to serialize any
	 * instance of this class.
	 */
	protected long serializedSize() {
		return 1+1+
			   (nPointers-1)*MAX_BITS_PER_POINTER+
			   (nIntervals<<1)*log2textLength+
			   MAX_BITS_PER_LENGTH+
			   log2alphabetLength;
	}


	/**
	 * Reads $v$ from $stack$ starting from $stack.getPosition()$. At the end of the
	 * process, the pointer of $stack$ is located at the first bit that follows
	 * $serialized(v)$.
	 *
	 * Remark: the procedure assumes that the pointer of $stack$ is indeed positioned at
	 * the beginning of $serialized(v)$: no explicit check is performed.
	 */
	protected void read(Stream stack) {
		stackPointers[0]=stack.getPosition();
		log2address=stackPointers[0]==0?MAX_BITS_PER_POINTER:Utils.log2(stackPointers[0]);
		stackPointers[1]=stack.read(log2address);
		hasBeenExtended=stack.read(1)==1?true:false;
		hasBeenStolen=stack.read(1)==1?true:false;
		int i;
		for (i=2; i<nPointers; i++) stackPointers[i]=stack.read(log2address);
		for (i=0; i<nIntervals; i++) {
			bwtIntervals[i][0]=stack.read(log2textLength);
			bwtIntervals[i][1]=stack.read(log2textLength);
		}
		length=stack.read(log2textLength);
		firstCharacter=(int)stack.read(log2alphabetLength);
	}


	/**
	 * Same as $read$, but if $hasBeenExtended=true$ or $hasBeenStolen=true$ the procedure
	 * halts without reading the whole serialized substring.
	 */
	protected void readFast(Stream stack) {
		stackPointers[0]=stack.getPosition();
		log2address=stackPointers[0]==0?MAX_BITS_PER_POINTER:Utils.log2(stackPointers[0]);
		stackPointers[1]=stack.read(log2address);
		hasBeenExtended=stack.read(1)==1?true:false;
		if (hasBeenExtended) return;
		hasBeenStolen=stack.read(1)==1?true:false;
		if (hasBeenStolen) return;
		int i;
		for (i=2; i<nPointers; i++) stackPointers[i]=stack.read(log2address);
		for (i=0; i<nIntervals; i++) {
			bwtIntervals[i][0]=stack.read(log2textLength);
			bwtIntervals[i][1]=stack.read(log2textLength);
		}
		length=stack.read(log2textLength);
		firstCharacter=(int)stack.read(log2alphabetLength);
	}


	/**
	 * Removes $serialized(v)$ from the top of the stack, assuming that $v$ has already
	 * been deserialized and that $serialized(v)$ is indeed at the top of the stack.
	 * At the end of the procedure, the pointer of $stack$ is located at the first bit of
	 * the previous serialized substring.
	 */
	protected void pop(Stream stack) {
		int i;
		stack.pop(log2alphabetLength);
		stack.pop(log2textLength);
		for (i=0; i<nIntervals; i++) {
			stack.pop(log2textLength);
			stack.pop(log2textLength);
		}
		for (i=2; i<nPointers; i++) stack.pop(log2address);
		stack.pop(1);
		stack.pop(1);
		stack.pop(log2address);
		stack.setPosition(stackPointers[1]);
	}


	/**
	 * Assume that the pointer in $stack$ is currently at the beginning of this substring
	 * $v$. The procedure advances the pointer to the beginning of the following substring.
	 *
	 * @return the number of bits to encode pointers in $serialized(v)$.
	 */
	protected int skip(Stream stack) {
		stack.setPosition(stackPointers[0]+log2address+
										   1+1+
										   (nPointers-2)*log2address+
										   (nIntervals<<1)*log2textLength+
										   log2textLength+
										   log2alphabetLength);
		return log2address;
	}


	/**
	 * Sets $hasBeenExtended=true$ in the serialized representation of $v$ in $stack$,
	 * but not in this object. Then, the pointer of $stack$ is restored to its initial
	 * state.
	 */
	protected final void markAsExtended(Stream stack) {
		long backupPointer = stack.getPosition();
		stack.setBit(stackPointers[0]+log2address);
		stack.setPosition(backupPointer);
	}


	/**
	 * Sets $hasBeenStolen=true$ in the serialized representation of $v$ in $stack$,
	 * but not in this object. Then, the pointer of $stack$ is restored to its initial
	 * state.
	 */
	protected final void markAsStolen(Stream stack) {
		long backupPointer = stack.getPosition();
		stack.setBit(stackPointers[0]+log2address+1);
		stack.setPosition(backupPointer);
	}

}