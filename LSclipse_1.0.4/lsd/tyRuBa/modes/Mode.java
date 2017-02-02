/*
 * Created on Apr 17, 2003
 *
 */
package tyRuBa.modes;

/**
 * A Mode is an object representing an abstract approximation of
 * the number of results a query is expected to return.
 * 
 * It is represented by a range of Multiplicities.
 */
public class Mode implements Cloneable {

	// Note at all times, lo should be <= hi
	final public Multiplicity lo;
	final public Multiplicity hi;
	final private String printString;
	private int numFree = 0;
	private int numBound = 0;
	
	public Mode(Multiplicity lo, Multiplicity hi) {
		this(lo, hi, 0, 0);
	}

	public Mode(Multiplicity lo, Multiplicity hi, int numFree, int numBound) {
		this.lo = lo;
		this.hi = hi;
		this.numFree = numFree;
		this.numBound = numBound;
		if (lo.equals(Multiplicity.zero)) {
			if (hi.equals(Multiplicity.zero)) {
				printString = "FAIL";
			} else if (hi.equals(Multiplicity.one)) {
				printString = "SEMIDET";
			} else if (hi.equals(Multiplicity.many)) {
				printString = "NONDET";
			} else if (hi.equals(Multiplicity.infinite)) {
				printString = "INFINITE";
			} else {
				throw new Error("this should not happen");
			}
		} else if (lo.equals(Multiplicity.one)) {
			if (hi.equals(Multiplicity.one)) {
				printString = "DET";
			} else if (hi.equals(Multiplicity.many)) {
				printString = "MULTI";
			} else if (hi.equals(Multiplicity.infinite)) {
				printString = "INFINITE";
			} else {
				throw new Error("this should not happen");
			}
		} else {
			System.err.println("lo = " + lo + "\nhi = " + hi);
			throw new Error("this should not happen");
		}
	}
	
	public static Mode makeFail() {
		return new Mode(Multiplicity.zero, Multiplicity.zero, 0, 0);
	}
	
	public static Mode makeSemidet() {
		return new Mode(Multiplicity.zero, Multiplicity.one, 0, 0);
	}
	
	public static Mode makeDet() {
		return new Mode(Multiplicity.one, Multiplicity.one, 0, 0);
	}
	
	public static Mode makeNondet() {
		return new Mode(Multiplicity.zero, Multiplicity.many, 0, 0);
	}
	
	public static Mode makeMulti() {
		return new Mode(Multiplicity.one, Multiplicity.many, 0, 0);
	}

	public boolean isFail() {
		return lo.equals(Multiplicity.zero) && hi.equals(Multiplicity.zero);
	}
	
	public boolean isSemiDet() {
	    return lo.equals(Multiplicity.zero) && hi.equals(Multiplicity.one);
	}
	
	public boolean isDet() {
		return lo.equals(Multiplicity.one) && hi.equals(Multiplicity.one);
	}
	
	public boolean isNondet() {
		return lo.equals(Multiplicity.zero) && hi.equals(Multiplicity.many);
	}
	
	public boolean isMulti() {
		return lo.equals(Multiplicity.one) && hi.equals(Multiplicity.many);
	}
	
	public static Mode makeConvertTo() {
		return new Mode(Multiplicity.zero, Multiplicity.one, 1, 1);
	}

	public static Mode convertFromString(String modeString) {
		if (modeString.equals("DET")) {
			return makeDet();
		} else if (modeString.equals("SEMIDET")) {
			return makeSemidet();
		} else if (modeString.equals("NONDET")) {
			return makeNondet();
		} else if (modeString.equals("MULTI")) {
			return makeMulti();
		} else if (modeString.equals("FAIL")) {
			return makeFail();
		} else if (modeString.equals("ERROR")) {
			return new ErrorMode("");
		} else {
			throw new Error("unknown mode " + modeString);
		}
	}

	public Mode add(Mode other) {
		if (other == null) {
			return this;
		} else if (other instanceof ErrorMode) {
			return other.add(this);
		} else {
			return new Mode(this.lo.max(other.lo), this.hi.add(other.hi),
				this.numFree + other.numFree, this.numBound + other.numBound);
		}
	}

	public Mode multiply(Mode other) {
		if (other instanceof ErrorMode) {
			return other;
		} else {
			return new Mode(this.lo.multiply(other.lo), this.hi.multiply(other.hi),
				this.numFree + other.numFree, this.numBound + other.numBound);
		}
	}

	public String toString() {
		return printString;
	}
	
	public double getPercentFree() {
		if (numFree == 0) {
			return 0;
		} else {
			return ((double) numFree) / (numFree + numBound);
		}
	}

	public boolean equals(Object other) {
		if (other instanceof Mode) {
			Mode om = (Mode) other;
			return this.hi.equals(om.hi) && this.lo.equals(om.lo);
		} else {
			return false;
		}
	}
	
	public int hashCode() {
		return hi.hashCode() + 13*lo.hashCode();
	}

	/** return -1 if this expected to return less results than other
	 *  return 0 if this expected to return same nr of results than other
	 *  return 1 if this expected to return more results than other*/
	public int compareTo(Mode other) {
		int result = this.hi.compareTo(other.hi);
		if (result == 0) {
			result = this.lo.compareTo(other.lo);
			if (result == 0) {
				double thisPercentFree = getPercentFree();
				double otherPercentFree = other.getPercentFree();
				if (thisPercentFree < otherPercentFree) {
					return -1; // less free means expectation of less results returned!
				} else if (thisPercentFree > otherPercentFree) {
					return 1; // more free means expectation of more results returned!
				} else {
					return 0;
				}
			}
		}
		return result;
	}
	
	public boolean isBetterThan(Mode other) {
		return this.compareTo(other) < 0; // It is better to return less results.
	}

	public boolean compatibleWith(Mode declared) {
		return declared.hi.compareTo(this.hi) >= 0;
	}

	public Mode first() {
		if (this instanceof ErrorMode) {
			return this;
		} else {
			return new Mode(lo.min(Multiplicity.one), hi.min(Multiplicity.one),
				numFree, numBound);
		}
	}
	
	public Mode negate() {
		if (isFail()) {
			return new Mode(Multiplicity.one, Multiplicity.one, numFree, numBound);
		} else if (this instanceof ErrorMode) {
			return this;
		} else if (isDet() || isMulti()) {
			return new Mode(Multiplicity.zero, Multiplicity.zero, numFree, numBound);
		} else {
			return new Mode(Multiplicity.zero, Multiplicity.one, numFree, numBound);
		}
	}

	public Mode unique() {
		if (isDet() || isFail()) {
			return new Mode(lo, hi, numFree, numBound);
		} else if (this instanceof ErrorMode) {
			return this;
		} else {
			return new Mode(Multiplicity.zero, Multiplicity.one, numFree, numBound);
		}
	}

	public Mode restrictedBy(Mode upperBound) {
		if (this.hi.compareTo(upperBound.hi) > 0)
			return new Mode(this.lo, upperBound.hi, numFree, numBound);
		else
			return this;
	}

	public Mode findAll() {
		return new Mode(Multiplicity.one, Multiplicity.one, numFree, numBound);
	}
	
	/**
	 * What's the mode if in actuality more parameters are bound than in the 
	 * declaration?
	 */
	public Mode moreBound() {
		return new Mode(Multiplicity.zero, hi, numFree, numBound);
	}

	public void setPercentFree(BindingList bindings) {
		for (int i = 0; i < bindings.size(); i++) {
			if (bindings.get(i).isBound()) {
				numBound++;
			} else {
				numFree++;
			}
		}
	}
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error("This should not happen");
		}
	}

	public Mode noOverlapWith(Mode other) {
		if (other == null) {
			return this;
		} else {
			return new Mode(lo.min(other.lo), hi.max(other.hi));
		}
	}

}
