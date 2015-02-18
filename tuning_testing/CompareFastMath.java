
import java.io.IOException;
import org.apache.commons.math3.util.FastMath;

/**
 * In a modern VM, $FastMath$ is never faster than $Math$, due to internalization.
 *
 * Ratios $time(FastMath)/time(Math)$ applied to $10^7$ random doubles:
 * min: 4.8
 * sqrt: 2.9
 * abs: 2.4
 * log: 8917.7
 * scalb(10): 564
 * expm1: 1.3
 *
 * The benchmark $FastMathTestPerformance$ in the FastMath distribution provides similar
 * results: see $FastMathTestPerformance.txt$. $Math$ should also be faster than 
 * $StrictMath$: in our experiments this is true except for $scalb$.
 *
 * Ratios $time(StrictMath)/time(Math)$ applied to $10^7$ random doubles:
 * min: 1
 * sqrt: 1.3
 * abs: 1.3
 * log: 7939
 * scalb(10): 0.4
 * expm1: 1
 */
public class CompareFastMath {
	
	public static void main(String[] args) {
		int i, j, nIterations, nElements, operation;
		double mathTime, fastMathTime;
		double[] array;
		nIterations=Integer.parseInt(args[0]);
		nElements=Integer.parseInt(args[1]);
		operation=Integer.parseInt(args[2]);
		array = new double[nElements];
		for (j=0; j<nElements; j++) array[j]=Math.random()*nElements;
		
		if (operation==0) {
			// min
			mathTime=System.currentTimeMillis();
			for (i=0; i<nIterations; i++) {
				for (j=1; j<nElements; j++) Math.min(array[j],array[j-1]);
			}
			mathTime=(System.currentTimeMillis()-mathTime)/nIterations;
			fastMathTime=System.currentTimeMillis();
			for (i=0; i<nIterations; i++) {
				for (j=1; j<nElements; j++) FastMath.min(array[j],array[j-1]);
			}
			fastMathTime=(System.currentTimeMillis()-fastMathTime)/nIterations;
			System.out.println("min: avg(FastMath)/avg(Math)="+(fastMathTime/mathTime));
		}
		else if (operation==1) {
			// sqrt
			mathTime=System.currentTimeMillis();
			for (i=0; i<nIterations; i++) {
				for (j=0; j<nElements; j++) Math.sqrt(array[j]);
			}
			mathTime=(System.currentTimeMillis()-mathTime)/nIterations;
			fastMathTime=System.currentTimeMillis();
			for (i=0; i<nIterations; i++) {
				for (j=0; j<nElements; j++) FastMath.sqrt(array[j]);
			}
			fastMathTime=(System.currentTimeMillis()-fastMathTime)/nIterations;
			System.out.println("sqrt: avg(FastMath)/avg(Math)="+(fastMathTime/mathTime));
		}
		else if (operation==2) {
			// abs
			mathTime=System.currentTimeMillis();
			for (i=0; i<nIterations; i++) {
				for (j=0; j<nElements; j++) Math.abs(array[j]);
			}
			mathTime=(System.currentTimeMillis()-mathTime)/nIterations;
			fastMathTime=System.currentTimeMillis();
			for (i=0; i<nIterations; i++) {
				for (j=0; j<nElements; j++) FastMath.abs(array[j]);
			}
			fastMathTime=(System.currentTimeMillis()-fastMathTime)/nIterations;
			System.out.println("abs: avg(FastMath)/avg(Math)="+(fastMathTime/mathTime));
		}
		else if (operation==3) {
			// log
			mathTime=System.currentTimeMillis();
			for (i=0; i<nIterations; i++) {
				for (j=0; j<nElements; j++) Math.log(array[j]);
			}
			mathTime=(System.currentTimeMillis()-mathTime)/nIterations;
			fastMathTime=System.currentTimeMillis();
			for (i=0; i<nIterations; i++) {
				for (j=0; j<nElements; j++) FastMath.log(array[j]);
			}
			fastMathTime=(System.currentTimeMillis()-fastMathTime)/nIterations;
			System.out.println("log: avg(FastMath)/avg(Math)="+(fastMathTime/mathTime));
		}
		else if (operation==4) {
			// scalb
			mathTime=System.currentTimeMillis();
			for (i=0; i<nIterations; i++) {
				for (j=0; j<nElements; j++) Math.scalb(array[j],10);
			}
			mathTime=(System.currentTimeMillis()-mathTime)/nIterations;
			fastMathTime=System.currentTimeMillis();
			for (i=0; i<nIterations; i++) {
				for (j=0; j<nElements; j++) FastMath.scalb(array[j],10);
			}
			fastMathTime=(System.currentTimeMillis()-fastMathTime)/nIterations;
			System.out.println("scalb(10): avg(FastMath)/avg(Math)="+(fastMathTime/mathTime));
		}
		else if (operation==5) {
			// expm1
			mathTime=System.currentTimeMillis();
			for (i=0; i<nIterations; i++) {
				for (j=0; j<nElements; j++) Math.expm1(array[j]);
			}
			mathTime=(System.currentTimeMillis()-mathTime)/nIterations;
			fastMathTime=System.currentTimeMillis();
			for (i=0; i<nIterations; i++) {
				for (j=0; j<nElements; j++) FastMath.expm1(array[j]);
			}
			fastMathTime=(System.currentTimeMillis()-fastMathTime)/nIterations;
			System.out.println("expm1: avg(FastMath)/avg(Math)="+(fastMathTime/mathTime));
		}
	}
	
}