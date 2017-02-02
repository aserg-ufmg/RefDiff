package tyRuBa.modes;

import java.io.Serializable;
import java.util.Map;
import java.util.Vector;

abstract public class Type implements Cloneable, Serializable {

	public static final Type integer =
		Factory.makeStrictAtomicType(Factory.makeTypeConstructor(Integer.class));
	public static final Type string =
		Factory.makeStrictAtomicType(Factory.makeTypeConstructor(String.class));
	public static final Type number =
		Factory.makeStrictAtomicType(Factory.makeTypeConstructor(Number.class));
	public static final Type object =
		Factory.makeStrictAtomicType(Factory.makeTypeConstructor(Object.class));	

	/** check if this type is equal to other, if not, throw TypeError */
	public void checkEqualTypes(Type other) throws TypeModeError {
		checkEqualTypes(other, true);
	}

	/** check if this type is equal to other, if not, throw TypeError
	 *  if grow is true, than SubAtomicType and SubCompositeType can grow
	 */
	public abstract void checkEqualTypes(Type other, boolean grow) throws TypeModeError;
	
	/** return true if this contains reference to var */
	public abstract boolean isFreeFor(TVar var);
	
	/** return a clone of this, making sure that each TVar is only cloned
	 *  once */
	public abstract Type clone(Map varRenamings);

	public abstract Type union(Type other) throws TypeModeError;
	
	public abstract Type intersect(Type other) throws TypeModeError;
	
	/** return the smallest super type of this and other */
//	abstract Type lowerBound(Type other) throws TypeModeError;

	public static void check(boolean b, Type t1, Type t2) throws TypeModeError {
		if (!b) {
			throw new TypeModeError("Incompatible types: " + t1 + ", " + t2);
		}
	}
	
	public abstract boolean isSubTypeOf(Type type, Map renamings);

	public abstract Type copyStrictPart();

	public abstract boolean hasOverlapWith(Type other);
	
	public boolean hasOverlapWith(Vector types, boolean hasOverlap) {
		int size = types.size();
		boolean equalTypes = false;
		int counter = 0;
		while (!equalTypes && counter < size) {
			Type currType = (Type)types.elementAt(counter);
			if (currType.equals(this)) {
				hasOverlap = hasOverlap && true;
				equalTypes = true;
			} else if (hasOverlap) {
				if (!currType.hasOverlapWith(this)) {
					hasOverlap = false;
				}
			}
			counter++;
		}
		if (!equalTypes) {
			types.add(this);
		}
		return hasOverlap;
	}

	abstract public Type getParamType(String currName, Type repAs);
	
	public Class javaEquivalent() throws TypeModeError {
	    throw new TypeModeError("This type "+this+" has no defined mapping to a Java equivalent");
	}

    public boolean isJavaType() {
        return false;
    }

}