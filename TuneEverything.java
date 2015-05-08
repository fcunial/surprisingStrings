
import java.io.IOException;


/**
 *
 */
public class TuneEverything {

	public static void main(String[] args) {
		int stringLength;
		int iterations;
		long time;
		String path;
		IntArray string=null;
		BernoulliSubstring w;
		SubstringIterator iterator;

		// Parsing input
		path="/Users/gustavedore/Projects/surprisingStrings_old/inputs/NC_021658.fna";
		stringLength=14993299;
		iterations=3;

		// Initializing
		int[] alphabet = new int[] {0,1,2,3};
		try { string=Utils.loadDNA(path,stringLength,1000); }
		catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		w = new BernoulliSubstring(4,2,string.length(),Utils.log2(string.length()));
		iterator = new SubstringIterator(string,alphabet,alphabet.length,w);

		// LONGS_PER_REGION
		System.gc();
		System.out.println("LONGS_PER_REGION");
		for (int l=0; l<=10; l++) {
			Constants.LONGS_PER_REGION=1<<l;
			time=System.currentTimeMillis();
			for (int i=0; i<iterations; i++) iterator.run();
			System.out.println(((double)(System.currentTimeMillis()-time))/iterations+" ");
		}
		Constants.LONGS_PER_REGION=2;

		// LONGS_PER_REGION_CHARACTERSTACK
		System.gc();
		System.out.println("LONGS_PER_REGION_CHARACTERSTACK");
		for (int l=0; l<=10; l++) {
			Constants.LONGS_PER_REGION_CHARACTERSTACK=1<<l;
			time=System.currentTimeMillis();
			for (int i=0; i<iterations; i++) iterator.run();
			System.out.println(((double)(System.currentTimeMillis()-time))/iterations+" ");
		}
		Constants.LONGS_PER_REGION_CHARACTERSTACK=2;

		// LONGS_PER_REGION_POINTERSTACK
		System.gc();
		System.out.println("LONGS_PER_REGION_POINTERSTACK");
		for (int l=0; l<=10; l++) {
			Constants.LONGS_PER_REGION_POINTERSTACK=1<<l;
			time=System.currentTimeMillis();
			for (int i=0; i<iterations; i++) iterator.run();
			System.out.println(((double)(System.currentTimeMillis()-time))/iterations+" ");
		}
		Constants.LONGS_PER_REGION_POINTERSTACK=2;

		// N_STEALING_ATTEMPTS
		System.gc();
		System.out.println("N_STEALING_ATTEMPTS");
		for (int l=1; l<=10; l++) {
			Constants.N_STEALING_ATTEMPTS=l;
			time=System.currentTimeMillis();
			for (int i=0; i<iterations; i++) iterator.run();
			System.out.println(((double)(System.currentTimeMillis()-time))/iterations+" ");
		}
		for (int l=15; l<=100; l+=5) {
			Constants.N_STEALING_ATTEMPTS=l;
			time=System.currentTimeMillis();
			for (int i=0; i<iterations; i++) iterator.run();
			System.out.println(((double)(System.currentTimeMillis()-time))/iterations+" ");
		}
		Constants.N_STEALING_ATTEMPTS=10;

		// MAX_STRING_LENGTH_FOR_SPLIT, DONOR_STACK_LOWERBOUND
		System.gc();
		System.out.println("MAX_STRING_LENGTH_FOR_SPLIT, DONOR_STACK_LOWERBOUND");
		for (int m=1; m<=20; m++) {
			Constants.MAX_STRING_LENGTH_FOR_SPLIT=m;
			for (int d=2; d<=20; d++) {
				Constants.DONOR_STACK_LOWERBOUND=d;
				time=System.currentTimeMillis();
				for (int i=0; i<iterations; i++) iterator.run();
				System.out.print(((double)(System.currentTimeMillis()-time))/iterations+" ");
			}
			System.out.println();
		}
		Constants.MAX_STRING_LENGTH_FOR_SPLIT=3;
		Constants.DONOR_STACK_LOWERBOUND=2;

	}

}