
import java.io.IOException;


/**
 * Measures the effects of ... on ...
 */
public class TuneWorkStealingRightMaximal {

	public static void main(String[] args) {
		int i, stringLength, maxLength;
		int m, mMin, mMax, mStep, d, dMin, dMax, dStep, iterations;
		long t, time;
		String path;
		IntArray string=null;
		RightMaximalSubstring w;
		SubstringIterator iterator;
		Runtime runtime = Runtime.getRuntime();

		// Parsing input
		path=args[0];
		stringLength=Integer.parseInt(args[1]);
		iterations=Integer.parseInt(args[2]);
		mMin=Integer.parseInt(args[3]);
		mMax=Integer.parseInt(args[4]);
		mStep=Integer.parseInt(args[5]);
		dMin=Integer.parseInt(args[6]);
		dMax=Integer.parseInt(args[7]);
		dStep=Integer.parseInt(args[8]);

		// Measuring
		int[] alphabet = new int[] {0,1,2,3};
		try { string=Utils.loadDNA(path,stringLength,1000); }
		catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		Constants constants = new Constants();
		w = new RightMaximalSubstring(4,2,string.length(),Utils.log2(string.length()));
		iterator = new SubstringIterator(string,alphabet,alphabet.length,w,constants);
		System.gc();
		for (m=mMin; m<=mMax; m+=mStep) {
			constants.MAX_STRING_LENGTH_FOR_SPLIT=m;
			for (d=dMin; d<=dMax; d+=dStep) {
				constants.DONOR_STACK_LOWERBOUND=d;
				time=0;
				for (i=0; i<iterations; i++) {
					t=System.nanoTime();
					iterator.run();
					time+=System.nanoTime()-t;
					System.gc();
				}
				System.out.print(((double)time)/iterations+" ");
			}
			System.out.println();
		}
	}

}