/**
 * This FrontEnd class provides a frontend for using the tyruba engine. 
 * 
 */

package tyRuBa.engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import tyRuBa.engine.factbase.FactLibraryManager;
import tyRuBa.engine.factbase.FileBasedValidatorManager;
import tyRuBa.engine.factbase.NamePersistenceManager;
import tyRuBa.engine.factbase.ValidatorManager;
import tyRuBa.modes.ConstructorType;
import tyRuBa.modes.PredInfo;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.util.Aurelizer;
import tyRuBa.util.ElementSource;
import tyRuBa.util.SynchPolicy;
import tyRuBa.util.SynchResource;
import tyRuBa.util.pager.Pager;

public class FrontEnd extends QueryEngine 
implements SynchResource {
	
	public long updateCounter = 0;

	/** Set to true when at least one bucke becomes outdated */
	boolean someOutdated = false;
	
	ProgressMonitor progressMonitor = null;

	/** A rulebase to store facts and rules and run queries */
	private BasicModedRuleBaseIndex rules;
//	private PredInfoProvider predinfo;

	/** The PrintStream where output will go */
	PrintStream os = System.out;

	private Collection holdingPen = makeBucketCollection();
	    // where buckets are temporarily held before adding to myBuckets.
	private Collection myBuckets = makeBucketCollection();
	
	private int progressBar = 0;
	private int updatedBuckets = 0;

    /** Global pager for all frontends */
    private static Pager pager;
	
	private ValidatorManager validatorManager;
	private NamePersistenceManager namePersistenceManager;
	private FactLibraryManager factLibraryManager;
	
	private File path;
	private String identifier;
	public static final int defaultPagerCacheSize = 5000;
    private static final int defaultPagerQueueSize = 1000;
	private long lastBackupTime;
	
	/**
	 * @codegroup metadata
	 */
	public FrontEnd(boolean loadInitFile, File path, boolean persistent, ProgressMonitor mon,boolean clean,boolean enableBackgroundCleaning) {
        
        progressMonitor = mon;
        this.path = path;

        if (pager != null) {
            pager.shutdown();
        }
        
        if (!checkAndFixConsistency()) {
		    clean = true;
        }

        if (clean) {
            deleteDirectory(path);
        }
        pager = new Pager(defaultPagerCacheSize,defaultPagerQueueSize, lastBackupTime, enableBackgroundCleaning);

        if (!path.exists()) {
        		path.mkdirs();
        }
        
        try {
    			new File(path.getPath() + "/running.data").createNewFile();
        } catch (IOException e) {
            throw new Error("Could not create running \"lock\" file");
        }

        this.validatorManager = new FileBasedValidatorManager(path.getPath());
		this.namePersistenceManager = new NamePersistenceManager(path.getPath());
		this.factLibraryManager = new FactLibraryManager(this);
		
		this.identifier = "**frontend**";
		 
		if(persistent)
			rules = RuleBase.make(this, "GLOBAL", false);
		else
			rules = RuleBase.make(this);
		RuleBase.silent = true;
		try {
//			parse("__meta(version(5,14,unofficial)).");
//			insert(
//				makeCompound(
//					makeName("__meta"),
//					makeCompound(makeName("self"), makeObject(rules))));
//			if (RuleBase.silent)
//				parse("__meta(option(silent)).");
//			if (RuleBase.useCache)
//				parse("__meta(option(cache)).");
//			if (RuleBase.softCache)
//				parse("__meta(option(softcache)).");
			System.err.println("Loading metabase decls");
			parse(MetaBase.declarations);
			MetaBase.addTypeMappings(this);
			System.err.println("DONE Loading metabase decls");

			if (loadInitFile) {
				boolean silent = RuleBase.silent;
				RuleBase.silent = true;
				NativePredicate.defineNativePredicates(this);
				URL initfile =
					this.getClass().getClassLoader().getResource(
						"lib/initfile.rub");
				load(initfile);
				RuleBase.silent = silent;
			}
		} catch (ParseException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} catch (TypeModeError e) {
			System.err.println(e.getMessage());
		}
	}

	private static Collection makeBucketCollection() {
		return new LinkedHashSet();
	}

	/** If loadInitFile is true then the FrontEnd will automatically
	 * load tyruba's standard init files included in its jar file.
	 **/
	public FrontEnd(boolean loadInitFile) {
		this(loadInitFile, new File("./fdb/"), false, null, false, false);
	}
	
	public FrontEnd(boolean loadInitFile, ProgressMonitor mon) {
		this(loadInitFile, new File("./fdb/"), false, mon, false, false);
	}
	
	/**
     * @param initfile
     * @param clean  Force old files to be deleted, start with a "clean" database.
     */
    public FrontEnd(boolean initfile, boolean clean) {
        this(initfile, new File("./fdb/"), false, null, clean,false);
    }

    public void setCacheSize(int cacheSize) {
		pager.setCacheSize(cacheSize);
	}
	
	public int getCacheSize() {
	    return pager.getCacheSize();
	}

	private boolean checkAndFixConsistency() {
        boolean result = true;
		String stPath = path.getPath(); 
		File crashed = new File(stPath + "/running.data");
		File checkFile = new File(stPath + "/lastBackup.data");
		if (checkFile.exists()) {
			lastBackupTime = checkFile.lastModified();
		} else {
			lastBackupTime = -1;
		}
        System.err.println("Checking consistency...");
		if (crashed.exists()) {
            System.err.println("System was running.");
			//the system crashed, it may be in an inconsistent state
			if (!checkFile.exists()) {
				//it crashed and we hadn't backed up yet, *delete* the old data
				System.err.println("We hadn't backed up before....");
				System.err.println("Data is in an inconsistent state, must delete everything.");
				deleteDirectory(path);
				path.mkdirs();
			} else {
				long backupTime = checkFile.lastModified();
				//it crashed, but we called backup before, so we can check/restore the backups
				System.err.println("Trying to restore backup files.");
				if (!restoreBackups(path,backupTime)) {
					System.err.println("Data is in an inconsistent state, must delete everything.");
					deleteDirectory(path);
					path.mkdirs();
					result = false;
				} else {
					System.err.println("Restoring backup file successfull!");
					//set the "last backup time" to now because the state of the engine
					//is the same as right after a backup.
		    		try {
		    			FileWriter fw = new FileWriter(checkFile, false);
		    			fw.write("Nothing to see here... move along. ;)");
		    			fw.close();
		    		} catch (IOException e) {
		    			throw new Error("Could not create backup file");
		    		}
					lastBackupTime = checkFile.lastModified();
				}
			}
			crashed.delete();
		}
        return result;		
	}
	
	public long getLastBackupTime() {
		return lastBackupTime;
	}
	
	private boolean restoreBackups(File f, long backupTime) {
		if (f.isDirectory()) {
			boolean success = true;
			File[] files = f.listFiles();
			for (int i = 0; i < files.length; i++) {
				success &= restoreBackups(files[i], backupTime);
			}
			return success;
		} else {
			String name = f.getName();
			if (!(name.endsWith(".bup") || name.endsWith(".data"))) {
				long fileModifiedTime = f.lastModified();
				if (fileModifiedTime > backupTime) {
					File backup = new File(f.getAbsolutePath() + ".bup");
					if (backup.exists()) {
						f.delete();
						backup.renameTo(f);
						return true;
					} else {
						//bad stuff, can't restore backups.  crash must have happened in the middle of an update.
						System.err.println(f.getAbsolutePath() + ": " + backupTime + " ::: " + fileModifiedTime);
						return false;
					}
				} else {
					return true;
				}
			} else {
				return true;
			}			
		}
	}
	
	private void deleteDirectory(File dir) {
	    if (dir.isDirectory()) {
	        File[] children = dir.listFiles();
	        for (int i = 0; i < children.length; i++) {
	            deleteDirectory(children[i]);
	        }
	    }  
	    dir.delete();
	}
	
	Validator obtainGroupValidator(Object identifier,boolean temporary) {
	    if (!(identifier instanceof String)) throw new Error("[ERROR] - obtainGroupValidator - ID needs to be a String");
	    Validator result = validatorManager.get((String) identifier);
	    
	    if (result != null) {
	        if (!result.isValid()) {
	            validatorManager.remove((String) identifier);
	            result = null;
	        }
	    }
	    
	    if (result == null) {
	        result = new Validator();
            validatorManager.add(result, (String) identifier);
	    } 
	    
	    return result;
	}

	/**
	 * This method is like backupFactBase, except that it will only perform the backup
	 * in case it is not going to take a long time. Use this method for periodic saves in
	 * interactive applications. 
	 * 
	 * As the query engine is less active it will gradually save data to disk in background,
	 * eventually saving all data to disk. When all page data has been saved, then fastBackup
	 * can run. As long as dirty pages remain in memory fastBackupWill not perform a
	 * backup.
	 * 
	 * Returns a boolean value to indicate whether backup was performed.
	 * @throws BackupFailedException if backup was attempted but failed for some reason.
	 */
	public boolean fastBackupFactBase() throws BackupFailedException {
		if (pager.isDirty())
			return false;
		else {
			backupFactBase();
			return true;
		}
	}
	
	synchronized public void backupFactBase() throws BackupFailedException {
	    System.err.println("[DEBUG] - backupFactBase - Entering Backup Method");
		getSynchPolicy().stopSources(); //wait for all queries to end
		try {
		    System.err.println("[DEBUG] - backupFactBase - Backup: Actually Backing Up");
            //Assert.assertTrue(SynchPolicy.busySources==0);
		    
		    rules.backup();
            Object[] buckets = getBuckets().toArray();
            for (int i = 0; i < buckets.length; i++) {
                RuleBaseBucket bucket = (RuleBaseBucket) buckets[i];
                bucket.backup(); //XXX: This might not be necessary anymore
            }
            
            pager.backup();
            validatorManager.backup();
            namePersistenceManager.backup();
            
            //Assert.assertTrue(SynchPolicy.busySources==0);
            File lastBackup = new File(path.getPath() + "/lastBackup.data");
            if (lastBackup.exists()) {
            	lastBackup.delete();
            }
            
    		try {
    			FileWriter fw = new FileWriter(lastBackup, false);
    			fw.write("Nothing to see here... move along. ;)");
    			fw.close();
    		} catch (IOException e) {
    			throw new Error("Could not create backup file");
    		}
		}
		finally {
			getSynchPolicy().allowSources();
		}
		System.err.println("[DEBUG] - backupFactBase - Done Backup Method");
	}
	
	synchronized public void shutdown() {
	    System.err.println("[DEBUG] - shutdown - Entering shutdown method");
	    setLogger(null); // force my logger (if any) to be closed.
	    getSynchPolicy().stopSources();
	    try {
	    	rules.backup();
	        if (!getBuckets().isEmpty()) {
	        	
                Object[] buckets = getBuckets().toArray();
                for (int i = 0; i < buckets.length; i++) {
                    RuleBaseBucket bucket = (RuleBaseBucket) buckets[i];
                    bucket.setLogger(null); // force any logger to be closed.
                    if (bucket.isTemporary()) {
                        bucket.destroy();
                    }
                }
            }
            pager.backup();
	        validatorManager.backup();
	        namePersistenceManager.backup();

	        //delete the running "lock" file
			new File(path.getPath() + "/running.data").delete();
	    } finally {
	        getSynchPolicy().allowSources();
	    }
	    System.err.println("[DEBUG] - shutdown - Done shutdown method");
	}
	
	public void redirectOutput(PrintStream output) {
		closeOutput();
		os = output;
	}

	/** Flush the query cache */
	public void flush() {
		updateCounter++;
//		rules.update();
	}

//	/** Retract all facts in the factbase that match this term */
//	public void retract(RBTerm t) {
//		// The way we do this by constructing a TyRuBa Expression
//		// that looks like this (where ?t is the parameter t)
//		// ?t,RETRACT(?t)
//		// and then running it as a query.
//		RBExpression doIt = makeAnd(makeTermExpression(t), makeRetract(t));
//		ElementSource result = frameQuery(doIt);
//		result.forceAll(); // evaluated for side effects. Nobody will
//		// look at result, we must force it to be evaluated!
//	}

	public String toString() {
		return "FrontEnd: " + rules;
	}

	// Method below here are factory methods that create Terms and
	// expressions and rules etc. As a convention each of these method's name
	// starts with make

	public static RBTerm makeCompound(ConstructorType cons, RBTerm[] args) {
		return cons.apply(makeTuple(args));
	}

	public static RBVariable makeVar(String name) {
		return RBVariable.make(name);
	}

	public static RBTerm makeTemplateVar(String name) {
		return new RBTemplateVar(name);
	}
	
	public static RBVariable makeUniqueVar(String name) {
		return RBVariable.makeUnique(name);
	}
	
	public static RBVariable makeIgnoredVar() {
		return RBIgnoredVariable.the;
	}
	
	public static RBTerm makeName(String n) {
		return RBCompoundTerm.makeJava(n);
	}
	
	public static RBTerm makeInteger(String n) {
		return RBCompoundTerm.makeJava(new Integer(n));
	}
	public static RBTerm makeInteger(int n) {
		return RBCompoundTerm.makeJava(new Integer(n));
	}

	public static RBTerm makeReal(String n) {
		return RBCompoundTerm.makeJava(new Float(n));
	}

	public static RBConjunction makeAnd(RBExpression e1, RBExpression e2) {
		return new RBConjunction(e1, e2);
	}
	
	public static RBPredicateExpression makePredicateExpression(String pred, RBTerm[] terms) {
		return new RBPredicateExpression(pred,terms);
	}

//	public static RBDuplicatesFilter makeNodup(RBTerm key, RBExpression q) {
//		return new RBDuplicatesFilter(key, q);
//	}

	public static RBTerm makeList(ElementSource els) {
		if (els.hasMoreElements()) {
			RBTerm first = (RBTerm) els.nextElement();
			return new RBPair(first, makeList(els));
		} else
			return theEmptyList;
	}

	public static RBTerm makeList(RBTerm[] elements) {
		return RBPair.make(elements);
	}

	public static RBTuple makeTuple(RBTerm[] elements) {
		return RBTuple.make(elements);
	}

	public static RBTuple makeTuple(ArrayList args) {
		return RBTuple.make(args);
	}

	// the variables below this point are singleton instances.
	// as a convention there names all start with "the"
	public static final RBTerm theEmptyList = RBJavaObjectCompoundTerm.theEmptyList;

	private static final int PROGRESS_BAR_LEN = 100;

	public void finalize() throws Throwable {
		try {
			closeOutput();
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
			if (Aurelizer.debug_sounds!=null)
				Aurelizer.debug_sounds.enter("error");
		}
	}

	private void closeOutput() {
		if (os != null && os!=System.out && os!=System.err)
			os.close();
	}

	/**
	 * @see tyRuBa.engine.QueryEngine#frontend()
	 */
	FrontEnd frontend() {
		return this;
	}

	/** You should not call this, only supposed to be called 
	 * by the buckets constructor */
	synchronized void addBucket(RuleBaseBucket bucket) {
		if (holdingPen==null)
			holdingPen = makeBucketCollection();
		holdingPen.add(bucket);
		someOutdated |= bucket.isOutdated();
	}
	
	/** You should not call this, only supposed to be called 
	 * by the buckets "destroy" method */
	synchronized void removeBucket(RuleBaseBucket bucket) {
		if (! getBuckets().remove(bucket)) 
			throw new Error("Attempted to remove bucket which is not present");		
		Map predMap = bucket.rulebase.localRuleBase.typeInfoBase.predicateMap;
		Iterator it = predMap.values().iterator();
		while (it.hasNext()) {
		    PredInfo pinfo = (PredInfo) it.next();	    
		}
	}

	public Collection getBuckets() {
		if (holdingPen!=null) {
			myBuckets.addAll(holdingPen);
			holdingPen = null;
		}
		return myBuckets;
	}
	
	synchronized private int bucketCount() {
		return myBuckets.size() + (holdingPen==null?0:holdingPen.size());
	}

	ModedRuleBaseIndex rulebase() {
		return rules;
	}

	/** Force an update of all outdated buckets now. */
	synchronized public void updateBuckets() throws TypeModeError, ParseException {
        long startTime = 0;
        progressBar = 0; updatedBuckets = 0;
		if (someOutdated) {
			getSynchPolicy().stopSources();
			try {
                 startTime = System.currentTimeMillis();
			    System.err.println("[DEBUG] - updateBuckets() - start updating buckets");
				if (progressMonitor!=null)
					progressMonitor.beginTask("Updating database",PROGRESS_BAR_LEN);
				Collection bucketColl = getBuckets();
				updateSomeBuckets(bucketColl);
				while (holdingPen!=null) { 
					// holdingPen has received new buckets since update started!
					bucketColl = holdingPen;
					getBuckets(); // forces the holdingPen buckets to be added to main bucket pool and then to null.
					updateSomeBuckets(bucketColl);
				}
				someOutdated = false;
			}
			finally {
				getSynchPolicy().allowSources();
				if (progressMonitor!=null)
					progressMonitor.done();		
				System.err.println("[DEBUG] - updateBuckets() - done updating buckets (" + (System.currentTimeMillis() - startTime) + "ms)");
                pager.printStats();
			}
		}
	}
	
	/**
	 * Perform a partial bucket update. The buckets to be checked and updated are given
	 * by bucketColl.
	 */
	private void updateSomeBuckets(Collection bucketColl) throws TypeModeError, ParseException {
		for (Iterator buckets = bucketColl.iterator(); buckets.hasNext();) {
			RuleBaseBucket bucket = (RuleBaseBucket) buckets.next();
		    	if (bucket.isOutdated()) {
		    		bucket.clear();
		    	}	
		}
		for (Iterator buckets = bucketColl.iterator(); buckets.hasNext();) {
			RuleBaseBucket bucket = (RuleBaseBucket) buckets.next();
			if (bucket.isOutdated()) {
				bucket.doUpdate();
			}
			if (progressMonitor!=null) {
				updatedBuckets++;
				int newProgressBar = updatedBuckets * PROGRESS_BAR_LEN / bucketCount();
				if (newProgressBar>progressBar) {
					progressMonitor.worked(newProgressBar-progressBar);
					progressBar = newProgressBar;
				}
			}
		}
	}

	void autoUpdateBuckets() throws TypeModeError, ParseException {
		if (RuleBase.autoUpdate)
			updateBuckets();
	}

	//////////////////////////////////////////////////////////
	///// For the implementation of SynchResource
	
	private SynchPolicy synchPol = null;
	
	public SynchPolicy getSynchPolicy() {
		if (synchPol==null)
			synchPol = new SynchPolicy(this);
		return synchPol;
	}
	
	////////
	
	
	public String getStoragePath() {
	    return path.getPath();	    
	}
	
	public ValidatorManager getValidatorManager() {
	    return validatorManager;
	}
	
	public NamePersistenceManager getNamePersistenceManager() {
		return namePersistenceManager;
	}
	
	public FactLibraryManager getFactLibraryManager() {
		return factLibraryManager;
	}
    
    public Pager getPager() {
        return pager;
    }
	
	public String getIdentifier() {
	    return identifier;
	}

	/**
	 * Simulate a crash of the query engine. This terminates all threads associated with
	 * the query engine and makes the query engine unusable.
	 * 
	 * Used for testing purposes (see testBackup 
	 */
	public void crash() {
		pager.crash();
		pager = null;
		
	}

	/**
	 * This enables metaData facts for the FrontEnd. 
	 * You must enableMetaData separately for buckets to get
	 * metaData for the declarations entered via buckets.
	 * @codegroup metadata
	 */
	public void enableMetaData() {
		rules.enableMetaData();
	}

}
