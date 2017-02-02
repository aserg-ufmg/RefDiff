/*
 * Created on May 28, 2003
 */
package tyRuBa.modes;

import java.util.Hashtable;
import java.util.Iterator;

import tyRuBa.engine.RBVariable;

/**
 * A BindingEnv stores RBVariables that become bound during execution
 */

public class BindingEnv extends Hashtable implements Cloneable {

	public BindingMode getBindingMode(RBVariable var) {
		return (BindingMode)get(var);
	}
	
	public BindingMode putBindingMode(RBVariable var, BindingMode bindingmode) {
		return (BindingMode)put(var, bindingmode);
	}
	
	public Object clone() {
		BindingEnv cl = (BindingEnv) super.clone();
		return cl;
	}

	/** return a BindingEnv containing only RBVariables that are bound in
	 *  both this and other */
	public BindingEnv intersection(BindingEnv other) {
		BindingEnv result = (BindingEnv) this.clone();
		for (Iterator iter = result.keySet().iterator(); iter.hasNext();) {
			RBVariable var = (RBVariable) iter.next();
			if (!other.isBound(var)) {
				iter.remove();
			}
		}
		return result;
	}
	
	public boolean isBound(RBVariable var) {
		BindingMode result = getBindingMode(var);
		return (result != null);
	}
	
}
