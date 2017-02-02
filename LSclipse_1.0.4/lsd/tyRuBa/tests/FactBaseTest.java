/*
 * Created on Aug 29, 2003
 */
package tyRuBa.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import junit.framework.Assert;

import tyRuBa.engine.BackupFailedException;
import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.RuleBaseBucket;
//import tyRuBa.engine.factbase.sql.SQLDatabaseConnectionManager;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.util.Aurelizer;

/**
 * @author kdvolder
 */
public class FactBaseTest extends TyrubaTest {
	
	private class TesterThread extends Thread {

		Throwable crash = null;

		private TesterThread(String string) {
			super(string);
		}

	}
	
	private class RandomQueriesThread extends TesterThread {
		
		int howmany;

		RandomQueriesThread(int name, int howmany) {
			super(""+name);
			this.howmany = howmany;
		}

		public void run() {
			try {
				doRandomQueries(howmany);
			} catch (Throwable e) {
				crash = e;
				e.printStackTrace();
				if (Aurelizer.debug_sounds!=null) 
					Aurelizer.debug_sounds.enter("error");
			}
		}
		
	}

	private class OutdatingThread extends TesterThread {
		
		int howmany;

		OutdatingThread(String name, int howmany) {
			super(name);
			this.howmany = howmany;
		}

		public void run() {
			try {
				for (int i=0;i<howmany;i++) {
					doRandomQueries(1);
					outdateSomebuckets(i,i+1);
				}
			} catch (Throwable e) {
				crash = e;
				throw new Error("Thread " + getName() + " crashed: " + e);
			}
		}
		
	}

	private class KillingThread extends TesterThread {
		
		int howmany;

		KillingThread(String name) {
			super(name);
		}

		public void run() {
			try {
				while (test_atoms.length>1) {
					doRandomQueries(1);
					shrinkAtoms();
				}
			} catch (Throwable e) {
				crash = e;
				e.printStackTrace();
				throw new Error("Thread " + getName() + " crashed: " + e);
			}
		}
		
	}

	private class DoRandomThingsThread extends TesterThread {
		
		int howmany;
		String name;

		DoRandomThingsThread(String name, int howmany) {
			super(name);
			this.name = name;
			this.howmany = howmany;
		}

		public void run() {
			Random rnd = new Random();

			try {
				for (int i=0;i<howmany;i++) {
					switch (rnd.nextInt(10)) {
						case 0:
							System.err.println(name+" "+i+" BEG backup");
							frontend.backupFactBase();
							System.err.println(name+" "+i+" END backup");
							break;
						case 1: 
							System.err.println(name+" "+i+" BEG outdating");
							outdateSomebuckets(rnd.nextInt(2),rnd.nextInt(4)+1);
							System.err.println(name+" "+i+" END outdating");
							break;
						default:
							System.err.println(name+" "+i+" BEG query");
							doRandomQueries(1);		
							System.err.println(name+" "+i+" END query");
							break;					
					}
				}
			} 
			catch (BackupFailedException e) {
				System.err.print("Backup fact base failed, but this is normal behavior");
				e.printStackTrace();
			}
			catch (Throwable e) {
				crash = e;
				e.printStackTrace();
				throw new Error("Thread " + getName() + " crashed: " + e);
			}
		}
		
	}
	
	
	
	/** A working directory to save test files */
	static String test_space = "test_space"; 
	
	private static final boolean regenrubs = true;
		
	// small  test:
	private static int init_numatoms = 10;
	// big  test:
	//private static int init_numatoms = 20;

	private static int maxarity = 4;
	
	private int bucketLoads; 

	File factstore = new File(test_space,"fact_store");
	
	/** Name for the file which has the declarations of the predicates in them */
	String declarations_fle = test_space+"/declarations.rub";
	
	/** Names for the bucket files */
	String[] bucket_fle;
	private RuleBaseBucket[] buckets;
	
	String[] test_preds = new String[] {
		 "foo", "bar","zor","snol","brol","wols"
	};
	int[]    pred_arity;
	
	String[] test_atoms;
	String[] initial_test_atoms;

	/**
	 * Constructor for FactBaseTest.
	 */
	public FactBaseTest(String arg0) {
		super(arg0);
	}

	protected void setUp(boolean reconnect) throws Exception {

		FrontEnd old_frontend = frontend;
		if (reconnect && frontend!=null) {
			try {
			    //frontend.shutdown();
				frontend.backupFactBase();
			}
			catch (BackupFailedException e) {
				// Oh well... this failure is now considered acceptable
				// behavior so we just have to keep using the old factbase.
				super.setUpNoFrontend();
				frontend = old_frontend;
				return;
			}
		}

		super.setUpNoFrontend();
	
		if (!reconnect) {
			File test_dir = new File(test_space);
			if (regenrubs)
				makeEmptyDir(test_dir);
			makeEmptyDir(factstore);	
            frontend = new FrontEnd(true,factstore,true,null,true,false);
		} else {
            frontend = new FrontEnd(true,factstore,true,null,false,false);
        }
		
		

//		frontend.setCacheSize(8000); 
		  // relatively small, make the engine work hard!
		
		if (!reconnect)  {		
			generateRubFiles();
		}
		
		// All the files have been setup load them
		frontend.load(declarations_fle);
		
		// Make buckets for all the bucket files
		buckets = new RuleBaseBucket[init_numatoms];
		for (int i = 0; i < bucket_fle.length; i++) {
			System.err.println("Making bucket: "+bucket_fle[i]);
			buckets[i] = new RubFileBucket(frontend,bucket_fle[i]);
		}

	}

	private void generateRubFiles() throws IOException {
		if (regenrubs) {
			int countFacts = 0;
			PrintWriter declarations = makeFile(declarations_fle);
		
			for (int i = 0; i < test_preds.length; i++) {
				declarations.print(test_preds[i]+" :: ");
		
				for (int j = 0; j < pred_arity[i]; j++) {
					if (j>0)
						declarations.print(", ");
					declarations.print("String");
				}
				declarations.println();
		
				declarations.print("PERSISTENT MODES (");
				for (int j = 0; j < pred_arity[i]; j++) {
					if (j>0)
						declarations.print(", ");
					declarations.print("F");
				}
				declarations.println(") IS NONDET END");
			}
			declarations.close();
		
			PrintWriter[] bucket = new PrintWriter[bucket_fle.length];
			for (int i = 0; i < bucket.length; i++) {
				bucket[i] = makeFile(bucket_fle[i]);
			}
		
			for (int currPred = 0; currPred < test_preds.length; currPred++) {
				int[] currAtom = new int[pred_arity[currPred]];
				for (int i = 0; i < currAtom.length; i++) {
					currAtom[i] = 0;
				}
				boolean stop = false;
				while (!stop) {
					int currBucket = currAtom[currAtom.length-1];
					bucket[currBucket].print(test_preds[currPred]+"(");
					for (int i = 0; i < currAtom.length; i++) {
						if (i>0)
							bucket[currBucket].print(",");
						bucket[currBucket].print(test_atoms[currAtom[i]]);			
					}
					bucket[currBucket].println(").");

					countFacts++;
				
					stop = nextParamList(0,currAtom);
				}
			}

			for (int i = 0; i < bucket.length; i++) {
				bucket[i].close();
			}
			System.err.println("========= generated "+countFacts+" test facts =======");
		}
	}

  	protected void setUp() throws Exception {
  		bucketLoads = 0;
  		
		// Make some atoms for the test facts:
		test_atoms = new String[init_numatoms];
		initial_test_atoms = test_atoms;
		for (int i = 0; i < test_atoms.length; i++) {
			test_atoms[i] = test_preds[i%test_preds.length]+i;
		}

		pred_arity = new int[test_preds.length];
		for (int i = 0; i < test_preds.length; i++) {
			int arity = i%maxarity+1; 
			pred_arity[i]=arity;
		}
		
		// Initialize bucket_fle names array
		bucket_fle = new String[test_atoms.length];
		for (int i = 0; i < test_atoms.length; i++) {
			bucket_fle[i] = test_space+"/"+test_atoms[i]+".rub";
		}

		setUp(false); // false => first time with a clean slate
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	private void makeEmptyDir(File dir) {
		if (dir.exists())
			deleteDir(dir);
		dir.mkdir();
	}

	private boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

	public void testRandomConcurrency() throws Throwable {
		final int workLoadSize 
			 // Short test: runs about 10 minutes on powerPC 1Ghz
			  = 40; 
		     
			 // An hour or so
			 // = 100;
		     
			 // Overnight:
			 //= 400;
		     
		// Something which looks like a concurrency bug in how Java classloader
		// works causes all threads to deadlock when initializing classes concurrently.
		// running a query first makes sure all is well before threads are started. 
		doRandomQueries(1);
		// Create 10 threads that run queries + 1 outdating thread
		TesterThread[] thread = new TesterThread[10];
		for (int i = 0; i < thread.length; i++) {
			thread[i] = new DoRandomThingsThread(""+i,workLoadSize);
			thread[i].start();
			System.out.println("Thread "+i+" started");
		}
		
		for (int i = 0; i < thread.length; i++) {
			thread[i].join();
			if (thread[i].crash!=null)
				throw thread[i].crash;
			System.out.println("Thread "+i+" ended");
		}		
	}

	public void testConcurrentKilling() throws Throwable {
		System.err.println("====== TEST: testConcurrentKilling ===");
		final int workLoadSize = 20;
		final int numThreads = 20;
		
		// For class initializer deadlocking bug
		doRandomQueries(1);
		
		// Create 10 threads that run queries + 1 killing thread
		TesterThread[] thread = new TesterThread[numThreads];
		for (int i = 0; i < thread.length-1; i++) {
			thread[i] = new RandomQueriesThread(i,workLoadSize);
			thread[i].start();
			System.out.println("Thread "+i+" started");
		}
		
		thread[thread.length-1] = new KillingThread("killing");
		thread[thread.length-1].start();
		System.out.println("Killing thread started");
		
		for (int i = 0; i < thread.length; i++) {
			thread[i].join();
			if (thread[i].crash!=null)
				throw thread[i].crash;
			System.out.println("Thread "+i+" ended");
		}		
	}

	public void testReconnecting() throws Exception {

			int expectedBucketLoads = numbuckets();

			doSomeQueries();
			setUp(true); // setup again with reconnection to persitent facts.
			doSomeQueries();
			for (int i=2;i<=5;i++) {
				expectedBucketLoads += outdateSomebuckets(0,i); 
					// What happens if buckets are outdated before trying to reconnect.
				setUp(true); // setup again with reconnection to persitent facts.
				System.err.println(i);
				doSomeQueries();
			}

			assertEquals("Number of buckets loaded",expectedBucketLoads,bucketLoads);
			
			frontend.backupFactBase();
			//frontend.shutdown();

			doSomeQueries();
			frontend.parse(test_preds[0]+"(ThisIsNewAfterSave).");
			test_must_succeed(test_preds[0]+"(ThisIsNewAfterSave)");
			
			frontend.crash();
			frontend = null; // Simulate a "crash" no backup performed at end.
			
			setUp(true); // setup again with reconnection to persitent facts.
			doSomeQueries();
			test_must_fail(test_preds[0]+"(ThisIsNewAfterSave)");
	}
	
	
	public void testBackupTestsCase0() throws Exception {
		//CASE 0: Normal case
		bucketLoads = 0;
		doSomeQueries();
		assertEquals(numbuckets(), bucketLoads);
		frontend.backupFactBase();
		frontend.shutdown();
		frontend = null; //it's shutdown now
		setUp(true);
		bucketLoads = 0; 
		doSomeQueries();
		assertEquals(0, bucketLoads);
		
	}
	public void testBackupTestsCase1() throws Exception {
		//CASE 1: Crash Before Backup
		bucketLoads = 0;
		doSomeQueries();
		assertEquals(numbuckets(), bucketLoads);
		frontend.crash();
		frontend = null; //CRASH!#$!#!
		setUp(true);
		bucketLoads = 0; 
		doSomeQueries();
		assertEquals(numbuckets(), bucketLoads);
	}
	
	public void testBackupTestsCase2() throws Exception {
		//CASE 2: Crash After Backup, But nothing has changed
		bucketLoads = 0;
		doSomeQueries();
		assertEquals(numbuckets(), bucketLoads);
		frontend.backupFactBase();
		frontend.crash();
		frontend = null; //CRASH!#$!#!
		setUp(true);
		bucketLoads = 0; 
		doSomeQueries();
		assertEquals(0, bucketLoads);		
	}
	
	public void testBackupTestsCase3() throws Exception {
		//CASE 3: Crash After Backup, some new facts were added, should be gone after restore
		bucketLoads = 0;
		doSomeQueries();
		assertEquals(numbuckets(), bucketLoads);
		frontend.backupFactBase();
		buckets[0].parse(test_preds[0]+"(newnewnew).");
		test_must_succeed(test_preds[0]+"(newnewnew)");
		frontend.crash();
		frontend = null; //CRASH!#$!#!
		setUp(true);
		bucketLoads = 0; 
		doSomeQueries();
        //the database will be inconsistent so it will be deleted and reloaded.
		//Actually... NO IT WON'T. If we crashed really fast before anything is even written to
		//disk after the backup then we have a clean database and no buckets will be
		//loaded.
		assertTrue(bucketLoads==10||bucketLoads==0);
		test_must_fail(test_preds[0]+"(newnewnew)"); // This fact was added after backup, so whatever happens it shouldn't be there
	}
	
	public void testStress() throws ParseException, TypeModeError {
			int expectedBucketLoads = 0;
			for (int space = 5;space <= 1000;space+=50) {
				System.out.println("Run with cache size target = "+space);
				//frontend.setCacheSize(space);
				expectedBucketLoads += outdateSomebuckets(0,1); // clear everything!
				doSomeQueries();
				//frontend.printStatistics();		
			}
			assertEquals("Number of buckets loaded ", expectedBucketLoads, bucketLoads);
	}

	public void testJustSomeQueries() throws ParseException, TypeModeError {
		System.err.println("==== TEST: JustSomeQueries ===");
		doSomeQueries();
		assertEquals("Number of buckets loaded ", numbuckets(), bucketLoads);
	}
	
	public void testBucketKilling() throws ParseException, TypeModeError {
		System.err.println("==== TEST: BucketKilling ===");
		doSomeQueries();
		
		while (test_atoms.length>1) {
			shrinkAtoms();
			doSomeQueries();
		}
		
	}

	public void testBucketKillingMany() throws ParseException, TypeModeError {
		System.err.println("==== TEST: BucketKillingMany ===");
		doSomeQueries();
		
		while (test_atoms.length>1) {
			shrinkAtoms();
		}

		doSomeQueries();
		
	}

	private void shrinkAtoms() {
		RuleBaseBucket toDestroy = buckets[test_atoms.length-1];
		synchronized (frontend) {
			toDestroy.destroy(); 
			   // must do it here or the test
			   // could fail simply because the test_atoms are not in synch
			   // with the actual buckets present in the system.
			   // this may cause result prediction to be wrong.
			System.err.println("Destroying bucket: "+toDestroy);
			String[] old = test_atoms;
			test_atoms = new String[old.length-1];
			System.arraycopy(old,0,test_atoms,0,test_atoms.length);
		}
	}

	private void doSomeQueries() throws ParseException, TypeModeError {
		// Queries with all vars
		for (int i = 0; i < test_preds.length; i++) {
			int predictCount = 1;
			String query = test_preds[i] + "(";
			for (int j = 0; j < pred_arity[i]; j++) {
				if (j > 0)
					query += ",";
				query += "?x" + j;
				if (j+1==pred_arity[i]) // last argument
					// adjust count for deleted buckets
					predictCount *= test_atoms.length; 
				else
					predictCount *= init_numatoms;
			}
			query += ")";
			System.err.println(query);
			if (predictCount>0) {
				test_must_succeed(query);
			}
			test_resultcount(query,predictCount);
		}

		//Queries with one var only (at the beginning)
		for (int i = 0; i < test_preds.length; i++) {
			String query = test_preds[i] + "(?x";
			for (int j = 1; j < pred_arity[i]; j++) {
				query += "," + test_atoms[j * 113 % test_atoms.length];
			}
			query += ")";
			System.out.println(query);
			if (pred_arity[i]>1)
				test_must_findall(query, "?x", initial_test_atoms);
			else // must account for deleted buckets that have the atoms
 				// in last arg
				test_must_findall(query, "?x", test_atoms);
		}
		//System.gc(); // to get rid of finalizers
	}
	
	public void testBucketOutdating() throws ParseException, TypeModeError {
			int expectedBucketLoads = numbuckets();

			doSomeQueries();

			expectedBucketLoads += outdateSomebuckets(0,1); // outdate all buckets
			doSomeQueries();
	
			expectedBucketLoads += outdateSomebuckets(1,2); // outdate odd buckets
			doSomeQueries();
	
			expectedBucketLoads += outdateSomebuckets(0,2); // outdate even buckets
			doSomeQueries();
	
			expectedBucketLoads += outdateSomebuckets(1,4); // buckets 1,5,9,...
			doSomeQueries();
	
			assertEquals("Number of buckets loaded ", expectedBucketLoads, bucketLoads);
	}    
    
	private int numbuckets() {
		return test_atoms.length;
	}

	void doRandomQueries(int howmany) throws ParseException, TypeModeError {
		Random rnd = new Random();
		for (int i=0;i<howmany;i++) {
			doRandomQuery(rnd);
		}
	}

	private void doRandomQuery(Random rnd) throws ParseException, TypeModeError {
		int prednum = rnd.nextInt(test_preds.length);
		String query = test_preds[prednum]+"(";
		int predicted_results = 1;
		boolean var = false;
		int atom = -1;
		int atoms_at_start;
		synchronized (frontend) {
			atoms_at_start = test_atoms.length;
			for (int argNum = 0; argNum < pred_arity[prednum]; argNum++) {
				if (argNum > 0)
					query += ",";
				var = rnd.nextBoolean();
				atom = rnd.nextInt(test_atoms.length);
				if (var) {
					query += "?x" + argNum;
					predicted_results *= init_numatoms;
				} else
					query += test_atoms[atom];
			}
			query += ")";

		}
		System.err.println("Doing Query");
		if (var) // last is a var so...
			test_must_succeed(query); 
			  // the above test does not nicely release its source
			  // so is a bit harder on the system and will
			  // cause finalzers to run and release things later
			  // possibly concurrently
			  // the system should be able to deal with this!
			
		int results = get_resultcount(query);
		synchronized (frontend) {
			int kill_adjusted = kill_adjust_predicted(predicted_results,atoms_at_start,var,atom);
			// maybe some more atoms got deleted in the mean time
			if (results != kill_adjusted)
				System.err.println("Q = "+query+" R = "+results+" P = "+kill_adjusted+" A = "+atoms_at_start);
			while (results != kill_adjusted && atoms_at_start > test_atoms.length) {
				kill_adjusted =
					kill_adjust_predicted(predicted_results,--atoms_at_start,var,atom);
				System.err.println("retry Q = "+query+" R = "+results+" P = "+kill_adjusted+" A = "+atoms_at_start);
				if (results == kill_adjusted)
					return;
			}
			Assert.assertEquals("Result count wrong for "+query,kill_adjusted,results);
		}
		System.err.println("Done Query");
	}

	private int kill_adjust_predicted(
		int predicted_results,
		int numatoms,
		boolean var,
		int atom) {
		int kill_adjusted;
		kill_adjusted = 
			var ? predicted_results / init_numatoms * numatoms
				: predicted_results ;
		if (!var && atom>=numatoms) // the atom in the query was deleted
			kill_adjusted = 0;
		return kill_adjusted;
	}
	
	public void testConcurrentQueries() throws Throwable {
	    
		//deadlock bug workaround
		doRandomQueries(1);
	    
		RandomQueriesThread[] thread = new RandomQueriesThread[10];
		for (int i = 0; i < thread.length; i++) {
			thread[i] = new RandomQueriesThread(i,10);
			thread[i].start();
			System.out.println("Thread "+i+" started");
		}
		for (int i = 0; i < thread.length; i++) {
			thread[i].join();
			if (thread[i].crash!=null)
				throw thread[i].crash;
			System.out.println("Thread "+i+" ended");
		}		
	}

	public void testConcurrentOutdating() throws Throwable {
		final int workLoadSize = 20;
		
		//deadlock bug workaround
		doRandomQueries(1);
		
		// Create 10 threads that run queries + 1 outdating thread
		TesterThread[] thread = new TesterThread[11];
		for (int i = 0; i < thread.length-1; i++) {
			thread[i] = new RandomQueriesThread(i,workLoadSize);
			thread[i].start();
			System.out.println("Thread "+i+" started");
		}
		
		thread[thread.length-1] = new OutdatingThread("outdating",workLoadSize);
		thread[thread.length-1].start();
		System.out.println("Outdating thread started");
		
		for (int i = 0; i < thread.length; i++) {
			thread[i].join();
			if (thread[i].crash!=null)
				throw thread[i].crash;
			System.out.println("Thread "+i+" ended");
		}		
	}

	/**
	 * Returns how many buckest where outdated
	 */
	private int outdateSomebuckets(int ofset, int mod) {
		int count = 0;
		for (int i = ofset; i < buckets.length; i+=mod) {
//			System.out.println("Outdating "+i);
			buckets[i].setOutdated();
			count++;
		}
		return count;
	}

	private boolean nextParamList(int i,int[] currAtom) {
		if (i>=currAtom.length)
			return true;
		else {
			currAtom[i] = (currAtom[i]+1)%test_atoms.length;
			if (currAtom[i]==0)
				return nextParamList(i+1,currAtom);
			else
				return false;
		}
	}

	private static PrintWriter makeFile(String name) {
		File path = new File(name);
		try {
			return new PrintWriter(new FileWriter(path));
		} catch (IOException e) {
			throw new Error("Error making logfile: "+e.getMessage());
		}
	}

	class RubFileBucket extends RuleBaseBucket {
		
		String myfile;
		
		RubFileBucket(FrontEnd fe,String filename) {
			super(fe,filename);
			myfile = filename;
		}

		public void update() throws ParseException, TypeModeError {
			try {
				//System.out.println("Bucket update: "+myfile);
				load(myfile);
				bucketLoads++;
			}
			catch (IOException e) {
				throw new Error("IOError for file "+myfile+": "+e.getMessage());
			}
		}
		
		public String toString() {
			return "RubFileBucket("+myfile+")";
		}

	}

}
