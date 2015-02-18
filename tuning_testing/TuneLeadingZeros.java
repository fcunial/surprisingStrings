
import java.io.IOException;


public class TuneLeadingZeros {
	
	public static void main(String[] args) {
		int i, nElements;
		long time;
		long[] array;		
		nElements=Integer.parseInt(args[0]);
		array = new long[nElements];
		Utils.initRandom();
		for (i=0; i<nElements; i++) array[i]=Utils.nextRandom();
		
		time=System.currentTimeMillis();
		for (i=0; i<nElements; i++) Long.numberOfLeadingZeros(array[i]);
		System.out.println("Java: "+(System.currentTimeMillis()-time)+" ms");
		time=System.currentTimeMillis();
		for (i=0; i<nElements; i++) Utils.numberOfLeadingZeros(array[i]);
		System.out.println("My method: "+(System.currentTimeMillis()-time)+" ms");
		
	}
	
}