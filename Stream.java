/**
 * Expanding and contracting stack of bits with random access.
 *
 * Remark: tradeoffs between occupied space and access time could be achieved by
 * transparently using ad hoc encodings of integers. Such developments are left to the
 * future.
 */
public class Stream {

	protected final int LONGS_PER_REGION;
	private final int LOG2_LONGS_PER_REGION;
	private final int LOG2_LONGS_PER_REGION_PLUS_SIX;
	private final int SIXTYFOUR_MINUS_LOG2_LONGS_PER_REGION;

	protected long[][] regions;
	private long nBits;  // Total number of bits in the stream
	protected int topRegion, topCell, topOffset;  // Top of the stack
	protected int pointerRegion, pointerCell, pointerOffset;  // Pointer to a bit in the stream


	/**
	 * @param longsPerRegion must be a power of two.
	 */
	public Stream(int longsPerRegion) {
		LONGS_PER_REGION=longsPerRegion;
		LOG2_LONGS_PER_REGION=Utils.log2(longsPerRegion);
		LOG2_LONGS_PER_REGION_PLUS_SIX=LOG2_LONGS_PER_REGION+6;
		SIXTYFOUR_MINUS_LOG2_LONGS_PER_REGION=64-LOG2_LONGS_PER_REGION;
		regions = new long[1][longsPerRegion];
	}


	public void clear() {
		regions = new long[1][LONGS_PER_REGION];
		topRegion=0; topCell=0; topOffset=0;
		nBits=0;
	}


	public void deallocate() {
		int nRegions = regions.length;
		for (int i=0; i<nRegions; i++) regions[i]=null;
		regions=null;
	}


	/**
	 * @return the number of bits in the stream
	 */
	public final long length() {
		return nBits;
	}




	// ------------------------------- STACK INTERFACE -----------------------------------

	/**
	 * Appends the $n$ least significant bits of $bits$ to the stack, possibly expanding
	 * it. Expansion implies: (1) doubling the size of $regions$; (2) copying
	 * $regions.length$ pointers (using the internalized procedure $System.arraycopy$);
	 * (3) increasing the number of occupied bits by one region.
	 */
	public final void push(long bits, int n) {
		final long lBits = bits&Utils.shiftOnesRight[64-n];
		int tmp = 64-topOffset;
		final int nRegions;
		long[] array = regions[topRegion];

		array[topCell]&=Utils.shiftOnesLeft[tmp];
		if (tmp>n) {
			array[topCell]|=lBits<<(tmp-n);
			topOffset+=n;
		}
		else {
			tmp=n-tmp;
			array[topCell]|=lBits>>>tmp;
			if (topCell+1<array.length) topCell++;
			else {
				nRegions=regions.length;
				if (topRegion+1==nRegions) {
					long[][] newRegions = new long[nRegions<<1][0];
					System.arraycopy(regions,0,newRegions,0,nRegions);
					regions=newRegions;
				}
				topRegion++;
				regions[topRegion] = new long[LONGS_PER_REGION];
				array=regions[topRegion];
				topCell=0;
			}
			array[topCell]=0L;
			array[topCell]|=lBits<<(64-tmp);
			topOffset=tmp;
		}
		nBits+=n;
	}


	/**
	 * Removes $n \leq 64$ bits from the top of the stack, possibly contracting it. The
	 * last region is immediately deallocated if unused.
	 *
	 * Remark: the procedure assumes that $n \leq nBits$.
	 * Remark: the random access pointer could be in an invalid position after $push$.
	 * This case is not explicitly checked.
	 */
	public final void pop(int n) {
		if (n<topOffset) topOffset-=n;
		else {
			if (topCell>0) topCell--;
			else {
				regions[topRegion]=null;
				topRegion--;
				topCell=LONGS_PER_REGION-1;
			}
			topOffset=64-n+topOffset;
		}
		nBits-=n;
	}




	// ------------------------------- STREAM INTERFACE ----------------------------------
	/**
	 * Positions the pointer to bit $bit<nBits$
	 */
	public final void setPosition(long bit) {
		pointerOffset=(int)(bit&Utils.shiftOnesRight[64-6]);
		bit>>>=6;
		pointerCell=(int)(bit&Utils.shiftOnesRight[SIXTYFOUR_MINUS_LOG2_LONGS_PER_REGION]);
		bit>>>=LOG2_LONGS_PER_REGION;
		pointerRegion=(int)bit;
	}


	private final void setPosition(int region, int cell, int offset) {
		pointerRegion=region;
		pointerCell=cell;
		pointerOffset=offset;
	}


	public final long getPosition() {
		final long lPointerRegion = pointerRegion<<LOG2_LONGS_PER_REGION_PLUS_SIX;
		final long lPointerCell = pointerCell<<6;
		return lPointerRegion|lPointerCell|pointerOffset;
	}


	/**
	 * Reads $n \leq 64$ bits and advances the pointer.
	 * Remark: The procedure assumes that there are at least $n$ bits to the right of the
	 * stream pointer: this is not explicitly checked.
	 */
	public final long read(int n) {
		int tmp = 64-pointerOffset;
		final int SIXTYFOUR_MINUS_N = 64-n;
		long out;
		long[] array = regions[pointerRegion];

		if (n<tmp) {
			out=array[pointerCell]>>>(tmp-n);
			pointerOffset+=n;
		}
		else if (n==tmp) {
			out=array[pointerCell];
			if (pointerCell+1<array.length) pointerCell++;
			else {
				pointerRegion++;
				array=regions[pointerRegion];
				pointerCell=0;
			}
			pointerOffset=0;
		}
		else {
			tmp=n-tmp;
			out=array[pointerCell]<<tmp;
			if (pointerCell+1<array.length) pointerCell++;
			else {
				pointerRegion++;
				array=regions[pointerRegion];
				pointerCell=0;
			}
			out|=array[pointerCell]>>>(64-tmp);
			pointerOffset=tmp;
		}
		return out&Utils.shiftOnesRight[SIXTYFOUR_MINUS_N];
	}


	/**
	 * Moves the pointer to $address$ and forces the corresponding bit to one
	 */
	public final void setBit(long address) {
		setPosition(address);
		regions[pointerRegion][pointerCell]|=Utils.oneSelectors1[64-pointerOffset-1];
	}

}






	/**
	 * Advances the pointer by $n \leq 64$ bits.
	 * Remark: the procedure assumes that there are at least $n$ bits to the right of the
	 * pointer.
	 */
/*	public final void skip(int n) {
		final int tmp = 64-pointerOffset;
		final int SIXTYFOUR_MINUS_N = 64-n;

		if (n<tmp) pointerOffset+=n;
		else {
			if (pointerCell+1<regions[pointerRegion].length) pointerCell++;
			else {
				pointerRegion++;
				pointerCell=0;
			}
			pointerOffset=n-tmp;
		}
	}*/


	/**
	 * Overwrites the $n$ least significant bits of $bits$ at the pointer, and advances
	 * the pointer.
	 */
	/*public final void overwrite(long bits, int n) {
		final int TMP = 64-pointerOffset;
		final int N_MINUS_TMP = n-TMP;
		final long LBITS = bits&Utils.shiftOnesRight[64-n];
		long mask;
		long[] array = regions[pointerRegion];

		mask=Utils.shiftOnesLeft[TMP];
		if (TMP>=n) mask|=Utils.shiftOnesRight[pointerOffset+n];
		array[pointerCell]&=mask;
		if (TMP>=n) array[pointerCell]|=LBITS<<(-N_MINUS_TMP);
		else {
			array[pointerCell]|=LBITS>>>N_MINUS_TMP;
			if (pointerCell+1<array.length) pointerCell++;
			else {
				pointerRegion++; pointerCell=0; pointerOffset=0;
				array=regions[pointerRegion];
			}
			array[pointerCell]&=Utils.shiftOnesRight[N_MINUS_TMP];
			array[pointerCell]|=LBITS<<(64-N_MINUS_TMP);
		}
	}*/