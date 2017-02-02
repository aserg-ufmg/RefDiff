package tyRuBa.engine;

import java.util.ArrayList;
import java.util.Vector;

import tyRuBa.util.ElementSource;

public abstract class RBCompoundExpression extends RBExpression {

	private ArrayList subexps;

	public RBCompoundExpression(Vector exps) {
		super();
		subexps = new ArrayList();
		for (int i = 0; i < exps.size(); i++) {
			addSubexp((RBExpression) exps.elementAt(i));
		}
	}

	public RBCompoundExpression(Object[] exps) {
		super();
		subexps = new ArrayList();
		for (int i = 0; i < exps.length; i++) {
			addSubexp((RBExpression) exps[i]);
		}
	}

	public RBCompoundExpression() {
		super();
		subexps = new ArrayList();
	}

	public RBCompoundExpression(RBExpression e1, RBExpression e2) {
		this();
		addSubexp(e1);
		addSubexp(e2);
	}

	public int getNumSubexps() {
		return subexps.size();
	}

	public RBExpression getSubexp(int i) {
		return (RBExpression) subexps.get(i);
	}

	public ElementSource getSubexps() {
		return ElementSource.with(subexps);
	}
	
	public ArrayList getSubexpsArrayList() {
		return (ArrayList)subexps.clone();
	}

	public void addSubexp(RBExpression e) {
		if (this.getClass().isInstance(e)) {
			RBCompoundExpression ce = (RBCompoundExpression) e;
			for (int i = 0; i < ce.getNumSubexps(); i++) {
				addSubexp(ce.getSubexp(i));
			}
		} else {
			subexps.add(e);
		}
	}

	/**Clone */
	public Object clone() {
		RBCompoundExpression cl;
		try {
			cl = (RBCompoundExpression) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error("This should not happen");
		}
		cl.subexps = (ArrayList) subexps.clone();
		return cl;
	}

	public String toString() {
		return toString(false);
	}

	public String toString(boolean brackets) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < getNumSubexps(); i++) {
			RBExpression currExp = getSubexp(i);
			if (i > 0) {
				result.append(separator() + " ");
			} 
			if (currExp instanceof RBCompoundExpression) {
				result.append(((RBCompoundExpression)currExp).toString(true));
			} else {
				result.append(getSubexp(i).toString());
			}
		}
		if (brackets) {
			return "(" + result.toString() + ")";
		} else {
			return result.toString();
		}
	}

	protected abstract String separator();
}