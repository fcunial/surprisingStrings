
import java.io.IOException;


/**
 * Measures the effects of reducing available memory on the construction and running
 * time of $SubstringIterator$. Should be extended to measure also peak memory of
 * construction, and the size of the BWT, using suitable reflection methods.
 */
public class MemoryOnSubstringIterator {

	public static void main(String[] args) {
		int i, stringLength, maxLength;
		int m, mMin, mMax, mStep, iterations;
		long time;
		String path;
		IntArray string=null;
		TestSubstring w;
		SubstringIterator iterator;
		Runtime runtime = Runtime.getRuntime();

		// Parsing input
		path=args[0];
		stringLength=Integer.parseInt(args[1]);
		iterations=Integer.parseInt(args[2]);
		mMax=Integer.parseInt(args[3]);
		mMin=Integer.parseInt(args[4]);
		mStep=Integer.parseInt(args[5]);
		maxLength=Integer.parseInt(args[6]);

		// Measuring
		final int N_THREADS = 2;
		int[] alphabet = new int[] {0,1,2,3};
		try { string=Utils.loadDNA(path,stringLength,1000); }
		catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		w = new TestSubstring(4,2,string.length(),Utils.log2(string.length()),maxLength);
		for (m=mMax; m>=mMin; m-=mStep) {
			iterator=null;
			time=System.currentTimeMillis();
			for (i=0; i<iterations; i++) iterator = new SubstringIterator(string,alphabet,alphabet.length,m,N_THREADS,w);
			System.out.print(((double)(System.currentTimeMillis()-time))/iterations+" ");
			System.gc();
			time=System.currentTimeMillis();
			for (i=0; i<iterations; i++) iterator.run(N_THREADS);
			System.out.print(((double)(System.currentTimeMillis()-time))/iterations+" ");
			System.out.println();
		}
	}


	private static class TestSubstring extends Substring {
		private final int MAX_LENGTH;

		public TestSubstring(int alphabetLength, int log2alphabetLength, int textLength, int log2textLength, int maxLength) {
			super(alphabetLength,log2alphabetLength,textLength,log2textLength);
			MAX_LENGTH=maxLength;
		}

		protected Substring getInstance() {
			return new TestSubstring(alphabetLength,log2alphabetLength,textLength,log2textLength,MAX_LENGTH);
		}

		protected boolean shouldBeExtendedLeft() {
			return super.shouldBeExtendedLeft()&&length<=MAX_LENGTH;
		}
	}

}