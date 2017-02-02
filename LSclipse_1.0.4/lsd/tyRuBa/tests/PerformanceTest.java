package tyRuBa.tests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import tyRuBa.engine.FrontEnd;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.util.ElementSource;

public class PerformanceTest {

	private boolean isScenario;
	private String[] queries;
	FrontEnd frontend;

	/** Array of Tests (with results) */
	Test[] tests;
	

	/** 
	 * Creates a performance test, runs it once and remembers the test results
	 */
	public PerformanceTest(FrontEnd frontend, String[] queries,boolean isScenario)
	throws ParseException, IOException, TypeModeError {
		this.frontend = frontend;
		this.queries = queries;
		this.isScenario = isScenario;
		run();
	}

	private void run() {
		if (isScenario)
			runScenario();
		else
			runOneByOne();
	}

	/** Creates a performance test from a file of queries (one per line) */	
	public static PerformanceTest make(FrontEnd frontend, String queryfile) throws ParseException, IOException, TypeModeError {
		ArrayList queries = new ArrayList();
		BufferedReader qf = new BufferedReader(new FileReader(queryfile));
		String query;
		boolean isScenario = false;
		while (null!=(query=qf.readLine())) {
			if (!query.startsWith("//"))
				queries.add(query);
			else if (query.startsWith("//SCENARIO")) {
				isScenario = true;
			}
		}
		return new PerformanceTest(frontend,(String[])queries.toArray(new String[queries.size()]),isScenario);	
	}

	public class Test {

		String query;
		long runtime = 0;
		long numresults = 0;
		private Throwable error = null;
		
		public Test(String query) {
			this.query = query;
		}

		void run() throws ParseException, TypeModeError {
			ElementSource result = frontend.frameQuery(query);
			while (result.hasMoreElements()) {
				numresults++;
				result.nextElement();
			}
		}
		
		void timedRun() {
			timedScenarioStepRun(System.currentTimeMillis());
		}

		public long timedScenarioStepRun(long startTime) {
			numresults = 0;
			try {
				run();
			}
			catch (Throwable e) {
				this.error = e;
			}
		 	long endtime = System.currentTimeMillis();
			this.runtime = endtime - startTime;
			return endtime;
		}

		public String toString() {
			if (error!=null)
				return query + "#CRASHED: "+error.getMessage();
			else
				return query + "  #results = " + numresults 
					+ "  seconds = "+((double)runtime)/1000;
		}

//		public void add(Test other) {
//			this.runtime += other.runtime;
//		}
//
//		public void div(double d) {
//			this.runtime /= d;
//		}

	}

	private void runOneByOne() {
		tests = new Test[queries.length];
		for (int i = 0; i < queries.length; i++) {
			tests[i] = new Test(queries[i]);
			System.gc(); // To let each test start rougly on
                          // the same footing and avoid gc noise.
			tests[i].timedRun();
			System.err.println(tests[i]);
		}
	}
	
	/*
	 * Run as a scenario meaning that we are more interested in how this series
	 * of queries performs as a whole. So we do not want to perform gc in between
	 * each test and we do not want to start/stop the clock in between each test,
	 * or perform IO operations between the tests to report results.
	 */
	private void runScenario() {
		tests = new Test[queries.length];
		for (int i = 0; i < queries.length; i++) {
			tests[i] = new Test(queries[i]);
		}
		System.gc(); // To let each scenario run start roughly on
                     // the same footing.
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < queries.length; i++) {
			startTime = tests[i].timedScenarioStepRun(startTime);
		}
	}
	
	public String toString() {
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < tests.length; i++) {
			out.append(tests[i]+"\n");
		}
		out.append("TOTAL : "+ totalTime() + "\n");
		return out.toString();
	}
	
//	public static void main(String[] args) throws ParseException, IOException, TypeModeError {
//		PrintWriter record = new PrintWriter(new FileOutputStream("bench_results.txt"));
//		int numruns = 8;
//
//		//repeatTest(record, numruns, usecache, softcache);
//		//repeatTest(record, numruns, true, false);
//		repeatTest(record, numruns, true, true);
//		//repeatTest(record, numruns, false, false);
//		
//		record.close();
//	}

//	private void repeatTest(
//		PrintWriter record,
//		int numruns,
//		boolean usecache,
//		boolean softcache)
//	throws ParseException, IOException, TypeModeError 
//	{
//		PerformanceTest[] results = new PerformanceTest[numruns];
//		for (int i = 0; i < results.length; i++) {
//			results[i] = splatterTest(usecache,softcache);
//			System.err.println("\n\n----------------------------------------------------\n");
//			System.err.println("Run "+i+" with "+(RuleBase.softCache?"SOFT ":"HARD")
//								+"cache "+(RuleBase.useCache?"ENABLED":"DISABLED\n"));
//			System.err.println(results[i]);
//		}
//		PerformanceTest total = results[0];
//		for (int i = 1; i < results.length; i++) {
//			total.add(results[i]);
//		}
//		total.div((double)results.length);
//		
//	record.println("\n\n---------- "+numruns +" runs AVERAGE ---------------------------\n");	
//	record.println("With "+(RuleBase.softCache?"SOFT":"HARD")
//						+" cache "+(RuleBase.useCache?"ENABLED":"DISABLED"));
//	record.println(total);
//	}

	public double totalTime() {
		long total = 0;
		for (int i = 0; i < tests.length; i++) {
			total += tests[i].runtime;
		}
		return total/1000.0;
	}

//	private void div(double d) {
//		for (int i = 0; i < tests.length; i++) {
//			this.tests[i].div(d);
//		}
//	}
//
//	private void add(PerformanceTest other) {
//		for (int i = 0; i < tests.length; i++) {
//			this.tests[i].add(other.tests[i]);
//		}
//	}

//	private static PerformanceTest splatterTest(boolean usecache,boolean softcache)
//	throws ParseException, IOException, TypeModeError {
//		PerformanceTest result = new PerformanceTest(
//			// File with the definition of predicates for testing
//				"benchmark.rub",
//			// Usecache? (can bias test results if similar tests are run in sequence)
//				usecache,
//			// Softcache (if usecache, is the cache "soft" or "permanent"
//				softcache,
//			// The queries to run and timed
//			new String[] {
//				"splatter(abcdefghijklmnop,?x),splatter(?y,?x)",
//				"topo_sort(?x)"
//			});
//		return result;
//	}

//	private static PerformanceTest desplatterTest()
//		throws ParseException, IOException, TypeModeError {
//					PerformanceTest result = new PerformanceTest(
//						// File with the definition of predicates for testing
//							"examples/benchmark/string_games.rub",
//						// Usecache? (can bias test results if similar tests are run in sequence)
//							false,
//						// Softcache (if usecache, is the cache "soft" or "permanent"
//							true,
//						// The queries to run and timed
//						new String[] {
//		//					"splatter(abcdefghijklmnop,?x)",
//		//					"splatter(abcdefghijklmnopq,?x)",
//		//					"splatter(abcdefghijklmnopqr,?x)",
//		//					"splatter(abcdefghijklmnopqrstu,?x)",
//		//					"splatter(abcdefghijklmnopqrstuvwx,?x)"
//						}
//					);
//		return result;
//	}

}
