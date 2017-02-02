package tyRuBa.engine;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

import tyRuBa.engine.factbase.*;
import tyRuBa.modes.BindingList;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Mode;
import tyRuBa.modes.PredInfo;
import tyRuBa.modes.PredicateMode;
import tyRuBa.modes.TupleType;
import tyRuBa.modes.TypeModeError;

/** 
 * A ModedRuleBaseCollection is a collection of ModedRuleBases that have
 * the same predicate identifier.  Each ModedRuleBase is distinguished from
 * the others by its predicate mode
 */

public class ModedRuleBaseCollection {
	
	private QueryEngine engine;
	ArrayList/*<ModedRuleBase>*/ modedRBs = new ArrayList/*<ModedRuleBase>*/();
	/** All the ground facts go in here, no separation based on modes! */
	FactBase facts = null;
    private PredicateIdentifier predId;
    private FactLibraryManager factLibraryManager;
	
	/** 
	 * Keeps the unconverted rules so that we can create more
	 * ModedRuleBases on the fly (and insert all rules into them).
	 * This ArrayList contains instances of InsertionInfo.
	 */
	ArrayList/*<InsertionInfo>*/ unconvertedRules = new ArrayList/*<InsertionInfo>*/();
	
	private class InsertionInfo {
		RBComponent rule;
		ModedRuleBaseIndex conversionContext;
		TupleType resultTypes;
		
		InsertionInfo(RBComponent r, ModedRuleBaseIndex c, TupleType t) {
			rule = r; 
			conversionContext = c;
			resultTypes = t;
		}
		
		public String toString() {
			return rule.toString();
		}

		/**
		 * @vsf+ RuleBaseCollectionBug
		 */
		public boolean isValid() {
			return rule.isValid();
		}
	}
		
	public ModedRuleBaseCollection(QueryEngine qe, PredInfo p, String identifier) {
		this.engine = qe;
		this.facts = p.getFactBase();
        this.predId = p.getPredId();
        this.factLibraryManager = engine.frontend().getFactLibraryManager();
		
		for (int i = 0; i < p.getNumPredicateMode(); i++) {
			PredicateMode pm = p.getPredicateModeAt(i);
			modedRBs.add(makeEmptyModedRuleBase(pm, facts));
		}
	}

	/** 
 	* Helper method for 'on the fly' creation of an additional ModedRuleBase
 	* for a mode which is not declared, but can be supported as a special
 	* case of a declared mode.
 	*
 	* @vsf+ RuleBaseCollectionBug
 	*/
	private RuleBase newModedRuleBase(PredicateMode pm, FactBase facts) {
		// haha!
		ModedRuleBase result = makeEmptyModedRuleBase(pm, facts);
		modedRBs.add(result); // important to add this before starting conversion of rules
		                      // otherwise => infinite loop if converted rule recurses to same mode!
		for (Iterator iter = unconvertedRules.iterator(); iter.hasNext();) {
			InsertionInfo insertion = (InsertionInfo) iter.next();
			if (!insertion.isValid())
				iter.remove();
			else {
				try {
					result.insert(
							insertion.rule, insertion.conversionContext, insertion.resultTypes);
				} catch (TypeModeError e) {
					e.printStackTrace();
					throw new Error("Cannot happen because all the rules have already been inserted before");
				}
			}
		}
		return result;
	}

	private ModedRuleBase makeEmptyModedRuleBase(PredicateMode pm, FactBase facts) {
		ModedRuleBase result = new ModedRuleBase(engine,pm, facts, factLibraryManager, predId);
		return result;
	}

	public int HashCode() {
		return modedRBs.hashCode() * 17 + 4986;
	}

	public void insertInEach(RBComponent r, ModedRuleBaseIndex rulebases,
	TupleType resultTypes) throws TypeModeError {
		if (r.isGroundFact()) {
			facts.insert(r);
		} else {
		    if (!facts.isPersistent()) {
		        unconvertedRules.add(new InsertionInfo(r, rulebases, resultTypes));
		        int size = modedRBs.size();
		        for (int i = 0; i < size; i++) {
		            ((RuleBase)modedRBs.get(i)).insert(r, rulebases, resultTypes);
		        }
		    } else {
		        throw new Error("Rules cannot be added to persistent factbases");
		    }
		}
	}
	
	/** return the ModedRuleBase in this collection that has the "best"
	 *  mode of execution that allow for bindings */
	public RuleBase getBest(BindingList bindings) {
		RuleBase result = null;
		for (int i = 0; i < modedRBs.size(); i++) {
			RuleBase currRulebase = (RuleBase)modedRBs.get(i);
			BindingList currBindings = currRulebase.getParamModes();
			if (currBindings.equals(bindings)) {
				//Must make sure that if there is an exact bindings match, it is returned
				return currRulebase; 
			} else if (bindings.satisfyBinding(currBindings)) {
				if (result == null) {
					result = currRulebase;
				} else if (currRulebase.isBetterThan(result)) {
					result = currRulebase;
				}
			}
		}
		if (result == null || result.getParamModes().equals(bindings)) {
			return result;
		} else {
			if (bindings.hasFree()) {
				// If it is not an exact match make new one on the fly!
				result = newModedRuleBase(
					Factory.makePredicateMode(bindings, result.getMode().moreBound(), false),
					facts);
				return result;
			}
			else {
				result = newModedRuleBase(
					Factory.makePredicateMode(bindings, Mode.makeSemidet(), false),
					facts);
				return result;
			}
		}		
	}
	
	public void dumpFacts(PrintStream out) {
		out.print(facts);
	}

    public void backup() {
        facts.backup();
    }
	
}
