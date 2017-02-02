package tyRuBa.engine;

import tyRuBa.modes.BindingMode;
import tyRuBa.modes.Factory;
import tyRuBa.modes.ModeCheckContext;

public abstract class RBAbstractPair extends RBTerm {

	private RBTerm car;
	private RBTerm cdr;

	public RBAbstractPair(RBTerm aCar, RBTerm aCdr) {
		car = aCar;
		cdr = aCdr;
	}

	public RBTerm getCar() {
		return car;
	}
	
	public RBTerm getCdr() {
		return cdr;
	}
	
	/**
	 * Method for list processing efficiency reasons only. Evil.
	 */
	public void setCdr(RBTerm aCdr) {
		if(cdr == null) {
			if(aCdr != null)
				cdr = aCdr;
			else
				throw new IllegalArgumentException("Cannot set cdr to null");
		} else
			throw new IllegalStateException("Cannot set cdr more than once");
	}
	
	public RBTerm getCddr() {
		return ((RBAbstractPair) cdr).getCdr();
	}
	
	public int getNumSubterms() throws ImproperListException {
		RBTerm cdr = getCdr();
		if (cdr instanceof RBAbstractPair) {
			return ((RBAbstractPair)cdr).getNumSubterms() + 1;
		} else if (cdr.equals(FrontEnd.theEmptyList)) {
			return 1;
		} else {
			throw new ImproperListException();
		}
	}

	public RBTerm getSubterm(int i) {
		if (i == 0)
			return getCar();
		else {
			try {
				return ((RBAbstractPair) getCdr()).getSubterm(i - 1);
			} catch (ClassCastException e) {
				throw new java.util.NoSuchElementException();
			}
		}
	}

	public RBTerm[] getSubterms() throws ImproperListException {
		int sz = getNumSubterms();
		RBTerm[] result = new RBTerm[sz];
		RBTerm current = this;
		for (int i = 0; current instanceof RBAbstractPair; i++) { // loop breaks by exception
			result[i] = ((RBAbstractPair)current).car;
			current = ((RBAbstractPair)current).cdr;
		}
		return result;
	}

	protected final String cdrToString(boolean begin, RBTerm cdr) {
		String result = "";
		if (cdr.getClass() == this.getClass()) {
			RBAbstractPair pcdr = (RBAbstractPair) cdr;
			if (!begin)
				result += ",";
			result = result + pcdr.getCar() + cdrToString(false, pcdr.getCdr());
		} else if (!(cdr.equals(FrontEnd.theEmptyList))) {
			result = result + "|" + cdr;
		}
		return result;
	}

	public Frame unify(RBTerm other, Frame f) {
//		System.err.println("before: " + this + " & " + other + ": " + f);
		if (other.getClass() == getClass()) {
			RBAbstractPair cother = (RBAbstractPair) other;
			f = getCar().unify(cother.getCar(), f);
			if (f != null)
				f = getCdr().unify(cother.getCdr(), f);
//			System.err.println("after: " + this + " & " + other + ": " + f);
			return f;
		} else if (other instanceof RBVariable)
			return other.unify(this, f);
		else
			return null;
	}

	protected boolean sameForm(RBTerm other, Frame lr, Frame rl) {
		if (other.getClass() == this.getClass()) {
			RBAbstractPair cother = (RBAbstractPair) other;
			return getCar().sameForm(cother.getCar(), lr, rl)
				&& getCdr().sameForm(cother.getCdr(), lr, rl);
		} else
			return false;
	}

	boolean freefor(RBVariable v) {
		return car.freefor(v) && cdr.freefor(v);
	}

	public boolean equals(Object x) {
		if (x == null)
			return false;
		if (!(x.getClass() == this.getClass()))
			return false;
		RBAbstractPair cx = (RBAbstractPair) x;
		return getCar().equals(cx.getCar()) && getCdr().equals(cx.getCdr());
	}

	public int hashCode() {
		return car.hashCode() + 11 * cdr.hashCode();
	}

	public int formHashCode() {
		return car.formHashCode() + 11 * cdr.formHashCode();
	}

	public void makeAllBound(ModeCheckContext context) {
		getCar().makeAllBound(context);
		getCdr().makeAllBound(context);
	}

	public BindingMode getBindingMode(ModeCheckContext context) {
		BindingMode carMode = getCar().getBindingMode(context);
		BindingMode cdrMode = getCdr().getBindingMode(context);
		if (carMode.isBound() && cdrMode.isBound()) {
			return carMode;
		} else {
			return Factory.makePartiallyBound();
		}
	}

	public boolean isGround() {
		return getCar().isGround() && getCdr().isGround();
	}
	
}
