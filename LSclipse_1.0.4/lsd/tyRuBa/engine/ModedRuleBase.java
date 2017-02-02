package tyRuBa.engine;

import java.util.Vector;

import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.engine.factbase.*;
import tyRuBa.modes.BindingList;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Mode;
import tyRuBa.modes.PredicateMode;
import tyRuBa.modes.TupleType;
import tyRuBa.modes.TypeModeError;

/** 
 * A ModedRuleBase is a rulebase that has a mode associate to it. A Rulebase
 * stores a collection of Logic inference rules.
 */
public class ModedRuleBase extends RuleBase {

	private RBComponentVector rules = null;
	private FactBase          facts;
	private Mode			  currMode = null;
	private Vector[]		  currTypes = null;
    private PredicateIdentifier predId;
    private FactLibraryManager libraryManager;
	
	/** Constructor */
	public ModedRuleBase(QueryEngine engine,
		PredicateMode predMode, 
		FactBase allTheFacts, FactLibraryManager libraryManager,PredicateIdentifier predId)
	{
		super(engine,predMode,allTheFacts.isPersistent());
		this.facts = allTheFacts;
        this.predId = predId;
        this.libraryManager = libraryManager;
		ensureRuleBase();
		currTypes = new Vector[predMode.getParamModes().getNumBound()];
		for (int i = 0; i < currTypes.length; i++) {
			currTypes[i] = new Vector();
		}
	}

	public void insert(RBComponent r, ModedRuleBaseIndex insertedFrom,
	TupleType inferredTypes) throws TypeModeError {
		try {
			PredicateMode thisRBMode = getPredMode();
			RBComponent converted = r.convertToMode(thisRBMode,
				Factory.makeModeCheckContext(insertedFrom));
				
			// do not do this test if this rule base belong to a "trusted" bucket
			// or the predicate mode is a "REALLY IS" mode
			if (getPredMode().toBeCheck()) {
				BindingList bindings = thisRBMode.getParamModes();
				TupleType boundTypes = Factory.makeTupleType();

				// get only the types of the terms that are bound
				for (int i = 0; i < bindings.size(); i++) {
					if (bindings.get(i).isBound()) {
						boundTypes.add(inferredTypes.get(i));
					}
				}

				if (currMode == null) {
					currMode = converted.getMode();
					for (int i = 0; i < currTypes.length; i++) {
						currTypes[i].add(boundTypes.get(i));
					}
				} else if (currTypes.length == 0) {
					// if there are no bound arguments in this predicate, then there
					// is never any overlap, and the inferred mode is incremented to be
					// the converted component's inferred mode
					currMode = currMode.add(converted.getMode());
				} else {
					boolean hasOverlap = true;
					for (int i = 0; i < currTypes.length; i++) {
						hasOverlap = boundTypes.get(i)
							.hasOverlapWith(currTypes[i], hasOverlap);
					}
					if (hasOverlap && currTypes.length > 0) {
						currMode = currMode.add(converted.getMode());
					} else {
						currMode = currMode.noOverlapWith(converted.getMode());
					}
				}
				
				if (currMode.compatibleWith(getMode())) {
					privateInsert(converted, insertedFrom);
				} else {
					throw new TypeModeError(
						"Inferred mode exceeds declared mode in " + 
						converted.getPredName() + "\n" +
						"inferred mode: " + currMode + "	declared mode: " + getMode());	
				}
			} else {
				privateInsert(converted, insertedFrom);
			}
		} catch (TypeModeError e) {
			throw new TypeModeError("while converting " + r + " to mode: "
				+ getPredMode() + "\n" + e.getMessage());
		}
	}

	/**
	 * This is called *after* converting into mode
	 */
	private void privateInsert(RBComponent converted, ModedRuleBaseIndex insertedFrom)  
	throws TypeModeError {
		ensureRuleBase();
		rules.insert(converted);
	}
	
	private void ensureRuleBase() {
		if (rules == null) {
			rules = new RBComponentVector();
		}
	}

	public String toString() {
		return "/******** BEGIN ModedRuleBase ***********************/\n"
			+ "Predicate mode: " + getPredMode() + "\n"
			+ "Inferred mode: " + currMode + "\n"
			+ rules + "\n"
			+ "/******** END ModedRuleBase *************************/";
	}

	public int hashCode() {
		throw new Error("That's strange... who wants to know my hashcode??");
	}

	protected Compiled compile(CompilationContext context) {
		if (rules != null) {
            if (isPersistent()) {
                return facts.compile(getPredMode(), context).disjoin(libraryManager.compile(getPredMode(), predId, context)).disjoin(rules.compile(context));    
            } else {
                return facts.compile(getPredMode(), context).disjoin(rules.compile(context));
            }
			
		} else {
            if (isPersistent()) {
                return facts.compile(getPredMode(), context).disjoin(libraryManager.compile(getPredMode(), predId, context));    
            } else {
                return facts.compile(getPredMode(), context);
            }
			
		}
	}

}
