
import java.io.IOException;


public class AverageLCP {

	public static void main(String[] args) {
		int i, j, stringLength, iterations, positiveIterations, suffix1, suffix2;
		double lcp;
		String path;
		IntArray string=null;
		XorShiftStarRandom random = new XorShiftStarRandom();

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
			suffix1=random.nextInt(stringLength);
			do { suffix2=random.nextInt(stringLength); } while (suffix2==suffix1);
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