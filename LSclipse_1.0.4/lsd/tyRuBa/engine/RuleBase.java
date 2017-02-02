package tyRuBa.engine;

import java.io.PrintStream;

import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.engine.compilation.SemiDetCompiled;
import tyRuBa.modes.BindingList;
import tyRuBa.modes.Mode;
import tyRuBa.modes.Multiplicity;
import tyRuBa.modes.PredicateMode;
import tyRuBa.modes.TupleType;
import tyRuBa.modes.TypeModeError;

/**
 * A RuleBase stores a collection of Logic inference rules and facts.
 * It has a dependency tracking mechanism that holds on to its
 * dependents weakly. Dependents are notified of a change to
 * the rulebase by means of an update message.
 */
public abstract class RuleBase {

	private QueryEngine engine;
	long upToDateWith = -1;

	private boolean uptodateCheck() {
		if (upToDateWith < engine.frontend().updateCounter) { 
			forceRecompilation();
			return false;
		} else {
			return true;
		}
	}

	private void forceRecompilation() {
		compiledRules = null;
		semidetCompiledRules = null;
	}

	private Compiled compiledRules = null;
	private SemiDetCompiled semidetCompiledRules = null;

	/** Use a cache which remember query results ? */
	public static boolean useCache = true;

	/** If true Cache will use SoftValueMap which allows entries to be reclaimed
	  * by the garbage collector when low on memory */
	public static boolean softCache = true;

	/** Do not print Ho. characters while computing queries */
	public static boolean silent = false;

	/** 
	 * This flag controls whether the QueryEngine will
	 * check for Bucket updates before running any query.
	 */
	public static boolean autoUpdate = true;

	static final public boolean debug_tracing = false;
	static final public boolean debug_checking = false;

//	/** A collection of my dependents. They will get an update
//	 * message whenever my contents changes. The collection is
//	 * weak so that dependents can be gc-ed */
//	private WeakCollection dependents = null;
//
//	/** Add a dependend to my Weak collection of dependents */
//	public void addDependent(Dependent d) {
//		if (dependents == null)
//			dependents = new WeakCollection();
//		dependents.add(d);
//	}
//
//	/** Remove a depedent from my Weak collection of dependents */
//	public void removeDependent(Dependent d) {
//		if (dependents != null)
//			dependents.remove(d);
//	}

//	/** Ask all my dependents to update themselves */
//	public void update() {
//		if (dependents != null) {
//			Object[] deps = dependents.toArray();
//			for (int i = 0; i < deps.length; i++) {
//				((Dependent) deps[i]).update();
//			}
//		}
//	}

	private PredicateMode predMode;
	
	/**
	 * @category preparedSQLQueries
	 */
	private boolean isPersistent;

	protected RuleBase(QueryEngine engine,PredicateMode predMode,boolean isSQLAble) {
		this.engine = engine;
		this.predMode = predMode;
		this.isPersistent = isSQLAble;
	}

	/** return the predicate mode of this moded rulebase */
	public PredicateMode getPredMode() {
		return predMode;
	}

	/** return the list of binding modes of the predicate mode */
	public BindingList getParamModes() {
		return getPredMode().getParamModes();
	}

	/** return the expected mode of the predicate mode */
	public Mode getMode() {
		return getPredMode().getMode();
	}

	/**
	 * Returns true if this rulebase mode is expected to produce fewer results than other
	 */
	public boolean isBetterThan(RuleBase other) {
		return getMode().isBetterThan(other.getMode());
	}
	
	public static BasicModedRuleBaseIndex make(FrontEnd frontEnd) {
		return new BasicModedRuleBaseIndex(frontEnd, null);
	}

	/** Factory method for creating RuleBases **/
	public static BasicModedRuleBaseIndex make(FrontEnd frontEnd, String identifier, boolean temporary) {
//		if(identifier != null)
//			engine.addGroup(identifier, temporary);
			
		return new BasicModedRuleBaseIndex(frontEnd, identifier);
	}

	/** Add a rule to the rulebase */
	abstract public void insert(RBComponent r, ModedRuleBaseIndex rulebases, 
	TupleType resultTypes) throws TypeModeError;

	/** Retract a fact from the rulebase. This operation is not supported for
	all kinds of RBComponents or all configurations of RuleBase implementations.
	Not all concrete rulebases support the operation */
	public void retract(RBFact f) {
		throw new Error("Unsupported operation RETRACT");
	}

	/** Don't implement this for most rule bases */
	public RuleBase addCondition(RBExpression e) {
		throw new Error("Operation not implemented");
	}

	public void dumpFacts(PrintStream out) {
	}

	public Compiled getCompiled() {
		uptodateCheck();
		if (compiledRules == null) {
			compiledRules = compile(new CompilationContext());
			if (RuleBase.useCache)
				compiledRules = Compiled.makeCachedRuleBase(compiledRules);
			upToDateWith = engine.frontend().updateCounter;
		}
		return compiledRules;
	}

	public SemiDetCompiled getSemiDetCompiledRules() {
		uptodateCheck();
		if (semidetCompiledRules == null) {
			Compiled compiled = getCompiled();
			if (compiled.getMode().hi.compareTo(Multiplicity.one)>0)
				semidetCompiledRules = compiled.first();
			else
				semidetCompiledRules = (SemiDetCompiled)compiled;
			upToDateWith = engine.frontend().updateCounter;
		}
		return semidetCompiledRules;
	}

	protected abstract Compiled compile(CompilationContext context);

	/**
	* @category preparedSQLQueries
	*/
    public boolean isPersistent() {
        return isPersistent;
    }

}
