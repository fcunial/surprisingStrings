import java.util.Arrays;

/**
 * A right-maximal substring that can compute border information from its suffixes. See
 * \cite{apostolico2000efficient} for algorithms.
 */
public class BorderSubstring extends RightMaximalSubstring {
	/**
	 * A representation of set $right_v=\{(a,a|v) : a \in \Sigma\}$, sorted by increasing
	 * $a$, where $a|v$ is a pointer to the string $w$ in the stack such that $v=xday$,
	 * $w=day$, $v=yz$, and $v=ry$, where $x,y,z,r$ are strings and $d,a$ are characters.
	 * Specifically, $a|v$ is stored in $stackPointers[MIN_POINTERS+i]$ if $a$ is the
	 * $i$th character in $right_v$.
	 */
	protected int[] rightCharacters;
	protected int rightLength;  // Number of elements in $right_v$

	/**
	 * Information about the longest border $y$: length, and the index in $rightCharacters$
	 * of the character $d$ such that $v=xdy$, where $x$ is a string.
	 */
	protected long longestBorderLength;
	protected int longestBorderCharacter;


	/**
	 * Artificial no-argument constructor, used just to avoid compile-time errors.
	 * See the no-argument constructor of $Substring$ for details.
	 */
	protected BorderSubstring() { }


	public BorderSubstring(int alphabetLength, int log2alphabetLength, long bwtLength, int log2bwtLength) {
		this.alphabetLength=alphabetLength;
		this.log2alphabetLength=log2alphabetLength;
		this.bwtLength=bwtLength;
		this.log2bwtLength=log2bwtLength;
		MAX_INTERVALS=alphabetLength+1;
		BITS_TO_ENCODE_MAX_INTERVALS=Utils.bitsToEncode(MAX_INTERVALS);
		BWT_INTERVALS_ARE_SORTED=true;
		bwtIntervals = new long[MAX_INTERVALS][2];
		MAX_POINTERS=MIN_POINTERS+alphabetLength;
		BITS_TO_ENCODE_MAX_POINTERS=Utils.bitsToEncode(MAX_POINTERS);
		stackPointers = new long[MAX_POINTERS];
		rightCharacters = new int[alphabetLength];
	}


	protected Substring getInstance() {
		return new BorderSubstring(alphabetLength,log2alphabetLength,bwtLength,log2bwtLength);
	}


	protected void push(Stream stack) {
		super.push(stack);
		pushBorderSubstring(stack);
	}


	private void pushBorderSubstring(Stream stack) {
		stack.push(rightLength,log2alphabetLength);
		if (rightLength==0) return;
		stack.push(longestBorderCharacter,Utils.log2(rightLength));
		for (int i=0; i<rightLength; i++) stack.push(rightCharacters[i],log2alphabetLength);
	}


	protected long serializedSize() {
		return super.serializedSize()+
			   log2alphabetLength+
			   log2alphabetLength+
			   alphabetLength*log2alphabetLength;
	}


	protected void read(Stream stack) {
		super.read(stack);
		rightLength=(int)stack.read(log2alphabetLength);
		readBorderSubstring(stack);
	}


	private void readBorderSubstring(Stream stack) {
		if (rightLength==0) {
			longestBorderCharacter=-1;
			return;
		}
		longestBorderCharacter=(int)stack.read(Utils.log2(rightLength));
		for (int i=0; i<rightLength; i++) rightCharacters[i]=(int)stack.read(log2alphabetLength);
	}


	protected void readFast(Stream stack) {
		super.readFast(stack);
		rightLength=(int)stack.read(log2alphabetLength);
		if (hasBeenExtended||hasBeenStolen) skipBorderSubstring(stack);
		else readBorderSubstring(stack);
	}


	protected void readFast2(Stream stack) {
		super.readFast2(stack);
		rightLength=(int)stack.read(log2alphabetLength);
		skipBorderSubstring(stack);
	}


	private void skipBorderSubstring(Stream stack) {
		if (rightLength>0) stack.setPosition(stack.getPosition()+
											 Utils.log2(rightLength)+
											 rightLength*log2alphabetLength);
	}


	protected void pop(Stream stack) {
		popBorderSubstring(stack);
		super.pop(stack);
	}


	private void popBorderSubstring(Stream stack) {
		long x = rightLength>0?rightLength*log2alphabetLength+Utils.log2(rightLength):0;
		stack.pop(x+
		          log2alphabetLength);
	}


	/**
	 * This procedure is IO-bound: it just deserializes a region of $stack$ that is very
	 * close to the bottom, and it copies arrays.
	 *
	 * Remark: we expect most longest borders to be short, thus the \emph{bottom} of
	 * $stack$ will be accessed frequently. Hopefully the processor will cache such region.
	 * Otherwise, we should create a software cache of frequently used $BorderSubstring$
	 * objects.
	 */
	protected void init(Substring suffix, int firstCharacter, Stream stack, long[] buffer) {
		super.init(suffix,firstCharacter,stack,buffer);
		longestBorderLength=0;
		rightLength=0;
		longestBorderCharacter=-1;
		if (length==1 || firstCharacter==-1 || rightContext==1) return;  // We don't compute borders for left-extensions that are not right-maximal.
		int pos = (int)buffer[firstCharacter];
		if (pos==-1) initFromSuffixWithoutBorder(firstCharacter,suffix,stack);
		else initFromSuffixWithBorder((BorderSubstring)suffix,pos,stack);
	}


	/**
	 * Handles the initialization of $v$ when the one-character suffix of $v$ has no
	 * border preceded by $firstCharacter$.
	 */
	protected final void initFromSuffixWithoutBorder(int firstCharacter, Substring suffix, Stream stack) {
		int lastCharacter, d;
		long pointer, backupPointer;
		Substring tmpString = getInstance();

		backupPointer=stack.getPosition();
		stack.setPosition(0);
		tmpString.readFast2(stack);  // Artificial bottom of the stack
		tmpString.readFast2(stack);  // $\epsilon$
		do { tmpString.readFast2(stack); }
		while (!tmpString.hasBeenExtended && tmpString.stackPointers[0]!=suffix.stackPointers[0]);
		lastCharacter=tmpString.firstCharacter;  // Last character of $v$
		if (lastCharacter==firstCharacter) {
			longestBorderLength=1;
			longestBorderCharacter=0;
			rightLength=1;
			nPointers=MIN_POINTERS+1;
			if (tmpString.stackPointers[0]==suffix.stackPointers[0]) {
				d=firstCharacter;
				pointer=-1;  // -1 will be converted to $stackPointers[0]$ by $Substring.push$
			}
			else {
				do { tmpString.readFast2(stack); }
				while (!tmpString.hasBeenExtended && tmpString.stackPointers[0]!=suffix.stackPointers[0]);
				d=tmpString.firstCharacter;  // Character that precedes the longest border of $v$
				pointer=tmpString.stackPointers[0];
			}
			rightCharacters[0]=d;
			stackPointers[MIN_POINTERS]=pointer;
		}
		stack.setPosition(backupPointer);
	}


	/**
	 * Handles the initialization of $v$ when the one-character suffix of $v$ has a border
	 * preceded by $firstCharacter$. The running time of this procedure is linear on the
	 * length of the right array of the longest border of $v$, and it does not depend on
	 * $alphabetLength$: this makes the sum of the initialization times of all
	 * right-maximal substrings of a text $s$ linear in the length of $s$.
	 *
	 * @param pos position of the first character of $v$ in $suffix.rightCharacters$.
	 */
	protected final void initFromSuffixWithBorder(BorderSubstring suffix, int pos, Stream stack) {
		int i, d, start;
		long pointer, backupPointer;
		BorderSubstring longestBorder = (BorderSubstring)getInstance();
		Substring tmpString = getInstance();

		// Loading longest border and preceding character
		backupPointer=stack.getPosition();
		stack.setPosition(suffix.stackPointers[MIN_POINTERS+pos]);
		longestBorder.read(stack);
		longestBorderLength=longestBorder.length;
		if (longestBorder.stackPointers[0]==suffix.stackPointers[0]) {
			d=firstCharacter;
			pointer=-1;  // -1 will be converted to $stackPointers[0]$ by $Substring.push$
		}
		else {
			do { tmpString.readFast2(stack); }
			while (!tmpString.hasBeenExtended && tmpString.stackPointers[0]!=suffix.stackPointers[0]);
			d=tmpString.firstCharacter;  // Character that precedes the longest border of $v$
			pointer=tmpString.stackPointers[0];
		}
		stack.setPosition(backupPointer);

		// Building array $right$
		if (longestBorder.rightLength==0) {
			rightLength=1;
			longestBorderCharacter=0;
			rightCharacters[0]=d;
			nPointers=MIN_POINTERS+1;
			stackPointers[MIN_POINTERS]=pointer;
			return;
		}
		longestBorderCharacter=Arrays.binarySearch(longestBorder.rightCharacters,0,longestBorder.rightLength,d);
		if (longestBorderCharacter>=0) {
			rightLength=longestBorder.rightLength;
			System.arraycopy(longestBorder.rightCharacters,0,rightCharacters,0,rightLength);
			nPointers=MIN_POINTERS+rightLength;
			System.arraycopy(longestBorder.stackPointers,MIN_POINTERS,stackPointers,MIN_POINTERS,rightLength);
			stackPointers[MIN_POINTERS+longestBorderCharacter]=pointer;
		}
		else {
			rightLength=longestBorder.rightLength+1;
			longestBorderCharacter=-longestBorderCharacter-1;
			System.arraycopy(longestBorder.rightCharacters,0,rightCharacters,0,longestBorderCharacter);
			rightCharacters[longestBorderCharacter]=d;
			System.arraycopy(longestBorder.rightCharacters,longestBorderCharacter,rightCharacters,longestBorderCharacter+1,rightLength-longestBorderCharacter-1);
			nPointers=MIN_POINTERS+rightLength;
			System.arraycopy(longestBorder.stackPointers,MIN_POINTERS,stackPointers,MIN_POINTERS,longestBorderCharacter);
			stackPointers[MIN_POINTERS+longestBorderCharacter]=pointer;
			System.arraycopy(longestBorder.stackPointers,MIN_POINTERS+longestBorderCharacter,stackPointers,MIN_POINTERS+longestBorderCharacter+1,rightLength-longestBorderCharacter-1);
		}
	}


	/**
	 * $buffer$ is used to map a character of the alphabet $[0..\alphabetLength-1]$ to its
	 * position in $rightCharacters$.
	 */
	protected void fillBuffer(long[] buffer) {
		for (int i=0; i<rightLength; i++) buffer[rightCharacters[i]]=i;
	}


	protected void emptyBuffer(long[] buffer) {
		for (int i=0; i<rightLength; i++) buffer[rightCharacters[i]]=-1;
	}

}