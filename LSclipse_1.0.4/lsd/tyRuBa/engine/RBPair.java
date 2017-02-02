package tyRuBa.engine;

import tyRuBa.engine.visitor.TermVisitor;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeModeError;
import tyRuBa.util.ObjectTuple;

/**
 * @author kdvolder
 */
public class RBPair extends RBAbstractPair {

	/**
	 * Constructor for incomplete RBPair. For efficiency reasons only. Evil.
	 */
	public RBPair(RBTerm aCar) {
		super(aCar, null);
	}

	/**
	 * Constructor for RBPair.
	 */
	public RBPair(RBTerm aCar, RBTerm aCdr) {
		super(aCar, aCdr);
	}

	public static RBTerm make(RBTerm[] terms) {
		RBTerm t = FrontEnd.theEmptyList;
		for (int i = terms.length - 1; i >= 0; i--) {
			t = new RBPair(terms[i], t);
		}
		return t;
	}

	/** If proper list then Turn into an Object[] otherwise
	 *  just do as in super */
	public Object up() {
		try {
			int size = getNumSubterms();
			Object[] array = new Object[size];
			for (int i = 0; i < size; i++) {
				array[i] = getSubterm(i).up();
			}
			return array;
		} catch (ImproperListException e) {
			return super.up();
		}
	}

	public String toString() {
		return "[" + cdrToString(true, this) + "]";
	}

	public String quotedToString() {
		return getCar().quotedToString() + getCdr().quotedToString();
	}

	protected Type getType(TypeEnv env) throws TypeModeError {
		Type car,cdr,result;
		try {		
			car = getCar().getType(env);
		} catch (TypeModeError e) {
			throw new TypeModeError(e, getCar());
		}
		try {		
			cdr = getCdr().getType(env);
		} catch (TypeModeError e) {
			throw new TypeModeError(e, getCdr());
		}
		try {
			result = Factory.makeListType(car).union(cdr);
		} catch (TypeModeError e) {
			throw new TypeModeError(e, this);
		}				
		return result;
	}

	public Object accept(TermVisitor v) {
		return v.visit(this);
	}

    /**
     * @see tyRuBa.util.TwoLevelKey#getFirst()
     */
    public String getFirst() {
        return getCar().getFirst();
    }

    /**
     * @see tyRuBa.util.TwoLevelKey#getSecond()
     */
    public Object getSecond() {
        Object[] result = new Object[2];
        result[0] = getCar().getSecond();
        result[1] = getCdr();
        return ObjectTuple.make(result);
    }
}
