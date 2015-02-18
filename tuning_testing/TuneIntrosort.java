
import java.io.IOException;


public class TuneIntrosort {
	
	public static void main(String[] args) {	
		int i, j, stringLength, bufferSize;
		int c1, c1min, c1max, c1step, c2, c2min, c2max, c2step, iterations;
		long time;
		String path;
		IntArray string=null, suffixes=null;
		Utils.initRandom();
		
		// Parsing input
		path=args[0];
		stringLength=Integer.parseInt(args[1]);
		iterations=Integer.parseInt(args[2]);
		c1min=Integer.parseInt(args[3]);
		c1max=Integer.parseInt(args[4]);
		c1step=Integer.parseInt(args[5]);
		c2min=Integer.parseInt(args[6]);
		c2max=Integer.parseInt(args[7]);
		c2step=Integer.parseInt(args[8]);
		
		// Measuring
		try { string=Utils.loadDNA(path,stringLength,1000); }
		catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		for (c1=c1min; c1<=c1max; c1+=c1step) {
			for (c2=c2min; c2<=c2max; c2+=c2step) {
				Suffixes.QUICKSORT_HEAPSORT_SCALE=c1;
				Suffixes.STOP_QUICKSORT_AT_SIZE=c2;
				suffixes = new IntArray(stringLength,Utils.log2(stringLength));
				for (i=0; i<stringLength; i++) suffixes.push(i);
				time=System.currentTimeMillis();
				for (i=0; i<iterations; i++) Suffixes.sort(suffixes,string);
				System.out.print(((double)(System.currentTimeMillis()-time))/iterations+" ");
			}
			System.out.println();
		}
	}
	
}