
import java.io.IOException;


/**
 *
 */
public class TestDrive {

	public static void main(String[] args) {
		int i, stringLength, maxLength;
		int l, iterations;
		long time;
		String path;
		IntArray string=null;
		RightMaximalSubstring w;
		SubstringIterator iterator;
		Runtime runtime = Runtime.getRuntime();

		// Parsing input
		path=args[0];
		stringLength=Integer.parseInt(args[1]);

		// Measuring
		int[] alphabet = new int[] {0,1,2,3};
		try { string=Utils.loadDNA(path,stringLength,1000); }
		catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		Constants constants = new Constants();
		//constants.N_THREADS=1;
		w = new RightMaximalSubstring(4,2,string.length(),Utils.log2(string.length()));
		time=System.currentTimeMillis();
		iterator = new SubstringIterator(string,alphabet,alphabet.length,w,constants);
		System.out.print("construction time: "+((double)(System.currentTimeMillis()-time))/1000+"s, ");
		System.gc();
		time=System.currentTimeMillis();
		iterator.run();
		System.out.println("traversal time: "+((double)(System.currentTimeMillis()-time))/1000);
	}

}