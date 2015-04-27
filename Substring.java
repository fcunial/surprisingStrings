/**
 * Representation of a substring $v$ of a text $s$ as understood by $SubstringIterator$.
 * For $SubstringIterator$, a substring $v$ of $s$ has the following features:
 *
 * (1) A set of substring intervals in $BWT_s$ (for example the interval of $v$). Such
 * intervals are maintained by $SubstringIterator.extendLeft$. $Substring$ fixes the
 * maximum number of intervals, the intervals of $\epsilon$, and whether the starting and
 * ending positions of the intervals are stored in sorted order. Different instances of
 * the same $Substring$ class could have a different number of intervals.
 *
 * (2) A set of pointers to previous positions in the stack of $SubstringIterator$. Such
 * pointers are managed by $Substring$ itself, specifically by method $init$. $Substring$
 * fixes also the maximum number of such pointers. Different instances of the same
 * $Substring$ class could have a different number of pointers.
 *
 * (3) An additional set of variables, unknown to $SubstringIterator$, that depend only on
 * $suf(v)$, i.e. on the string that satisfies $v = a \cdot suf(v), a \in \Sigma$. It is
 * the responsibility of $v$ to update these variables given $suf(v)$, inside method $init$.
 *
 * (4) The ability to be pushed to and popped from the stack of $SubstringIterator$.
 *
 * (5) The ability to receive a signal when $v$ is explored for the first time by
 * $SubstringIterator$ (method $visited$).
 *
 * Remark: This object and its subclasses are designed to be employed as reusable data
 * containers, i.e. just as facilities to load data from a bit stream, manipulate it, and
 * push it back to the stream, similar to JavaBeans. A typical program needs only a
 * limited number of instances of this object at any given time, it allocates the memory
 * for each such instance exactly once (inside the constructor), and such memory is the
 * largest possible to accommodate any instance loaded from the stack.
 */
public class Substring {

	protected final int MAX_BITS_PER_POINTER = 64;  // Maximum number of bits to encode a stack pointer in $serialized(v)$
	protected final int MAX_BITS_PER_LENGTH = 64;  // Maximum number of bits to encode a substring length in $serialized(v)$
	protected static final int MIN_POINTERS = 2;  // Minimum number of pointers in $stackPointers$
	protected int MAX_POINTERS;  // Maximum number of pointers in $stackPointers$. To be set by each descendant class.
	protected int BITS_TO_ENCODE_MAX_POINTERS;
	protected int MAX_INTERVALS;  // Maximum number of rows in $bwtIntervals$. To be set by each descendant class.
	protected int BITS_TO_ENCODE_MAX_INTERVALS;
	protected boolean BWT_INTERVALS_ARE_SORTED;  // TRUE iff the sequence $bwtIntervals[0][0],bwtIntervals[0][1],bwtIntervals[1][0],bwtIntervals[0][1],...$ is increasing. Avoids one sorting operation in $extendLeft$. To be set by each descendant class.
	protected int alphabetLength, log2alphabetLength, log2bwtLength;
	protected int nIntervals;  // Number of rows in $bwtIntervals$
	protected int nPointers;  // Number of elements in $stackPointers$
	protected long bwtLength, textLength;
	protected double oneOverLogTextLength;

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
	 * the stack.
	 *
	 * Subclasses can maintain more pointers to implement additional functions, but
	 * all such additional pointers must refer to the first bit of some $serialized(w)$
	 * such that $w.hasBeenExtended=true$.
	 */
	protected long[] stackPointers;
	protected int log2address;  // $Utils.log2(stackPointers[0])$

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


	/**
	 * Artificial no-argument constructor, used just to avoid compile-time errors.
	 * Every subclass of $Substring$ must provide a full reimplementation of the
	 * constructor with arguments.
	 */
	protected Substring() { }


	/**
	 * @param bwtLength $|s|+1$, where $s$ is the input text;
	 * @param log2bwtLength $\log_{2}(|s|+1)$, where $s$ is the input text.
	 */
	protected Substring(int alphabetLength, int log2alphabetLength, long bwtLength, int log2bwtLength) {
		this.alphabetLength=alphabetLength;
		this.log2alphabetLength=log2alphabetLength;
		this.bwtLength=bwtLength;
		textLength=bwtLength-1;
		oneOverLogTextLength=1D/Math.log(textLength);
		this.log2bwtLength=log2bwtLength;
		MAX_INTERVALS=1;
		BITS_TO_ENCODE_MAX_INTERVALS=Utils.bitsToEncode(MAX_INTERVALS);
		BWT_INTERVALS_ARE_SORTED=true;
		bwtIntervals = new long[MAX_INTERVALS][2];
		MAX_POINTERS=MIN_POINTERS;
		BITS_TO_ENCODE_MAX_POINTERS=Utils.bitsToEncode(MAX_POINTERS);
		stackPointers = new long[MAX_POINTERS];
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
/*		if (nPointers!=otherSubstring.nPointers) return false;
		for (i=0; i<nPointers; i++) {
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
		return new Substring(alphabetLength,log2alphabetLength,bwtLength,log2bwtLength);
	}


	/**
	 * Returns a representation of the empty string, which is pushed on the stack first.
	 *
	 * @param C the $C$ array of backward search, assumed to exclude $#$.
	 */
	protected Substring getEpsilon(long[] C) {
		Substring out = getInstance();
		out.length=0;
		out.nIntervals=1;
		out.bwtIntervals[0][0]=0;
		out.bwtIntervals[0][1]=bwtLength-1;
		out.nPointers=MIN_POINTERS;
		return out;
	}


	/**
	 * Pushes to $sequence$ the sequence of characters of $substring$, in left-to-right
	 * order, which is assumed to be stored in $characterStack$ in left-to-right order.
	 * $substring$ is assumed to have already been read from the stack; $sequence$ is
	 * assumed to be large enough to contain $substring.length$ elements.
	 *
	 * @return TRUE iff $substring.firstCharacter==-1$. This character is not appended to
	 * $sequence$.
	 */
	public final boolean getSequence(RigidStream characterStack, IntArray sequence) {
		boolean out = false;
		sequence.clear();
		if (length==0) return false;
		if (firstCharacter==-1) out=true;
		else sequence.push(firstCharacter);
		for (long i=length-2; i>=0; i--) sequence.push(characterStack.getElementAt(i));
		return out;
	}


	public String toString() {
		String out = "address="+stackPointers[0]+"|previous="+stackPointers[1]+"|length="+length+"|firstCharacter="+firstCharacter+"|nPointers="+nPointers+"|nIntervals="+nIntervals+"\n";
		for (int i=0; i<nIntervals; i++) out+="["+bwtIntervals[i][0]+".."+bwtIntervals[i][1]+"], ";
		out+="\n";
		return out;
	}


	/**
	 * Initializes string $v$ from $suf(v)$ and from the current setting of
	 * $bwtIntervals$. At this point the left-extensions of $v$ are not known yet.
	 *
	 * @param firstCharacter first character of $v$; -1 indicates $#$;
	 * @param buffer reusable memory area in which $suffix$ has stored additional
	 * information for the initialization of $v$. We assume
	 * $buffer.length>=alphabetLength+1$.
	 */
	protected void init(Substring suffix, int firstCharacter, Stream stack, RigidStream characterStack, SimpleStream pointerStack, long[] buffer) {
		length=suffix.length+1;
		this.firstCharacter=firstCharacter;
		nPointers=MIN_POINTERS;
	}


	/**
	 * Fills $buffer$ with messages for initializing the left extensions of $v$.
	 * The procedure assumes that $buffer$ is empty before invocation, i.e. that
	 * $buffer[i]=-1$ for all $i \in [0..buffer.length]$, and that
	 * $buffer.length>=alphabetLength+1$.
	 */
	protected void fillBuffer(long[] buffer, boolean flag) { }


	/**
	 * Returns $buffer$ to the empty state by undoing what has been done by $fillBuffer$.
	 */
	protected void emptyBuffer(long[] buffer, boolean flag) { }


	/**
	 * @return true iff the left-extensions of $v$ should be explored by
	 * $SubstringIterator$.
	 */
	protected boolean shouldBeExtendedLeft() {
		return firstCharacter>-1;  // Not extending to the left substrings that start by $#$
	}


	/**
	 * Signal produced by $SubstringIterator$ after it has initialized $v$ and after it
	 * has extended it to the left. This signal is launched only for strings that have
	 * been pushed on the stack, i.e. only for strings such that $shouldBeExtendedLeft$ is
	 * true.
	 */
	protected void visited(Stream stack, RigidStream characterStack, SimpleStream pointerStack, Substring[] leftExtensions) { }


	/**
	 * @return the number of occurrences of $v$ in $s$
	 */
	protected long frequency() {
		// We assume that the rest of the code is correct and that $bwtIntervals[0][0]$
		// and $bwtIntervals[0][1]$ are valid.
		return bwtIntervals[0][1]>=bwtIntervals[0][0]?bwtIntervals[0][1]-bwtIntervals[0][0]+1:0;
	}



/*                            _____ _             _
                             /  ___| |           | |
                             \ `--.| |_ __ _  ___| | __
                              `--. \ __/ _` |/ __| |/ /
                             /\__/ / || (_| | (__|   <
                             \____/ \__\__,_|\___|_|\_\

Format: HEAD | HEAD' || TAIL | TAIL'

HEAD is a header that is common both to substrings that have been extended and to
substrings that have not been extended. It contains the following fields:
1. stackPointers[1]
2. hasBeenExtended
3. hasBeenStolen
4. length
5. nPointers
6. stackPointers[MIN_POINTERS..nPointers-1]

TAIL is stored only for strings that have not been extended. It contains the following
fields:
1. firstCharacter
2. nIntervals
4. bwtIntervals[0..nIntervals-1]

HEAD' is a header defined by subclasses of $Substring$ that is common to both substrings
that have been extended and to substrings that have not been extended. TAIL' is a tail
defined by subclasses of $Substring$ that is stored only for strings that have not been
extended.
*/

	protected final void push(Stream stack) {
		pushHead(stack);
		pushHeadPrime(stack);
		if (!hasBeenExtended) {
			pushTail(stack);
			pushTailPrime(stack);
		}
	}


	/**
	 * Overwrites $stackPointers[0]$, and transforms into to $stackPointers[0]$ every -1
	 * in $stackPointers$.
	 */
	private final void pushHead(Stream stack) {
		stackPointers[0]=stack.nBits();
		log2address=stackPointers[0]==0?MAX_BITS_PER_POINTER:Utils.bitsToEncode(stackPointers[0]);
		stack.push(stackPointers[1],log2address);
		stack.push(hasBeenExtended?1:0,1);
		stack.push(hasBeenStolen?1:0,1);
		stack.push(length,log2bwtLength);
		stack.push(nPointers,BITS_TO_ENCODE_MAX_POINTERS);
		for (int i=MIN_POINTERS; i<nPointers; i++) {
			if (stackPointers[i]==-1) stackPointers[i]=stackPointers[0];
			stack.push(stackPointers[i],log2address);
		}
	}


	private final void pushTail(Stream stack) {
		stack.push(firstCharacter,log2alphabetLength);
		stack.push(nIntervals,BITS_TO_ENCODE_MAX_INTERVALS);
		for (int i=0; i<nIntervals; i++) {
			stack.push(bwtIntervals[i][0],log2bwtLength);
			stack.push(bwtIntervals[i][1],log2bwtLength);
		}
	}


	protected void pushHeadPrime(Stream stack) { }


	protected void pushTailPrime(Stream stack) { }


	/**
	 * Reads $v$ from $stack$ starting from $stack.getPosition()$. At the end of the
	 * process, the pointer of $stack$ is located at the first bit that follows
	 * $serialized(v)$. The procedure assumes that the pointer of $stack$ is indeed
	 * positioned at the beginning of $serialized(v)$: no explicit check is performed.
	 *
	 * @param fastHead skips information in HEAD if $hasBeenExtended=TRUE$;
	 * @param fastTail skips information in TAIL if $hasBeenStolen=TRUE$.
	 */
	protected final void read(Stream stack, boolean fastHead, boolean fastTail) {
		readHead(stack,fastHead);
		readHeadPrime(stack,fastHead);
		firstCharacter=-1;
		nIntervals=0;
		if (!hasBeenExtended) {
			readTail(stack,fastTail);
			readTailPrime(stack,fastTail);
		}
	}


	/**
	 * @param fast skips reading $stackPointers$ if $hasBeenExtended=TRUE$.
	 */
	private final void readHead(Stream stack, boolean fast) {
		stackPointers[0]=stack.getPosition();
		log2address=stackPointers[0]==0?MAX_BITS_PER_POINTER:Utils.bitsToEncode(stackPointers[0]);
		stackPointers[1]=stack.read(log2address);
		hasBeenExtended=stack.read(1)==1?true:false;
		hasBeenStolen=stack.read(1)==1?true:false;
		length=stack.read(log2bwtLength);
		nPointers=(int)stack.read(BITS_TO_ENCODE_MAX_POINTERS);
		if (fast && hasBeenExtended) stack.setPosition( stack.getPosition()+
			                   		     			    (nPointers-MIN_POINTERS)*log2address );
		else {
			for (int i=MIN_POINTERS; i<nPointers; i++) stackPointers[i]=stack.read(log2address);
		}
	}


	/**
	 * @param fast skips $bwtIntervals$ if $hasBeenStolen=TRUE$.
	 */
	private final void readTail(Stream stack, boolean fast) {
		firstCharacter=(int)stack.read(log2alphabetLength);
		nIntervals=(int)stack.read(BITS_TO_ENCODE_MAX_INTERVALS);
		if (fast && hasBeenStolen) stack.setPosition( stack.getPosition()+
			                   						  nIntervals*log2bwtLength*2 );
		else {
			for (int i=0; i<nIntervals; i++) {
				bwtIntervals[i][0]=stack.read(log2bwtLength);
				bwtIntervals[i][1]=stack.read(log2bwtLength);
			}
		}
	}


	protected void readHeadPrime(Stream stack, boolean fast) { }


	protected void readTailPrime(Stream stack, boolean fast) { }


	/**
	 * Removes $serialized(v)$ from the top of the stack, assuming that $v$ has already
	 * been deserialized and that $serialized(v)$ is indeed at the top of the stack.
	 *
	 * @param justTail removes just TAIL and TAIL'.
	 */
	protected final void pop(Stream stack, boolean justTail) {
		if (!hasBeenExtended) {
			popTailPrime(stack);
			popTail(stack);
		}
		if (!justTail) {
			popHeadPrime(stack);
			popHead(stack);
		}
	}


	private final void popTail(Stream stack) {
		stack.pop( nIntervals*log2bwtLength*2+
				   BITS_TO_ENCODE_MAX_INTERVALS+
		           log2alphabetLength );
	}


	private final void popHead(Stream stack) {
		stack.pop( log2address*(nPointers-MIN_POINTERS)+
		           BITS_TO_ENCODE_MAX_POINTERS+
		           log2bwtLength+
		           1+
		           1+
		           log2address );
	}


	protected void popTailPrime(Stream stack) { }


	protected void popHeadPrime(Stream stack) { }


	/**
	 * Sets $hasBeenExtended=true$ in the serialized representation of $v$ in $stack$,
	 * but not in this object. The pointer of $stack$ is then restored to its initial
	 * state.
	 */
	protected final void markAsExtended(Stream stack) {
		long backupPointer = stack.getPosition();
		stack.setBit(stackPointers[0]+log2address);
		stack.setPosition(backupPointer);
	}


	/**
	 * Sets $hasBeenStolen=true$ in the serialized representation of $v$ in $stack$,
	 * but not in this object. Then, the pointer of $stack$ is restored to its original
	 * state.
	 */
	protected final void markAsStolen(Stream stack) {
		long backupPointer = stack.getPosition();
		stack.setBit(stackPointers[0]+log2address+1);
		stack.setPosition(backupPointer);
	}

}








/**
	 * Same as $read$, but the procedure halts after reading the serialized substring up
	 * to $nIntervals$, and it leaves the stack position immediately after the end of the
	 * serialized substring.
	 */
/*	protected void readFast2(Stream stack) {
		stackPointers[0]=stack.getPosition();
		log2address=stackPointers[0]==0?MAX_BITS_PER_POINTER:Utils.bitsToEncode(stackPointers[0]);
		stackPointers[1]=stack.read(log2address);
		hasBeenExtended=stack.read(1)==1?true:false;
		hasBeenStolen=stack.read(1)==1?true:false;
		length=stack.read(log2bwtLength);
		firstCharacter=(int)stack.read(log2alphabetLength);
		nPointers=(int)stack.read(BITS_TO_ENCODE_MAX_POINTERS);
		nIntervals=(int)stack.read(BITS_TO_ENCODE_MAX_INTERVALS);
		stack.setPosition(stack.getPosition()+
						  (nPointers-MIN_POINTERS)*log2address+
						  nIntervals*log2bwtLength*2);
	}
*/


/**
	 * @return an \emph{upper bound} on the number of bits required to serialize any
	 * instance of this class.
	 */
/*	protected long serializedSize() {
		return MAX_BITS_PER_POINTER+
			   1+1+
			   log2bwtLength+
			   log2alphabetLength+
			   BITS_TO_ENCODE_MAX_POINTERS+
			   BITS_TO_ENCODE_MAX_INTERVALS+
			   (nPointers-MIN_POINTERS+1)*MAX_BITS_PER_POINTER+
			   (nIntervals<<1)*log2bwtLength;
	}
*/

/**
	 * Assume that the pointer in $stack$ is currently at the beginning of this substring
	 * $v$. The procedure advances the pointer to the beginning of the following substring
	 * while reading the minimum possible amount of information.
	 */
/*	protected void skip(Stream stack) {
		stackPointers[0]=stack.getPosition();
		log2address=stackPointers[0]==0?MAX_BITS_PER_POINTER:Utils.bitsToEncode(stackPointers[0]);
		stack.setPosition(stack.getPosition()+
						  log2address+
						  1+1+
						  log2bwtLength+
						  log2alphabetLength);
		nPointers=(int)stack.read(BITS_TO_ENCODE_MAX_POINTERS);
		nIntervals=(int)stack.read(BITS_TO_ENCODE_MAX_INTERVALS);
		stack.setPosition(stack.getPosition()+
						  (nPointers-MIN_POINTERS)*log2address+
						  nIntervals*log2bwtLength*2);
	}
*/

