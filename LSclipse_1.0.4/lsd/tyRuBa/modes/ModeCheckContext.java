/*
 * Created on May 28, 2003
 */
package tyRuBa.modes;

import java.util.Collection;
import java.util.Iterator;

import tyRuBa.engine.ModedRuleBaseIndex;
import tyRuBa.engine.PredicateIdentifier;
import tyRuBa.engine.RBTuple;
import tyRuBa.engine.RBVariable;
import tyRuBa.engine.RuleBase;

/** 
 * A ModeCheckContext contains a BindingEnv and a ModedRuleBaseIndex used
 * during convertToMode 
 */

public class ModeCheckContext implements Cloneable {

	private BindingEnv bindings;
	private ModedRuleBaseIndex rulebases;
	
	/** Constructor */
	public ModeCheckContext(BindingEnv initialBindings, ModedRuleBaseIndex rulebases) {
		this.bindings = initialBindings;
		this.rulebases = rulebases;
	}

	/** return ModedRuleBase with the "best" mode that allow for term */
	public RuleBase getBestRuleBase(PredicateIdentifier predId,
	RBTuple args, BindingList bindings) {
		for (int i = 0; i < args.getNumSubterms(); i++) {
			bindings.add(args.getSubterm(i).getBindingMode(this));
		}
		RuleBase result = rulebases.getBest(predId, bindings);
		return result;
	}

	public ModedRuleBaseIndex getModedRuleBaseIndex() {
		return rulebases;
	}
	
	public BindingEnv getBindingEnv() {
		return bindings;
	}
	
	/** return true if var becomes bound during execution */
	public boolean isBound(RBVariable var) {
		return getBindingEnv().isBound(var);
	}

	public void removeAllBound(Collection vars) {
		Iterator itr = vars.iterator();
		while (itr.hasNext()) {
			RBVariable curr = (RBVariable)itr.next();
			if (isBound(curr)) {
				itr.remove();
			}
		}
	}
	
	/** variable becomes bound */
	public void makeBound(RBVariable variable) {
		bindings.putBindingMode(variable, Factory.makeBound());
	}
	
	/** all RBVariable in vars become bound */
	public void bindVars(Collection vars) {
		Iterator itr = vars.iterator();
		while (itr.hasNext()) {
			makeBound((RBVariable)itr.next());
		}
	}

	public Object clone() {
		ModeCheckContext cl = new ModeCheckContext(bindings,
			getModedRuleBaseIndex());
		cl.bindings = (BindingEnv)getBindingEnv().clone();
		return cl;
	}

	public String toString() {
		return "---------ModeCheckContext---------\n" 
			+ "Bindings: " + bindings;
	}

	/** return ModeCheckContext consisting only of RBVariables that are bound
	 *  in both this and other */
	public ModeCheckContext intersection(ModeCheckContext other) {
		return new ModeCheckContext(getBindingEnv().intersection(
			other.getBindingEnv()), getModedRuleBaseIndex());
	}

	public boolean checkIfAllBound(Collection boundVars) {
		for (Iterator iter = boundVars.iterator(); iter.hasNext();) {
			RBVariable element = (RBVariable) iter.next();
			if (!isBound(element)) {
				return false;
			}
		}
		return true;
	}
}
