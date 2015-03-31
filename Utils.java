import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Remark: 0xFFFFFFFFFFFFFFFFL>>>64=0xFFFFFFFFFFFFFFFFL in Java.
 * Remark: 0xFFFFFFFFFFFFFFFFL <<64=0xFFFFFFFFFFFFFFFFL in Java.
 * Remark: all integers and longs are signed in Java.
 */
public class Utils {
	/**
	 * State of the xorshift pseudorandom number generator
	 */
	private static long INCREASING_LONG = 0L;
	private static long RANDOM = 0L;

	public static final int LAST_6_BITS = 0x3F;
	public static final int MSB_INT_ONE = 0x80000000;
	public static final int MSB_INT_ZERO = 0x7FFFFFFF;

	/**
	 * All possible left- and right-shifts of $0xFFFFFFFFFFFFFFFF$
	 */
	public static final long[] shiftOnesRight = { 0xFFFFFFFFFFFFFFFFL,0x7FFFFFFFFFFFFFFFL,0x3FFFFFFFFFFFFFFFL,0x1FFFFFFFFFFFFFFFL,0xFFFFFFFFFFFFFFFL,0x7FFFFFFFFFFFFFFL,0x3FFFFFFFFFFFFFFL,0x1FFFFFFFFFFFFFFL,0xFFFFFFFFFFFFFFL,0x7FFFFFFFFFFFFFL,0x3FFFFFFFFFFFFFL,0x1FFFFFFFFFFFFFL,0xFFFFFFFFFFFFFL,0x7FFFFFFFFFFFFL,0x3FFFFFFFFFFFFL,0x1FFFFFFFFFFFFL,0xFFFFFFFFFFFFL,0x7FFFFFFFFFFFL,0x3FFFFFFFFFFFL,0x1FFFFFFFFFFFL,0xFFFFFFFFFFFL,0x7FFFFFFFFFFL,0x3FFFFFFFFFFL,0x1FFFFFFFFFFL,0xFFFFFFFFFFL,0x7FFFFFFFFFL,0x3FFFFFFFFFL,0x1FFFFFFFFFL,0xFFFFFFFFFL,0x7FFFFFFFFL,0x3FFFFFFFFL,0x1FFFFFFFFL,0xFFFFFFFFL,0x7FFFFFFFL,0x3FFFFFFFL,0x1FFFFFFFL,0xFFFFFFFL,0x7FFFFFFL,0x3FFFFFFL,0x1FFFFFFL,0xFFFFFFL,0x7FFFFFL,0x3FFFFFL,0x1FFFFFL,0xFFFFFL,0x7FFFFL,0x3FFFFL,0x1FFFFL,0xFFFFL,0x7FFFL,0x3FFFL,0x1FFFL,0xFFFL,0x7FFL,0x3FFL,0x1FFL,0xFFL,0x7FL,0x3FL,0x1FL,0xFL,0x7L,0x3L,0x1L,0x0000000000000000L };
	public static final long[] shiftOnesLeft = { 0xFFFFFFFFFFFFFFFFL,0xFFFFFFFFFFFFFFFEL,0xFFFFFFFFFFFFFFFCL,0xFFFFFFFFFFFFFFF8L,0xFFFFFFFFFFFFFFF0L,0xFFFFFFFFFFFFFFE0L,0xFFFFFFFFFFFFFFC0L,0xFFFFFFFFFFFFFF80L,0xFFFFFFFFFFFFFF00L,0xFFFFFFFFFFFFFE00L,0xFFFFFFFFFFFFFC00L,0xFFFFFFFFFFFFF800L,0xFFFFFFFFFFFFF000L,0xFFFFFFFFFFFFE000L,0xFFFFFFFFFFFFC000L,0xFFFFFFFFFFFF8000L,0xFFFFFFFFFFFF0000L,0xFFFFFFFFFFFE0000L,0xFFFFFFFFFFFC0000L,0xFFFFFFFFFFF80000L,0xFFFFFFFFFFF00000L,0xFFFFFFFFFFE00000L,0xFFFFFFFFFFC00000L,0xFFFFFFFFFF800000L,0xFFFFFFFFFF000000L,0xFFFFFFFFFE000000L,0xFFFFFFFFFC000000L,0xFFFFFFFFF8000000L,0xFFFFFFFFF0000000L,0xFFFFFFFFE0000000L,0xFFFFFFFFC0000000L,0xFFFFFFFF80000000L,0xFFFFFFFF00000000L,0xFFFFFFFE00000000L,0xFFFFFFFC00000000L,0xFFFFFFF800000000L,0xFFFFFFF000000000L,0xFFFFFFE000000000L,0xFFFFFFC000000000L,0xFFFFFF8000000000L,0xFFFFFF0000000000L,0xFFFFFE0000000000L,0xFFFFFC0000000000L,0xFFFFF80000000000L,0xFFFFF00000000000L,0xFFFFE00000000000L,0xFFFFC00000000000L,0xFFFF800000000000L,0xFFFF000000000000L,0xFFFE000000000000L,0xFFFC000000000000L,0xFFF8000000000000L,0xFFF0000000000000L,0xFFE0000000000000L,0xFFC0000000000000L,0xFF80000000000000L,0xFF00000000000000L,0xFE00000000000000L,0xFC00000000000000L,0xF800000000000000L,0xF000000000000000L,0xE000000000000000L,0xC000000000000000L,0x8000000000000000L,0x0000000000000000L };

	/**
	 * All possible left-shifts of a block of length $2^k$ in a long, $0 \leq k < 6$.
	 * Either the bits in the block are ones and the bits out of the block are zeros
	 * ($oneSelectors$), or the bits in the block are zeros and the bits out of the block
	 * are ones ($zeroSelectors$). Left-shifts are by a single bit in order to avoid shift
	 * operations when addressing the arrays themselves.
	 */
	public static final long[] oneSelectors1 = { 0x1L,0x2L,0x4L,0x8L,0x10L,0x20L,0x40L,0x80L,0x100L,0x200L,0x400L,0x800L,0x1000L,0x2000L,0x4000L,0x8000L,0x10000L,0x20000L,0x40000L,0x80000L,0x100000L,0x200000L,0x400000L,0x800000L,0x1000000L,0x2000000L,0x4000000L,0x8000000L,0x10000000L,0x20000000L,0x40000000L,0x80000000L,0x100000000L,0x200000000L,0x400000000L,0x800000000L,0x1000000000L,0x2000000000L,0x4000000000L,0x8000000000L,0x10000000000L,0x20000000000L,0x40000000000L,0x80000000000L,0x100000000000L,0x200000000000L,0x400000000000L,0x800000000000L,0x1000000000000L,0x2000000000000L,0x4000000000000L,0x8000000000000L,0x10000000000000L,0x20000000000000L,0x40000000000000L,0x80000000000000L,0x100000000000000L,0x200000000000000L,0x400000000000000L,0x800000000000000L,0x1000000000000000L,0x2000000000000000L,0x4000000000000000L,0x8000000000000000L };
	public static final long[] oneSelectors2 = { 0x3L,0x6L,0xCL,0x18L,0x30L,0x60L,0xC0L,0x180L,0x300L,0x600L,0xC00L,0x1800L,0x3000L,0x6000L,0xC000L,0x18000L,0x30000L,0x60000L,0xC0000L,0x180000L,0x300000L,0x600000L,0xC00000L,0x1800000L,0x3000000L,0x6000000L,0xC000000L,0x18000000L,0x30000000L,0x60000000L,0xC0000000L,0x180000000L,0x300000000L,0x600000000L,0xC00000000L,0x1800000000L,0x3000000000L,0x6000000000L,0xC000000000L,0x18000000000L,0x30000000000L,0x60000000000L,0xC0000000000L,0x180000000000L,0x300000000000L,0x600000000000L,0xC00000000000L,0x1800000000000L,0x3000000000000L,0x6000000000000L,0xC000000000000L,0x18000000000000L,0x30000000000000L,0x60000000000000L,0xC0000000000000L,0x180000000000000L,0x300000000000000L,0x600000000000000L,0xC00000000000000L,0x1800000000000000L,0x3000000000000000L,0x6000000000000000L,0xC000000000000000L };
	public static final long[] oneSelectors4 = { 0xFL,0x1EL,0x3CL,0x78L,0xF0L,0x1E0L,0x3C0L,0x780L,0xF00L,0x1E00L,0x3C00L,0x7800L,0xF000L,0x1E000L,0x3C000L,0x78000L,0xF0000L,0x1E0000L,0x3C0000L,0x780000L,0xF00000L,0x1E00000L,0x3C00000L,0x7800000L,0xF000000L,0x1E000000L,0x3C000000L,0x78000000L,0xF0000000L,0x1E0000000L,0x3C0000000L,0x780000000L,0xF00000000L,0x1E00000000L,0x3C00000000L,0x7800000000L,0xF000000000L,0x1E000000000L,0x3C000000000L,0x78000000000L,0xF0000000000L,0x1E0000000000L,0x3C0000000000L,0x780000000000L,0xF00000000000L,0x1E00000000000L,0x3C00000000000L,0x7800000000000L,0xF000000000000L,0x1E000000000000L,0x3C000000000000L,0x78000000000000L,0xF0000000000000L,0x1E0000000000000L,0x3C0000000000000L,0x780000000000000L,0xF00000000000000L,0x1E00000000000000L,0x3C00000000000000L,0x7800000000000000L,0xF000000000000000L };
	public static final long[] oneSelectors8 = { 0xFFL,0x1FEL,0x3FCL,0x7F8L,0xFF0L,0x1FE0L,0x3FC0L,0x7F80L,0xFF00L,0x1FE00L,0x3FC00L,0x7F800L,0xFF000L,0x1FE000L,0x3FC000L,0x7F8000L,0xFF0000L,0x1FE0000L,0x3FC0000L,0x7F80000L,0xFF00000L,0x1FE00000L,0x3FC00000L,0x7F800000L,0xFF000000L,0x1FE000000L,0x3FC000000L,0x7F8000000L,0xFF0000000L,0x1FE0000000L,0x3FC0000000L,0x7F80000000L,0xFF00000000L,0x1FE00000000L,0x3FC00000000L,0x7F800000000L,0xFF000000000L,0x1FE000000000L,0x3FC000000000L,0x7F8000000000L,0xFF0000000000L,0x1FE0000000000L,0x3FC0000000000L,0x7F80000000000L,0xFF00000000000L,0x1FE00000000000L,0x3FC00000000000L,0x7F800000000000L,0xFF000000000000L,0x1FE000000000000L,0x3FC000000000000L,0x7F8000000000000L,0xFF0000000000000L,0x1FE0000000000000L,0x3FC0000000000000L,0x7F80000000000000L,0xFF00000000000000L };
	public static final long[] oneSelectors16 = { 0xFFFFL,0x1FFFEL,0x3FFFCL,0x7FFF8L,0xFFFF0L,0x1FFFE0L,0x3FFFC0L,0x7FFF80L,0xFFFF00L,0x1FFFE00L,0x3FFFC00L,0x7FFF800L,0xFFFF000L,0x1FFFE000L,0x3FFFC000L,0x7FFF8000L,0xFFFF0000L,0x1FFFE0000L,0x3FFFC0000L,0x7FFF80000L,0xFFFF00000L,0x1FFFE00000L,0x3FFFC00000L,0x7FFF800000L,0xFFFF000000L,0x1FFFE000000L,0x3FFFC000000L,0x7FFF8000000L,0xFFFF0000000L,0x1FFFE0000000L,0x3FFFC0000000L,0x7FFF80000000L,0xFFFF00000000L,0x1FFFE00000000L,0x3FFFC00000000L,0x7FFF800000000L,0xFFFF000000000L,0x1FFFE000000000L,0x3FFFC000000000L,0x7FFF8000000000L,0xFFFF0000000000L,0x1FFFE0000000000L,0x3FFFC0000000000L,0x7FFF80000000000L,0xFFFF00000000000L,0x1FFFE00000000000L,0x3FFFC00000000000L,0x7FFF800000000000L,0xFFFF000000000000L };
	public static final long[] oneSelectors32 = { 0xFFFFFFFFL,0x1FFFFFFFEL,0x3FFFFFFFCL,0x7FFFFFFF8L,0xFFFFFFFF0L,0x1FFFFFFFE0L,0x3FFFFFFFC0L,0x7FFFFFFF80L,0xFFFFFFFF00L,0x1FFFFFFFE00L,0x3FFFFFFFC00L,0x7FFFFFFF800L,0xFFFFFFFF000L,0x1FFFFFFFE000L,0x3FFFFFFFC000L,0x7FFFFFFF8000L,0xFFFFFFFF0000L,0x1FFFFFFFE0000L,0x3FFFFFFFC0000L,0x7FFFFFFF80000L,0xFFFFFFFF00000L,0x1FFFFFFFE00000L,0x3FFFFFFFC00000L,0x7FFFFFFF800000L,0xFFFFFFFF000000L,0x1FFFFFFFE000000L,0x3FFFFFFFC000000L,0x7FFFFFFF8000000L,0xFFFFFFFF0000000L,0x1FFFFFFFE0000000L,0x3FFFFFFFC0000000L,0x7FFFFFFF80000000L,0xFFFFFFFF00000000L };
	public static final long[] zeroSelectors1 = { 0xFFFFFFFFFFFFFFFEL,0xFFFFFFFFFFFFFFFDL,0xFFFFFFFFFFFFFFFBL,0xFFFFFFFFFFFFFFF7L,0xFFFFFFFFFFFFFFEFL,0xFFFFFFFFFFFFFFDFL,0xFFFFFFFFFFFFFFBFL,0xFFFFFFFFFFFFFF7FL,0xFFFFFFFFFFFFFEFFL,0xFFFFFFFFFFFFFDFFL,0xFFFFFFFFFFFFFBFFL,0xFFFFFFFFFFFFF7FFL,0xFFFFFFFFFFFFEFFFL,0xFFFFFFFFFFFFDFFFL,0xFFFFFFFFFFFFBFFFL,0xFFFFFFFFFFFF7FFFL,0xFFFFFFFFFFFEFFFFL,0xFFFFFFFFFFFDFFFFL,0xFFFFFFFFFFFBFFFFL,0xFFFFFFFFFFF7FFFFL,0xFFFFFFFFFFEFFFFFL,0xFFFFFFFFFFDFFFFFL,0xFFFFFFFFFFBFFFFFL,0xFFFFFFFFFF7FFFFFL,0xFFFFFFFFFEFFFFFFL,0xFFFFFFFFFDFFFFFFL,0xFFFFFFFFFBFFFFFFL,0xFFFFFFFFF7FFFFFFL,0xFFFFFFFFEFFFFFFFL,0xFFFFFFFFDFFFFFFFL,0xFFFFFFFFBFFFFFFFL,0xFFFFFFFF7FFFFFFFL,0xFFFFFFFEFFFFFFFFL,0xFFFFFFFDFFFFFFFFL,0xFFFFFFFBFFFFFFFFL,0xFFFFFFF7FFFFFFFFL,0xFFFFFFEFFFFFFFFFL,0xFFFFFFDFFFFFFFFFL,0xFFFFFFBFFFFFFFFFL,0xFFFFFF7FFFFFFFFFL,0xFFFFFEFFFFFFFFFFL,0xFFFFFDFFFFFFFFFFL,0xFFFFFBFFFFFFFFFFL,0xFFFFF7FFFFFFFFFFL,0xFFFFEFFFFFFFFFFFL,0xFFFFDFFFFFFFFFFFL,0xFFFFBFFFFFFFFFFFL,0xFFFF7FFFFFFFFFFFL,0xFFFEFFFFFFFFFFFFL,0xFFFDFFFFFFFFFFFFL,0xFFFBFFFFFFFFFFFFL,0xFFF7FFFFFFFFFFFFL,0xFFEFFFFFFFFFFFFFL,0xFFDFFFFFFFFFFFFFL,0xFFBFFFFFFFFFFFFFL,0xFF7FFFFFFFFFFFFFL,0xFEFFFFFFFFFFFFFFL,0xFDFFFFFFFFFFFFFFL,0xFBFFFFFFFFFFFFFFL,0xF7FFFFFFFFFFFFFFL,0xEFFFFFFFFFFFFFFFL,0xDFFFFFFFFFFFFFFFL,0xBFFFFFFFFFFFFFFFL,0x7FFFFFFFFFFFFFFFL };
	public static final long[] zeroSelectors2 = { 0xFFFFFFFFFFFFFFFCL,0xFFFFFFFFFFFFFFF9L,0xFFFFFFFFFFFFFFF3L,0xFFFFFFFFFFFFFFE7L,0xFFFFFFFFFFFFFFCFL,0xFFFFFFFFFFFFFF9FL,0xFFFFFFFFFFFFFF3FL,0xFFFFFFFFFFFFFE7FL,0xFFFFFFFFFFFFFCFFL,0xFFFFFFFFFFFFF9FFL,0xFFFFFFFFFFFFF3FFL,0xFFFFFFFFFFFFE7FFL,0xFFFFFFFFFFFFCFFFL,0xFFFFFFFFFFFF9FFFL,0xFFFFFFFFFFFF3FFFL,0xFFFFFFFFFFFE7FFFL,0xFFFFFFFFFFFCFFFFL,0xFFFFFFFFFFF9FFFFL,0xFFFFFFFFFFF3FFFFL,0xFFFFFFFFFFE7FFFFL,0xFFFFFFFFFFCFFFFFL,0xFFFFFFFFFF9FFFFFL,0xFFFFFFFFFF3FFFFFL,0xFFFFFFFFFE7FFFFFL,0xFFFFFFFFFCFFFFFFL,0xFFFFFFFFF9FFFFFFL,0xFFFFFFFFF3FFFFFFL,0xFFFFFFFFE7FFFFFFL,0xFFFFFFFFCFFFFFFFL,0xFFFFFFFF9FFFFFFFL,0xFFFFFFFF3FFFFFFFL,0xFFFFFFFE7FFFFFFFL,0xFFFFFFFCFFFFFFFFL,0xFFFFFFF9FFFFFFFFL,0xFFFFFFF3FFFFFFFFL,0xFFFFFFE7FFFFFFFFL,0xFFFFFFCFFFFFFFFFL,0xFFFFFF9FFFFFFFFFL,0xFFFFFF3FFFFFFFFFL,0xFFFFFE7FFFFFFFFFL,0xFFFFFCFFFFFFFFFFL,0xFFFFF9FFFFFFFFFFL,0xFFFFF3FFFFFFFFFFL,0xFFFFE7FFFFFFFFFFL,0xFFFFCFFFFFFFFFFFL,0xFFFF9FFFFFFFFFFFL,0xFFFF3FFFFFFFFFFFL,0xFFFE7FFFFFFFFFFFL,0xFFFCFFFFFFFFFFFFL,0xFFF9FFFFFFFFFFFFL,0xFFF3FFFFFFFFFFFFL,0xFFE7FFFFFFFFFFFFL,0xFFCFFFFFFFFFFFFFL,0xFF9FFFFFFFFFFFFFL,0xFF3FFFFFFFFFFFFFL,0xFE7FFFFFFFFFFFFFL,0xFCFFFFFFFFFFFFFFL,0xF9FFFFFFFFFFFFFFL,0xF3FFFFFFFFFFFFFFL,0xE7FFFFFFFFFFFFFFL,0xCFFFFFFFFFFFFFFFL,0x9FFFFFFFFFFFFFFFL,0x3FFFFFFFFFFFFFFFL };
	public static final long[] zeroSelectors4 = { 0xFFFFFFFFFFFFFFF0L,0xFFFFFFFFFFFFFFE1L,0xFFFFFFFFFFFFFFC3L,0xFFFFFFFFFFFFFF87L,0xFFFFFFFFFFFFFF0FL,0xFFFFFFFFFFFFFE1FL,0xFFFFFFFFFFFFFC3FL,0xFFFFFFFFFFFFF87FL,0xFFFFFFFFFFFFF0FFL,0xFFFFFFFFFFFFE1FFL,0xFFFFFFFFFFFFC3FFL,0xFFFFFFFFFFFF87FFL,0xFFFFFFFFFFFF0FFFL,0xFFFFFFFFFFFE1FFFL,0xFFFFFFFFFFFC3FFFL,0xFFFFFFFFFFF87FFFL,0xFFFFFFFFFFF0FFFFL,0xFFFFFFFFFFE1FFFFL,0xFFFFFFFFFFC3FFFFL,0xFFFFFFFFFF87FFFFL,0xFFFFFFFFFF0FFFFFL,0xFFFFFFFFFE1FFFFFL,0xFFFFFFFFFC3FFFFFL,0xFFFFFFFFF87FFFFFL,0xFFFFFFFFF0FFFFFFL,0xFFFFFFFFE1FFFFFFL,0xFFFFFFFFC3FFFFFFL,0xFFFFFFFF87FFFFFFL,0xFFFFFFFF0FFFFFFFL,0xFFFFFFFE1FFFFFFFL,0xFFFFFFFC3FFFFFFFL,0xFFFFFFF87FFFFFFFL,0xFFFFFFF0FFFFFFFFL,0xFFFFFFE1FFFFFFFFL,0xFFFFFFC3FFFFFFFFL,0xFFFFFF87FFFFFFFFL,0xFFFFFF0FFFFFFFFFL,0xFFFFFE1FFFFFFFFFL,0xFFFFFC3FFFFFFFFFL,0xFFFFF87FFFFFFFFFL,0xFFFFF0FFFFFFFFFFL,0xFFFFE1FFFFFFFFFFL,0xFFFFC3FFFFFFFFFFL,0xFFFF87FFFFFFFFFFL,0xFFFF0FFFFFFFFFFFL,0xFFFE1FFFFFFFFFFFL,0xFFFC3FFFFFFFFFFFL,0xFFF87FFFFFFFFFFFL,0xFFF0FFFFFFFFFFFFL,0xFFE1FFFFFFFFFFFFL,0xFFC3FFFFFFFFFFFFL,0xFF87FFFFFFFFFFFFL,0xFF0FFFFFFFFFFFFFL,0xFE1FFFFFFFFFFFFFL,0xFC3FFFFFFFFFFFFFL,0xF87FFFFFFFFFFFFFL,0xF0FFFFFFFFFFFFFFL,0xE1FFFFFFFFFFFFFFL,0xC3FFFFFFFFFFFFFFL,0x87FFFFFFFFFFFFFFL,0xFFFFFFFFFFFFFFFL };
	public static final long[] zeroSelectors8 = { 0xFFFFFFFFFFFFFF00L,0xFFFFFFFFFFFFFE01L,0xFFFFFFFFFFFFFC03L,0xFFFFFFFFFFFFF807L,0xFFFFFFFFFFFFF00FL,0xFFFFFFFFFFFFE01FL,0xFFFFFFFFFFFFC03FL,0xFFFFFFFFFFFF807FL,0xFFFFFFFFFFFF00FFL,0xFFFFFFFFFFFE01FFL,0xFFFFFFFFFFFC03FFL,0xFFFFFFFFFFF807FFL,0xFFFFFFFFFFF00FFFL,0xFFFFFFFFFFE01FFFL,0xFFFFFFFFFFC03FFFL,0xFFFFFFFFFF807FFFL,0xFFFFFFFFFF00FFFFL,0xFFFFFFFFFE01FFFFL,0xFFFFFFFFFC03FFFFL,0xFFFFFFFFF807FFFFL,0xFFFFFFFFF00FFFFFL,0xFFFFFFFFE01FFFFFL,0xFFFFFFFFC03FFFFFL,0xFFFFFFFF807FFFFFL,0xFFFFFFFF00FFFFFFL,0xFFFFFFFE01FFFFFFL,0xFFFFFFFC03FFFFFFL,0xFFFFFFF807FFFFFFL,0xFFFFFFF00FFFFFFFL,0xFFFFFFE01FFFFFFFL,0xFFFFFFC03FFFFFFFL,0xFFFFFF807FFFFFFFL,0xFFFFFF00FFFFFFFFL,0xFFFFFE01FFFFFFFFL,0xFFFFFC03FFFFFFFFL,0xFFFFF807FFFFFFFFL,0xFFFFF00FFFFFFFFFL,0xFFFFE01FFFFFFFFFL,0xFFFFC03FFFFFFFFFL,0xFFFF807FFFFFFFFFL,0xFFFF00FFFFFFFFFFL,0xFFFE01FFFFFFFFFFL,0xFFFC03FFFFFFFFFFL,0xFFF807FFFFFFFFFFL,0xFFF00FFFFFFFFFFFL,0xFFE01FFFFFFFFFFFL,0xFFC03FFFFFFFFFFFL,0xFF807FFFFFFFFFFFL,0xFF00FFFFFFFFFFFFL,0xFE01FFFFFFFFFFFFL,0xFC03FFFFFFFFFFFFL,0xF807FFFFFFFFFFFFL,0xF00FFFFFFFFFFFFFL,0xE01FFFFFFFFFFFFFL,0xC03FFFFFFFFFFFFFL,0x807FFFFFFFFFFFFFL,0xFFFFFFFFFFFFFFL };
	public static final long[] zeroSelectors16 = { 0xFFFFFFFFFFFF0000L,0xFFFFFFFFFFFE0001L,0xFFFFFFFFFFFC0003L,0xFFFFFFFFFFF80007L,0xFFFFFFFFFFF0000FL,0xFFFFFFFFFFE0001FL,0xFFFFFFFFFFC0003FL,0xFFFFFFFFFF80007FL,0xFFFFFFFFFF0000FFL,0xFFFFFFFFFE0001FFL,0xFFFFFFFFFC0003FFL,0xFFFFFFFFF80007FFL,0xFFFFFFFFF0000FFFL,0xFFFFFFFFE0001FFFL,0xFFFFFFFFC0003FFFL,0xFFFFFFFF80007FFFL,0xFFFFFFFF0000FFFFL,0xFFFFFFFE0001FFFFL,0xFFFFFFFC0003FFFFL,0xFFFFFFF80007FFFFL,0xFFFFFFF0000FFFFFL,0xFFFFFFE0001FFFFFL,0xFFFFFFC0003FFFFFL,0xFFFFFF80007FFFFFL,0xFFFFFF0000FFFFFFL,0xFFFFFE0001FFFFFFL,0xFFFFFC0003FFFFFFL,0xFFFFF80007FFFFFFL,0xFFFFF0000FFFFFFFL,0xFFFFE0001FFFFFFFL,0xFFFFC0003FFFFFFFL,0xFFFF80007FFFFFFFL,0xFFFF0000FFFFFFFFL,0xFFFE0001FFFFFFFFL,0xFFFC0003FFFFFFFFL,0xFFF80007FFFFFFFFL,0xFFF0000FFFFFFFFFL,0xFFE0001FFFFFFFFFL,0xFFC0003FFFFFFFFFL,0xFF80007FFFFFFFFFL,0xFF0000FFFFFFFFFFL,0xFE0001FFFFFFFFFFL,0xFC0003FFFFFFFFFFL,0xF80007FFFFFFFFFFL,0xF0000FFFFFFFFFFFL,0xE0001FFFFFFFFFFFL,0xC0003FFFFFFFFFFFL,0x80007FFFFFFFFFFFL,0xFFFFFFFFFFFFL };
	public static final long[] zeroSelectors32 = { 0xFFFFFFFF00000000L,0xFFFFFFFE00000001L,0xFFFFFFFC00000003L,0xFFFFFFF800000007L,0xFFFFFFF00000000FL,0xFFFFFFE00000001FL,0xFFFFFFC00000003FL,0xFFFFFF800000007FL,0xFFFFFF00000000FFL,0xFFFFFE00000001FFL,0xFFFFFC00000003FFL,0xFFFFF800000007FFL,0xFFFFF00000000FFFL,0xFFFFE00000001FFFL,0xFFFFC00000003FFFL,0xFFFF800000007FFFL,0xFFFF00000000FFFFL,0xFFFE00000001FFFFL,0xFFFC00000003FFFFL,0xFFF800000007FFFFL,0xFFF00000000FFFFFL,0xFFE00000001FFFFFL,0xFFC00000003FFFFFL,0xFF800000007FFFFFL,0xFF00000000FFFFFFL,0xFE00000001FFFFFFL,0xFC00000003FFFFFFL,0xF800000007FFFFFFL,0xF00000000FFFFFFFL,0xE00000001FFFFFFFL,0xC00000003FFFFFFFL,0x800000007FFFFFFFL,0xFFFFFFFFL };

	/**
	 * The number of leading and trailing zeros for every byte
	 */
	public static final byte[] leadingZeros = { 8,7,6,6,5,5,5,5,4,4,4,4,4,4,4,4,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 };
	public static final byte[] trailingZeros = { 8,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,4,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,5,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,4,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,6,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,4,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,5,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,4,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,7,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,4,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,5,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,4,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,6,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,4,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,5,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0,4,0,1,0,2,0,1,0,3,0,1,0,2,0,1,0 };


	/**
	 * All the factorials that can be encoded in a long
	 */
    public static final long[] factorials = new long[] {
                       1l,                  1l,                   2l,
                       6l,                 24l,                 120l,
                     720l,               5040l,               40320l,
                  362880l,            3628800l,            39916800l,
               479001600l,         6227020800l,         87178291200l,
           1307674368000l,     20922789888000l,     355687428096000l,
        6402373705728000l, 121645100408832000l, 2432902008176640000l };


	public static final double SQRT_2 = Math.sqrt(2);


	/**
	 * @return $\ceil{\log_{2}(x)}$
	 */
	public static final int log2(int x) {
		return 32-Integer.numberOfLeadingZeros(x-1);
	}


	/**
	 * @return $\ceil{\log_{2}(x)}$
	 */
	public static final int log2(long x) {
		return 64-Long.numberOfLeadingZeros(x-1);
	}


	public static final int bitsToEncode(int x) {
		return 32-Integer.numberOfLeadingZeros(x);
	}


	public static final int closestPowerOfTwo(int x) {
		if ((x&(x-1))==0) return x;
		return (int)oneSelectors1[32-Integer.numberOfLeadingZeros(x)];
	}


	/**
	 * Deprecated because it is intrinsified by modern JVMs: see <library_call.cpp> and
	 * <vmSymbols.hpp>.
	 */
	public static final int numberOfLeadingZeros(int x) {
		if (x==0) return 32;
		int out = 0;
		if ((x>>>16)==0) { out+=16; x<<=16; }
		if ((x>>>24)==0) { out+=8; x<<=8; }
		return out+leadingZeros[(int)(x>>>24)];
	}


	/**
	 * Deprecated because it is intrinsified by modern JVMs: see <library_call.cpp> and
	 * <vmSymbols.hpp>.
	 */
	public static final int numberOfLeadingZeros(long x) {
		if (x==0) return 64;
		int out = 0;
		if ((x>>>32)==0) { out+=32; x<<=32; }
		if ((x>>>48)==0) { out+=16; x<<=16; }
		if ((x>>>56)==0) { out+=8; x<<=8; }
		return out+leadingZeros[(int)(x>>>56)];
	}


	/**
	 * Deprecated because it is intrinsified by modern JVMs: see <library_call.cpp> and
	 * <vmSymbols.hpp>.
	 */
	public static final int numberOfTrailingZeros(long x) {
		if (x==0) return 64;
		int out = 0;
		if ((x&0xFFFFFFFF)==0) { out+=32; x>>=32; }
		if ((x&0xFFFF)==0) { out+=16; x>>=16; }
		if ((x&0xFF)==0) { out+=8; x>>=8; }
		return out+trailingZeros[(int)(x&0xFF)];
	}


	/**
	 * @return $\ceil{numerator/denominator}$
	 */
	public static final int divideAndRoundUp(int numerator, int denominator) {
	  return 1+(numerator-1)/denominator;
	}


	/**
	 * Initializes the xorshift pseudorandom number generator with a random seed.
	 * Should be performed only once by any given program.
	 * Borrowed from Sebastiano Vigna's $dsiutils$: see the corresponding sources for
	 * documentation.
	 */
/*	public static final void initRandom() {
		RANDOM=++INCREASING_LONG+System.nanoTime();
		RANDOM^=RANDOM>>>33;
		RANDOM*=0xff51afd7ed558ccdL;
		RANDOM^=RANDOM>>>33;
		RANDOM*=0xc4ceb9fe1a85ec53L;
		RANDOM^=RANDOM>>>33;
		nextRandom(2);
	}
*/

	/**
	 * Simple xorshift pseudorandom number generator. Borrowed from Sebastiano Vigna's
	 * $dsiutils$: see the corresponding sources for documentation.
	 */
/*	public static final int nextRandom(int upperBound) {
		RANDOM^=RANDOM<<23;
		RANDOM^=RANDOM>>>52;
		RANDOM^=RANDOM>>>17;
		return (int)(((2685821657736338717L*RANDOM)>>>1)%upperBound);
	}
*/

	/**
	 * Simple xorshift pseudorandom number generator. Borrowed from Sebastiano Vigna's
	 * $dsiutils$: see the corresponding sources for documentation.
	 */
/*	public static final long nextRandom() {
		RANDOM^=RANDOM<<23;
		RANDOM^=RANDOM>>>52;
		RANDOM^=RANDOM>>>17;
		return 2685821657736338717L*RANDOM;
	}
*/

	/**
	 * @return $x!$ if $x<=170$, $Double.POSITIVE_INFINITY$ if $x>170$. 170 is the largest
	 * value of $x$ for which $x!$ can be encoded in a double. Borrowed from the Apache
	 * Commons math library.
	 *
	 * Remark: this algorithm is not necessarily the fastest. A first way to make it
	 * faster would be to replace $Math.log$ and $Math.exp$.
	 */
	public static final double factorial(int x) {
		if (x<21) return factorials[x];
		if (x>170) return Double.POSITIVE_INFINITY;
		double logSum = 0;
        for (int i=2; i<=x; i++) logSum+=Math.log(i);
        return Math.floor(Math.exp(logSum)+0.5);
	}


	/**
	 * @return the most significant bit of $x$, if $x$ is nonzero; -1 otherwise.
	 */
	public static final int mostSignificantBit(long x) {
		return 63-Long.numberOfLeadingZeros(x);
	}

	/**
	 * @return the most significant bit of $x$, if $x$ is nonzero; -1 otherwise.
	 */
	public static final int mostSignificantBit(int x) {
		return 31-Integer.numberOfLeadingZeros(x);
	}










	public static final IntArray loadDNA(String path, int length, int bufferSize) throws IOException {
		int c;
		BufferedReader br;
		IntArray out;

		out = new IntArray(length,2);
		br = new BufferedReader(new FileReader(path),bufferSize);
		c=br.read();
		while (c!=-1) {
			switch (c) {
				case 'a': out.push(0); break;
				case 'A': out.push(0); break;
				case 'c': out.push(1); break;
				case 'C': out.push(1); break;
				case 'g': out.push(2); break;
				case 'G': out.push(2); break;
				case 't': out.push(3); break;
				case 'T': out.push(3); break;
			}
			c=br.read();
		}
		br.close(); br=null;
		return out;
	}

	/*
	private static final void printDNA(IntArray string) {
		int i, n, c;
		char d = 0;
		n=string.length();
		for (i=0; i<n; i++) {
			c=string.getElementAt(i);
			switch (c) {
				case 0: d='a'; break;
				case 1: d='c'; break;
				case 2: d='g'; break;
				case 3: d='t'; break;
			}
			System.out.print(""+d);
		}
	}
	*/
}