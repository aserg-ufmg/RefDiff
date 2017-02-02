package tyRuBa.engine;

import java.util.Enumeration;
import java.util.Hashtable;

public class Frame extends Hashtable {

	/** Frame maintains a stack of rules that where applied to bind variables
	  in this frame */
	//Stack rules = new Stack();

	/** Add a rule */
	//void pushRule(RBRule r) {
	// rules.push(r);
	//}

	//    public RBTerm get(String s) {
	//	return get(new RBVariable(s));
	//}

	public RBTerm get(RBSubstitutable v) {
		return (RBTerm) super.get(v);
	}

	public Object clone() {
		Frame cl = (Frame) super.clone();
		//cl.rules = (Stack)rules.clone();
		return cl;
	}

	public String toString() {
		StringBuffer result = new StringBuffer("|");
		Enumeration keys = this.keys();
		if (!keys.hasMoreElements()) {
			result.append(" SUCCESS");
		} else {
			while (keys.hasMoreElements()) {
				result.append(" ");
				RBSubstitutable key = (RBSubstitutable) keys.nextElement();
				result.append(key + "=" + get(key));
			}
		}
		result.append(" |");
		return result.toString();
	}

	/** This is a call Frame. bodyresult comes from further evaluations in the
	  body. copy new stuff from body into "this" */
	public Frame callResult(Frame body) {
		Frame result = new Frame();
		Enumeration keys = this.keys();
		Frame instAux = new Frame();
		while (keys.hasMoreElements()) {
			RBSubstitutable key = (RBSubstitutable) keys.nextElement();
			RBTerm value = get(key);
			result.put(key, value.substantiate(body, instAux));
		}
		return result;
	}

	/** Append this and other and return the result (functional) */
	public Frame append(Frame other) {
		Frame f = (Frame) this.clone();
		Enumeration others = other.keys();
		while (others.hasMoreElements()) {
			Object key = others.nextElement();
			Object val = other.get(key);
			f.put(key, val);
		}

		// others = other.rules.elements();
		//while(others.hasMoreElements()) {
		//  RBRule rule = (RBRule)others.nextElement();
		//  f.push(rule);
		// }

		return f;
	}

	public boolean equals(Object x) {
		if (!(x.getClass() == this.getClass()))
			return false;
		boolean equal = true;
		Frame other = (Frame) x;
		Frame l = new Frame();
		Frame r = new Frame();
		Enumeration keys = this.keys();
		while (equal && keys.hasMoreElements()) {
			RBSubstitutable key = (RBSubstitutable) keys.nextElement();
			RBTerm value = get(key);
			equal = value.sameForm(other.get(key), l, r);
		}
		return equal;
	}

	public int hashCode() {
		int hash = 0;
		Enumeration keys = this.keys();
		while (keys.hasMoreElements()) {
			RBSubstitutable key = (RBSubstitutable) keys.nextElement();
			RBTerm value = get(key);
			hash += key.hashCode() * ((RBTerm) value).formHashCode();
		}
		return hash; 
	}

	public Frame removeVars(RBSubstitutable[] vars) {
		Frame result = (Frame) this.clone();
		for (int i = 0; i < vars.length; i++) {
			result.remove(vars[i]);
		}
		return result;
	}

}
