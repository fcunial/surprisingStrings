import java.util.Arrays;

/**
 * A right-maximal substring that computes its longest border from its suffix. See
 * \cite{apostolico2000efficient} for algorithms. This class provides subclasses with a
 * $Substring$ object that represents its longest border.
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
	 * A representation of set $left_v=\{(a,v|a) : a \in \Sigma\}$, sorted by increasing
	 * $a$, where $v|a$ is a pointer to the string $w$ in the stack such that $v=yadx$,
	 * $w=yad$, $v=zy$, and $v=yr$, where $x,y,z,r$ are strings and $d,a$ are characters.
	 * Specifically, $v|a$ is stored in $stackPointers[MIN_POINTERS+rightLength+i]$ if $a$
	 * is the $i$th character in $left_v$.
	 */
	protected int[] leftCharacters;
	protected int leftLength;  // Number of elements in $left_v$

	/**
	 * The longest border $y$ of $v$
	 */
	protected BorderSubstring longestBorder;

	/**
	 * The index in $rightCharacters$ of the character $d$ such that $v=xdy$,
	 * where $y$ is the longest border of $v$ and $x$ is a string.
	 * This number is pushed in the stack.
	 */
	protected int longestBorderRightCharacter;

	/**
	 * The index in $leftCharacters$ of the character $d$ such that $v=ydx$,
	 * where $y$ is the longest border of $v$ and $x$ is a string.
	 * This number is not pushed in the stack and it's not automatically computed when
	 * reading the stack. It is, however, computed by $init$.
	 */
	protected int longestBorderLeftCharacter;


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
		MAX_POINTERS=MIN_POINTERS+2*alphabetLength;
		BITS_TO_ENCODE_MAX_POINTERS=Utils.bitsToEncode(MAX_POINTERS);
		stackPointers = new long[MAX_POINTERS];
		rightCharacters = new int[alphabetLength];
		leftCharacters = new int[alphabetLength];
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
		stack.push(leftLength,log2alphabetLength);
		if (rightLength==0) return;
		stack.push(longestBorderRightCharacter,Utils.log2(rightLength));
		int i;
		for (i=0; i<rightLength; i++) stack.push(rightCharacters[i],log2alphabetLength);
		for (i=0; i<leftLength; i++) stack.push(leftCharacters[i],log2alphabetLength);
	}


	protected void read(Stream stack) {
		super.read(stack);
		rightLength=(int)stack.read(log2alphabetLength);
		leftLength=(int)stack.read(log2alphabetLength);
		readBorderSubstring(stack);
	}


	private void readBorderSubstring(Stream stack) {
		if (rightLength==0) {
			longestBorderRightCharacter=-1;
			leftLength=0;
			return;
		}
		longestBorderRightCharacter=(int)stack.read(Utils.log2(rightLength));
		int i;
		for (i=0; i<rightLength; i++) rightCharacters[i]=(int)stack.read(log2alphabetLength);
		for (i=0; i<leftLength; i++) leftCharacters[i]=(int)stack.read(log2alphabetLength);
	}


	protected void readFast(Stream stack) {
		super.readFast(stack);
		rightLength=(int)stack.read(log2alphabetLength);
		leftLength=(int)stack.read(log2alphabetLength);
		if (hasBeenExtended||hasBeenStolen) skipBorderSubstring(stack);
		else readBorderSubstring(stack);
	}


	protected void skip(Stream stack) {
		super.skip(stack);
		rightLength=(int)stack.read(log2alphabetLength);
		leftLength=(int)stack.read(log2alphabetLength);
		skipBorderSubstring(stack);
	}


	private void skipBorderSubstring(Stream stack) {
		if (rightLength>0) stack.setPosition(stack.getPosition()+
											 Utils.log2(rightLength)+
											 rightLength*log2alphabetLength+
											 leftLength*log2alphabetLength);
	}


	protected void pop(Stream stack) {
		popBorderSubstring(stack);
		super.pop(stack);
	}


	private void popBorderSubstring(Stream stack) {
		long x = rightLength>0?rightLength*log2alphabetLength+leftLength*log2alphabetLength+Utils.log2(rightLength):0;
		stack.pop(x+
		          log2alphabetLength+
		          log2alphabetLength);
	}



/*                       ______               _
                         | ___ \             | |
                         | |_/ / ___  _ __ __| | ___ _ __ ___
                         | ___ \/ _ \| '__/ _` |/ _ \ '__/ __|
                         | |_/ / (_) | | | (_| |  __/ |  \__ \
                         \____/ \___/|_|  \__,_|\___|_|  |___/                          */
	/**
	 * This procedure is IO-bound: it just deserializes a region of $stack$ that is very
	 * close to the bottom, and it copies arrays.
	 *
	 * Remark: we expect most longest borders to be short, thus the \emph{bottom} of
	 * $stack$ will be accessed frequently. Hopefully the processor will cache such region.
	 * Otherwise, we should create a software cache of frequently used $BorderSubstring$
	 * objects.
	 */
	protected void init(Substring suffix, int firstCharacter, Stream stack, RigidStream characterStack, SimpleStream pointerStack, long[] buffer) {
		super.init(suffix,firstCharacter,stack,characterStack,pointerStack,buffer);
		rightLength=0;
		longestBorderRightCharacter=-1;
		longestBorderLeftCharacter=-1;
		if (length==1 || firstCharacter==-1 || rightContext==1) return;  // We don't compute borders for left-extensions that are not right-maximal.
		int pos = (int)buffer[firstCharacter];
		if (pos==-1) initFromSuffixWithoutBorder(firstCharacter,suffix,stack,characterStack,pointerStack);
		else initFromSuffixWithBorder((BorderSubstring)suffix,pos,stack,characterStack,pointerStack);
		initLeftCharacters(characterStack,pointerStack);
	}


	/**
	 * Initializes the right array of $v$ and the longest border of $v$ when the
	 * one-character suffix of $v$ has no border preceded by $firstCharacter$.
	 * We assume $|v|>1$.
	 */
	private final void initFromSuffixWithoutBorder(int firstCharacter, Substring suffix, Stream stack, RigidStream characterStack, SimpleStream pointerStack) {
		int lastCharacter, d;
		long pointer, backupPointer;

		if (length==2) lastCharacter=suffix.firstCharacter;
		else lastCharacter = (int)(characterStack.getElementAt(0));  // Last character of $v$
		if (lastCharacter==firstCharacter) {
			longestBorderRightCharacter=0;
			rightLength=1;
			nPointers=MIN_POINTERS+1;
			if (length==2) {
				if (longestBorder==null) longestBorder=(BorderSubstring)getInstance();  // Executed only once
				backupPointer=stack.getPosition();
				stack.setPosition(suffix.stackPointers[0]);
				longestBorder.read(stack);
				stack.setPosition(backupPointer);
				d=firstCharacter;
				pointer=-1;  // -1 will be converted to $stackPointers[0]$ by $Substring.push$
			}
			else {
				if (longestBorder==null) longestBorder=(BorderSubstring)getInstance();  // Executed only once
				backupPointer=stack.getPosition();
				stack.setPosition(pointerStack.getElementAt(0));
				longestBorder.read(stack);
				stack.setPosition(backupPointer);
				d=(int)(characterStack.getElementAt(1));  // Character that precedes the longest border of $v$
				pointer=pointerStack.getElementAt(1);
			}
			rightCharacters[0]=d;
			stackPointers[MIN_POINTERS]=pointer;
		}
	}


	/**
	 * Initializes the right array of $v$ and the longest border of $v$ when the
	 * one-character suffix of $v$ has a border preceded by $firstCharacter$.
	 * The running time of this procedure is linear on the length of the right array of
	 * the longest border of $v$, and it does not depend on $alphabetLength$: this makes
	 * the sum of the initialization times of all right-maximal substrings of a text $s$
	 * linear in the length of $s$.
	 *
	 * @param pos position of the first character of $v$ in $suffix.rightCharacters$.
	 */
	private final void initFromSuffixWithBorder(BorderSubstring suffix, int pos, Stream stack, RigidStream characterStack, SimpleStream pointerStack) {
		int i, d, start;
		long pointer, backupPointer, longestBorderPointer, longestBorderLength;

		// Loading longest border and preceding character
		if (longestBorder==null) longestBorder=(BorderSubstring)getInstance();  // Executed only once
		backupPointer=stack.getPosition();
		longestBorderPointer=suffix.stackPointers[MIN_POINTERS+pos];
		stack.setPosition(longestBorderPointer);
		longestBorder.read(stack);
		longestBorderLength=longestBorder.length;
		if (longestBorderLength==length-1) {
			d=firstCharacter;
			pointer=-1;  // -1 will be converted to $stackPointers[0]$ by $Substring.push$
		}
		else {
			d=(int)(characterStack.getElementAt(longestBorderLength));  // Character that precedes the longest border of $v$
			pointer=pointerStack.getElementAt(longestBorderLength);
		}
		stack.setPosition(backupPointer);

		// Building array $right$
		if (longestBorder.rightLength==0) {
			rightLength=1;
			longestBorderRightCharacter=0;
			rightCharacters[0]=d;
			nPointers=MIN_POINTERS+1;
			stackPointers[MIN_POINTERS]=pointer;
			return;
		}
		longestBorderRightCharacter=Arrays.binarySearch(longestBorder.rightCharacters,0,longestBorder.rightLength,d);
		if (longestBorderRightCharacter>=0) {
			rightLength=longestBorder.rightLength;
			System.arraycopy(longestBorder.rightCharacters,0,rightCharacters,0,rightLength);
			nPointers=MIN_POINTERS+rightLength;
			System.arraycopy(longestBorder.stackPointers,MIN_POINTERS,stackPointers,MIN_POINTERS,rightLength);
			stackPointers[MIN_POINTERS+longestBorderRightCharacter]=pointer;
		}
		else {
			rightLength=longestBorder.rightLength+1;
			longestBorderRightCharacter=-longestBorderRightCharacter-1;
			System.arraycopy(longestBorder.rightCharacters,0,rightCharacters,0,longestBorderRightCharacter);
			rightCharacters[longestBorderRightCharacter]=d;
			System.arraycopy(longestBorder.rightCharacters,longestBorderRightCharacter,rightCharacters,longestBorderRightCharacter+1,rightLength-longestBorderRightCharacter-1);
			nPointers=MIN_POINTERS+rightLength;
			System.arraycopy(longestBorder.stackPointers,MIN_POINTERS,stackPointers,MIN_POINTERS,longestBorderRightCharacter);
			stackPointers[MIN_POINTERS+longestBorderRightCharacter]=pointer;
			System.arraycopy(longestBorder.stackPointers,MIN_POINTERS+longestBorderRightCharacter,stackPointers,MIN_POINTERS+longestBorderRightCharacter+1,rightLength-longestBorderRightCharacter-1);
		}
	}


	/**
	 * Initializes the left array of $v$ from the longest border of $v$.
	 */
	private final void initLeftCharacters(RigidStream characterStack, SimpleStream pointerStack) {
		if (rightLength==0) {
			leftLength=0;
			return;
		}
		int d, longestBorderStart, start;
		long longestBorderLength, pointer, k;

		// Computing preceding character and pointer
		longestBorderLength=longestBorder.length;
		if (longestBorderLength==length-1) {
			d=(int)(characterStack.getElementAt(0));
			pointer=-1;  // -1 will be converted to $stackPointers[0]$ by $Substring.push$
		}
		else {
			k=length-longestBorderLength-1;
			d=(int)(characterStack.getElementAt(k));  // Character that follows the longest border of $v$
			pointer=pointerStack.getElementAt(k);
		}

		// Building array $left$
		if (longestBorder.leftLength==0) {
			leftLength=1;
			longestBorderLeftCharacter=0;
			leftCharacters[0]=d;
			nPointers++;
			stackPointers[nPointers-1]=pointer;
			return;
		}
		longestBorderLeftCharacter=Arrays.binarySearch(longestBorder.leftCharacters,0,longestBorder.leftLength,d);
		longestBorderStart=MIN_POINTERS+longestBorder.rightLength;
		start=MIN_POINTERS+rightLength;
		if (longestBorderLeftCharacter>=0) {
			leftLength=longestBorder.leftLength;
			System.arraycopy(longestBorder.leftCharacters,0,leftCharacters,0,leftLength);
			nPointers+=leftLength;
			System.arraycopy(longestBorder.stackPointers,longestBorderStart,stackPointers,start,leftLength);
			stackPointers[start+longestBorderLeftCharacter]=pointer;
		}
		else {
			leftLength=longestBorder.leftLength+1;
			longestBorderLeftCharacter=-longestBorderLeftCharacter-1;
			System.arraycopy(longestBorder.leftCharacters,0,leftCharacters,0,longestBorderLeftCharacter);
			leftCharacters[longestBorderLeftCharacter]=d;
			System.arraycopy(longestBorder.leftCharacters,longestBorderLeftCharacter,leftCharacters,longestBorderLeftCharacter+1,leftLength-longestBorderLeftCharacter-1);
			nPointers+=leftLength;
			System.arraycopy(longestBorder.stackPointers,longestBorderStart,stackPointers,start,longestBorderLeftCharacter);
			stackPointers[start+longestBorderLeftCharacter]=pointer;
			System.arraycopy(longestBorder.stackPointers,longestBorderStart+longestBorderLeftCharacter,stackPointers,start+longestBorderLeftCharacter+1,leftLength-longestBorderLeftCharacter-1);
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

/*
    protected void readFast2(Stream stack) {
		super.readFast2(stack);
		rightLength=(int)stack.read(log2alphabetLength);
		leftLength=(int)stack.read(log2alphabetLength);
		skipBorderSubstring(stack);
	}

	protected long serializedSize() {
		return super.serializedSize()+
			   log2alphabetLength+
			   log2alphabetLength+
			   log2alphabetLength+
			   alphabetLength*log2alphabetLength*2;
	}
*/

