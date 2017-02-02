package tyRuBa.engine;

import java.io.File;

import junit.framework.Assert;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.util.Files;

/**
 * A RuleBaseBucekt is conceptual set of fact/rules that are added to a
 * FrontEnd. 
 * 
 * A RuleBaseBucket should know how to update its facts when asked to do
 * so by calling update(). Therefore, when using RuleBaseBuckets you are
 * expected to make a subclass and implement the "update" method accordingly.
 * 
 * A rulebasebucket has all capabilities of a query engine. It behaves largely
 * as an alias of its associated FrontEnd.
 * 
 * Note: the RuleBaseBucket outdated flag is already set when it is created.
 * Thus, the first time the contents of the bucket is needed it will 
 * be requested to update itself to generate the facts for the first time.
 */

public abstract class RuleBaseBucket extends QueryEngine {

	private static int tmpBuckets = 0;

	//became part of Validator:
	//boolean outdated;
	Validator validator;
	String identifier;
	boolean temporary;

	FrontEnd frontend;
	BucketModedRuleBaseIndex rulebase;

	public void setOutdated() {
		synchronized (frontend) {
		    frontend.getFrontEndValidatorManager().update(validator.handle(), new Boolean(true), null);
			frontend.someOutdated = true;
		}
	}

	/**
	 * Constructor RuleBucket.
	 * @param engine
	 */
	public RuleBaseBucket(FrontEnd frontend, String identifyingString) {
		this.frontend = frontend;
		Assert.assertTrue(frontend!=null);
		
		if(identifyingString == null) {
			this.temporary = true;
			this.identifier = "TMP_" + tmpBuckets++;
			//this.outdated = true;
			this.validator = frontend.obtainGroupValidator(identifier,temporary);
		} else {
			this.temporary = false;
			this.identifier = identifyingString;
			//this.outdated = !frontend.groupExists(identifier);
			this.validator = frontend.obtainGroupValidator(identifier,temporary);
		}
		
		frontend.addBucket(this);
		rulebase = new BucketModedRuleBaseIndex(this, this.identifier,
			(BasicModedRuleBaseIndex)frontend.rulebase());
	}

	/** Add a fact into this bucket */
	public void insert(RBComponent t) throws TypeModeError {
		super.insert(new ValidatorComponent(t, validator));
	}

//	/** Add a fact into this bucket */
//	public void insert(RBRule r) {
//		throw new Error("Not supported in this version");
////		if (nonFacts == null) {
////			nonFacts = new RBComponentVector();
////			frontend.ruleBase().insert(nonFacts);
////		}
////		nonFacts.insert(r);
//	}
	
	/** Gets the storage path for this query engine (will be where
	 * the factbase is stored
	 */
	public String getStoragePath() {
	    return frontend.getStoragePath() + "/" + identifier;
	}
	
	public String getIdentifier() {
	    return identifier;
	}

	
	protected void clear() {
		// Invalidate the things stored in the frontend directly
		validator.invalidate();
		frontend.getFrontEndValidatorManager().remove(validator.handle());
		validator = frontend.obtainGroupValidator(identifier, temporary);
		// And also clear out the stuff in our componentVector
		rulebase.clear();
		frontend.flush();
	}

	public FrontEnd frontend() {
		return frontend;
	}

	public boolean isOutdated() {
		return validator.isOutdated();
	}
	
	public boolean isTemporary() {
	    return temporary;
	}

	/**
	 * This method must be overriden. It will be called by the query engine
	 * when it determines that the contents of the bucket is needed after it has
	 * been "setOutdated". This method has to regenerate all the facts in this
	 * bucket and reinsert them into the bucket. This method should never
	 * be called directly, only by doUpdate, which is triggered by the QueryEngine.
	 */
	protected abstract void update() throws TypeModeError, ParseException;

	void doUpdate() throws TypeModeError, ParseException {
		update();
		frontend.getFrontEndValidatorManager().update(validator.handle(), new Boolean(false), null);
	}
	
	ModedRuleBaseIndex rulebase() {
		return rulebase;
	}

	public void destroy() {
		synchronized (frontend) {
			FrontEnd holdOn = frontend;
			frontend.getSynchPolicy().stopSources();
			try {
				clear();
				frontend.removeBucket(this);
				//if it was a temporary bucket, delete the database (if it exists)
				if (temporary) {
			        File f = new File(getStoragePath());
			        Files.deleteDirectory(f);
				}
				frontend = null;
				rulebase = null;
				validator = null;
			}
			finally {
				holdOn.getSynchPolicy().allowSources();		
			}
		}
	}
	
	public void backup() {
	    rulebase.backup();
	}

	/**
	 * @codegroup metadata
	 */
	public void enableMetaData() {
		rulebase.enableMetaData();
	}
}
