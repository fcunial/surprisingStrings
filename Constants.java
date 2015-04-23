public class Constants {


	/**
	 * Number of threads to be used in all parallel steps. Influences the number of blocks
	 * in the constructor of $SubstringIterator$.
	 */
	public static int N_THREADS = Runtime.getRuntime().availableProcessors();



	// ------------------------------- Suffixes ------------------------------------------
	/**
	 * Configurable parameters used for tuning $sort$ and $buildLCPArray$
	 */
	public static int QUICKSORT_HEAPSORT_SCALE = 2;
	public static int STOP_QUICKSORT_AT_SIZE = 40;
	public static int DISTINGUISHING_PREFIX = 3;




	// --------------------------- SubstringIterator -------------------------------------
	/**
	 * Maximum number of bytes to be used during construction (in addition to those used
	 * by $string$). Inversely proportional to the number of blocks and to construction
	 * time. Does not affect the time of $run$ if substrings have just one interval.
	 */
	public static long MAX_MEMORY = 7000000;




	// ------------------------ SubstringIteratorThread ----------------------------------
	/**
	 * Number of longs allocated to each region of the stack (a power of two).
	 * Balances between space and time.
	 */
	public static int LONGS_PER_REGION = 2;
	public static int LONGS_PER_REGION_CHARACTERSTACK = LONGS_PER_REGION;  // Must be tuned experimentally
	public static int LONGS_PER_REGION_POINTERSTACK = LONGS_PER_REGION;  // Must be tuned experimentally

	/**
	 * Number of work-stealing attempts performed by each thread before terminating.
	 */
	public static int N_STEALING_ATTEMPTS = 10;

	/**
	 * Only strings of length at most $MAX_STRING_LENGTH_FOR_SPLIT$ are stolen from
	 * the donor. Must be at least 1.
	 * Increasing this value has the drawback of making the receiver scan a longer prefix
	 * of the donor stack, but it has the advantage of increasing the granularity of the
	 * stealing, and thus of improving load balancing \cite{rao1987parallel}.
	 */
	public static int MAX_STRING_LENGTH_FOR_SPLIT = 3;

	/**
	 * The stack of the donor thread is split iff it contains at least
	 * $DONOR_STACK_LOWERBOUND$ strings of length at most $MAX_STRING_LENGTH_FOR_SPLIT$
	 * that have not yet been extended.
	 * Increasing this value makes the receiver less likely to steal from a thread, but it
	 * gives more material to the receiver after stealing.
	 */
	public static int DONOR_STACK_LOWERBOUND = 2;











	/**
	 * Base-$e$ logarithm of the probability of each character in the alphabet
	 */
	public static double[] logProbabilities = new double[] {1/4,1/4,1/4,1/4};


	public static final byte SCORE_ID = 8;


	/**
	 * Computes the surprise score of type $SCORE_ID$ of a substring. This procedure
	 * defines $SCORE_ID$.
	 */
	public static final double getScore(double frequency, double expectation, double variance) {
		switch (SCORE_ID) {
			// Expectation only
			case 1: return frequency-expectation;
			case 2: return frequency/expectation;
			case 3: return (frequency-expectation)/expectation;
			case 4: return (frequency-expectation)/Math.sqrt(expectation);
			case 5: return Math.abs(frequency-expectation)/Math.sqrt(expectation);
			case 6: return (frequency-expectation)*(frequency-expectation)/expectation;
			case 7: return (frequency-expectation)/Math.sqrt(variance);  // Assumes that $variance$ is the approximation $expectation*(1-pHat)$
			// Expectation and variance
			case 8: return (frequency-expectation)/Math.sqrt(variance);
			case 9: return Math.abs(frequency-expectation)/Math.sqrt(variance);
		}
		return 0;
	}


	/**
	 * Determines which internal nodes of the suffix tree of the text to explore in order
	 * to detect surprising substrings of a given type.
	 *
	 * Remark: When the score is non-monotonic inside a left-equivalence class, or when it
	 * is not a convex function of a monotonic score, or when it is not a convex and
	 * increasing function of a convex function of a monotonic score, we cannot limit the
	 * search to nodes of the suffix tree and to their one-character extensions, thus the
	 * whole approach breaks down.
	 *
	 * @param stringType 0: over-represented; 1: under-represented; 2: both;
	 * @param maxProbability $\max\{\mathbb{P}(a) : a \in \Sigma\}$;
	 * @return 0: right-maximal substrings; 1: one-character right-extensions of
	 * right-maximal substrings; 2: both; 3: don't explore any node, i.e. the
	 * whole approach fails.
	 */
	public static final byte nodesToExplore(byte stringType, double maxProbability) {
		if (SCORE_ID<=5) return stringType;
		if (SCORE_ID==6) return 2;
		if (SCORE_ID==7) {
			if (maxProbability<0.5) return stringType;
			else return 3;
		}
		if (SCORE_ID==8) {
			// These three upper bounds refer to function $f(m)=1/((4m)^{1/m})$ evaluated at
			// $m=1,2$, and to $\sqrt{2}-1$. Just these two values of $m$ need to be
			// considered since $f(m)>\sqrt{2}-1$ at $m \geq 3$.
			if (maxProbability<Math.min(Math.min(Math.sqrt(2)-1,1d/Math.sqrt(8)),0.25)) return stringType;
			else return 3;
		}
		if (SCORE_ID==9) {
			// These three upper bounds refer to function $f(m)=1/((4m)^{1/m})$ evaluated at
			// $m=1,2$, and to $\sqrt{2}-1$. Just these two values of $m$ need to be
			// considered since $f(m)>\sqrt{2}-1$ at $m \geq 3$.
			if (maxProbability<Math.min(Math.min(Math.sqrt(2)-1,1d/Math.sqrt(8)),0.25)) return 2;
			else return 3;
		}
		return 3;
	}

}