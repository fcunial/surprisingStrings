
import java.io.IOException;


/**
 *
 */
public class TestDrive {

	static int[][] matrix;
	static int maxSubstringLength;


	public static void main(String[] args) {
		int i, j, stringLength, maxLength;
		int l, iterations;
		long time;
		String path;
		IntArray string=null;
		TestBernoulliSubstring w;
		SubstringIterator iterator;
		Runtime runtime = Runtime.getRuntime();

		// Parsing input
		path="/Users/gustavedore/Projects/surprisingStrings_old/inputs/NC_021658.fna";
		stringLength=14782125;
		// n. lines: 211174

		// Measuring
		int[] alphabet = new int[] {0,1,2,3};
//maxSubstringLength=10*(int)Math.ceil(Math.log(stringLength)/Math.log(alphabet.length));
		try { string=Utils.loadDNA(path,stringLength,1000); }
		catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		stringLength=(int)string.length();
System.out.println("Effective length: "+stringLength);
//Utils.shuffle(string);
		w = new TestBernoulliSubstring(alphabet.length,Utils.log2(alphabet.length),Utils.bitsToEncode(alphabet.length),stringLength+1,Utils.log2(stringLength+1),Utils.bitsToEncode(stringLength+1));
		Constants.N_THREADS=2;
		matrix = new int[1000][1000];
		time=System.currentTimeMillis();
		iterator = new SubstringIterator(string,alphabet,alphabet.length,w);
		System.out.print("construction time: "+((double)(System.currentTimeMillis()-time))/1000+"s, ");

		Constants.N_THREADS=2;
		Constants.BORDER_THRESHOLD_1=-1;
		Constants.CACHE_SIZE=0;
		Constants.BORDER_THRESHOLD_2=Integer.MAX_VALUE;
		time=System.currentTimeMillis();
		iterator.run();
		System.out.println("traversal time with no caching and no cutoff: "+((double)(System.currentTimeMillis()-time))/1000);

/*		Constants.N_THREADS=1;
		Constants.BORDER_THRESHOLD_1=16;
		Constants.CACHE_SIZE=16;
		Constants.BORDER_THRESHOLD_2=Integer.MAX_VALUE;
		time=System.currentTimeMillis();
		iterator.run();
		System.out.println("traversal time with no cutoff and cache of size "+Constants.CACHE_SIZE+": "+((double)(System.currentTimeMillis()-time))/1000);
*/

/*		Constants.BORDER_THRESHOLD_2=20;
		for (int c=16; c>=1; c>>=1) {
			Constants.BORDER_THRESHOLD_1=c;
			Constants.CACHE_SIZE=c;
			histogram = new int[maxSubstringLength];
			time=System.currentTimeMillis();
			iterator.run();
			System.out.println("traversal time with cutoff and cache of size "+c+": "+((double)(System.currentTimeMillis()-time))/1000);
		}
*/
/*		System.out.println("matrix:");
		for (i=0; i<matrix.length; i++) {
			for (j=0; j<matrix[i].length; j++) System.out.print(matrix[i][j]+" ");
			System.out.println();
		}
*/	}


	private static class TestBernoulliSubstring extends BernoulliSubstring {
		protected TestBernoulliSubstring() { }
		public TestBernoulliSubstring(int alphabetLength, int log2alphabetLength, int bitsToEncodeAlphabetLength, long bwtLength, int log2BWTLength, int bitsToEncodeBWTLength) {
			super(alphabetLength,log2alphabetLength,bitsToEncodeAlphabetLength,bwtLength,log2BWTLength,bitsToEncodeBWTLength);
		}
		protected Substring getInstance() {
			return new TestBernoulliSubstring(alphabetLength,log2alphabetLength,bitsToEncodeAlphabetLength,bwtLength,log2BWTLength,bitsToEncodeBWTLength);
		}

		protected void visited(Stream stack, RigidStream characterStack, SimpleStream pointerStack, Substring[] cache, Substring[] leftExtensions) {
			super.visited(stack,characterStack,pointerStack,cache,leftExtensions);
			if (longestBorderLength<matrix.length && length-longestBorderLength<matrix.length) {
				matrix[(int)longestBorderLength][(int)(length-longestBorderLength)]++;
			}
		}
	}

}