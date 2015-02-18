
import java.io.IOException;


public class AverageLCP {
	
	public static void main(String[] args) {	
		int i, j, stringLength, iterations, positiveIterations, suffix1, suffix2;
		double lcp;
		String path;
		IntArray string=null;
		Utils.initRandom();
		
		// Parsing input
		path=args[0];
		stringLength=Integer.parseInt(args[1]);
		iterations=Integer.parseInt(args[2]);
		
		// Measuring
		try { string=Utils.loadDNA(path,stringLength,1000); }
		catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		lcp=0; positiveIterations=0;
		for (i=0; i<iterations; i++) {
			suffix1=Utils.nextRandom(stringLength);
			do { suffix2=Utils.nextRandom(stringLength); } while (suffix2==suffix1);
			j=string.lcp(suffix1,suffix2,false);
			if (j>0) {
				lcp+=j;
				positiveIterations++;
			}
		}
		System.out.println("Average LCP: "+(lcp/positiveIterations));
		System.out.println();
	}
	
}