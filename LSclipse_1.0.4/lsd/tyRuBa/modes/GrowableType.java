package tyRuBa.modes;

import java.util.Map;

/**
 * A GrowableType is a type that is allowed to "grow". I.e. like a typevariable
 * who's range is determined by a BoundaryType from which it can change to one
 * of its supertypes.
 */
public class GrowableType extends Type {
	
	private BoundaryType lowerBound;
	private BoundaryType upperBound;

	public GrowableType(BoundaryType lowerBound) {
		this.lowerBound = lowerBound;
		this.upperBound = lowerBound;
	}
	
	private GrowableType(BoundaryType lowerBound, BoundaryType upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	public int hashCode() {
		return lowerBound.hashCode() + 13 * (upperBound.hashCode());
	}
	
	public boolean equals(Object other) {
		if (! (other instanceof GrowableType)) {
			return false;
		} else {
			GrowableType sother = (GrowableType) other;
			return lowerBound.equals(sother.lowerBound)
				&& upperBound.equals(sother.upperBound);
		}
	}
	
	public String toString() {
		return upperBound.toString();
	}

	public void checkEqualTypes(Type other, boolean grow) throws TypeModeError {
		if (other instanceof TVar) {
			other.checkEqualTypes(this, grow);
		} else if (this.equals(other)) {
			return;
		} else if (other instanceof GrowableType) {
			GrowableType sother = (GrowableType) other;
			lowerBound = (BoundaryType) lowerBound.union(sother.lowerBound);
			upperBound = lowerBound;
			sother.lowerBound = lowerBound;
			sother.upperBound = lowerBound;
		} else {
			check(other instanceof BoundaryType, this, other);
			BoundaryType b_other = (BoundaryType) other;
			BoundaryType new_lowerBound = (BoundaryType) this.lowerBound.union(b_other);
			if (grow) {
				lowerBound = new_lowerBound;
				upperBound = lowerBound;
			}
		}
	}

	public boolean isSubTypeOf(Type other, Map renamings) {
		return lowerBound.isSubTypeOf(other, renamings);
	}

	public Type intersect(Type other) throws TypeModeError {
		if (other instanceof GrowableType) {
			GrowableType sother = (GrowableType) other;
			BoundaryType max =
				(BoundaryType)upperBound.union(sother.upperBound);
			BoundaryType min =
				(BoundaryType)lowerBound.intersect(sother.lowerBound);
			if (max.equals(min)) {
				return min;
			} else {
				return new GrowableType(min, max);
			}
		} else if (other instanceof BoundaryType) {
		    BoundaryType cother = (BoundaryType) other;
		    BoundaryType result = (BoundaryType)lowerBound.intersect(other);
			if (cother.isStrict()) {
				check(cother.isSuperTypeOf(upperBound), this, other);
				return result;
			} else {
				if (! result.isSuperTypeOf(upperBound)) {
					return new GrowableType(result, upperBound);
				} else {
					return result;
				}
			}
		} else {
			return lowerBound.intersect(other);
		}
	}
	
//	Type lowerBound(Type other) throws TypeModeError {
//		return lowerBound.lowerBound(other);
//	}

	public boolean isFreeFor(TVar var) {
		return upperBound.isFreeFor(var);
	}

	public Type clone(Map tfact) {
		return new GrowableType((BoundaryType)lowerBound.clone(tfact),
			(BoundaryType)upperBound.clone(tfact));
	}

	public Type union(Type other) throws TypeModeError {
		if (other instanceof TVar) 
			return other.union(this);
		else if (other instanceof BoundaryType) {
			BoundaryType b_other = (BoundaryType)other;
			return new GrowableType((BoundaryType)lowerBound.union(b_other),
					                 (BoundaryType)upperBound.union(b_other));
		}
		else {
			check(other instanceof GrowableType,this,other);
			BoundaryType otherLower = ((GrowableType)other).lowerBound;
			BoundaryType otherUpper = ((GrowableType)other).upperBound;
			return new GrowableType(
					(BoundaryType)lowerBound.union(otherLower),
					(BoundaryType)upperBound.union(otherUpper));
		}
	}

	public Type copyStrictPart() {
		throw new Error("This should not be called!");
	}

	public boolean hasOverlapWith(Type other) {
		return lowerBound.hasOverlapWith(other);
	}

	public Type getParamType(String currName, Type repAs) {
		if (repAs instanceof TVar) {
			if (currName.equals(((TVar)repAs).getName())) {
				return this;
			} else {
				return null;
			}
		} else {
			return lowerBound.getParamType(currName, repAs);
		}
	}

}
