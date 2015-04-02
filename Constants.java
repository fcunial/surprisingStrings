public class Constants {

	Runtime runtime = Runtime.getRuntime();


	/**
	 * Number of threads to be used in all parallel steps. Influences the number of blocks
	 * in the constructor of $SubstringIterator$.
	 */
	public int N_THREADS = runtime.availableProcessors();



	// ------------------------------- Suffixes ------------------------------------------
	/**
	 * Configurable parameters used for tuning $sort$ and $buildLCPArray$
	 */
	public int QUICKSORT_HEAPSORT_SCALE = 2;
	public int STOP_QUICKSORT_AT_SIZE = 40;
	public int DISTINGUISHING_PREFIX = 3;




	// --------------------------- SubstringIterator -------------------------------------
	/**
	 * Maximum number of bytes to be used during construction (in addition to those used
	 * by $string$). Inversely proportional to the number of blocks and to construction
	 * time. Does not affect the time of $run$ if substrings have just one interval.
	 */
	public int MAX_MEMORY = 2000000;




	// ------------------------ SubstringIteratorThread ----------------------------------
	/**
	 * Number of longs allocated to each region of the stack (a power of two).
	 * Balances between space and time.
	 */
	public int LONGS_PER_REGION = 8;

	/**
	 * Number of work-stealing attempts performed by each thread before terminating.
	 */
	public int N_STEALING_ATTEMPTS = 8;

	/**
	 * Only strings of length at most $MAX_STRING_LENGTH_FOR_SPLIT$ are stolen from
	 * the donor. Must be at least 1.
	 * Increasing this value has the drawback of making the receiver scan a longer prefix
	 * of the donor stack, but it has the advantage of increasing the granularity of the
	 * stealing.
	 */
	public int MAX_STRING_LENGTH_FOR_SPLIT = 4;

	/**
	 * The stack of the donor thread is split iff it contains at least
	 * $DONOR_STACK_LOWERBOUND$ strings of length at most $MAX_STRING_LENGTH_FOR_SPLIT$
	 * that have not yet been extended.
	 * Increasing this value makes the receiver less likely to steal from a thread, but it
	 * gives more material to the receiver after stealing.
	 */
	public int DONOR_STACK_LOWERBOUND = 2;


}