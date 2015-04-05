
import java.io.IOException;


/**
 * Measures the effects of ... on ...
 */
public class TuneNStealingAttempts {

	public static void main(String[] args) {
		int i, stringLength, maxLength;
		int l, iterations;
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
		maxLength=Integer.parseInt(args[3]);

		// Measuring
		int[] alphabet = new int[] {0,1,2,3};
		try { string=Utils.loadDNA(path,stringLength,1000); }
		catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		Constants constants = new Constants();
		w = new TestSubstring(4,2,string.length(),Utils.log2(string.length()),maxLength);
		iterator = new SubstringIterator(string,alphabet,alphabet.length,w,constants);
		System.gc();
		for (l=1; l<=10; l++) {
			constants.N_STEALING_ATTEMPTS=l;
			time=System.currentTimeMillis();
			for (i=0; i<iterations; i++) iterator.run();
			System.out.println(((double)(System.currentTimeMillis()-time))/iterations+" ");
		}
		for (l=15; l<=100; l+=5) {
			constants.N_STEALING_ATTEMPTS=l;
			time=System.currentTimeMillis();
			for (i=0; i<iterations; i++) iterator.run();
			System.out.println(((double)(System.currentTimeMillis()-time))/iterations+" ");
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