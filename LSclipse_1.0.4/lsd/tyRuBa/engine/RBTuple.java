package tyRuBa.engine;

import java.util.ArrayList;

import tyRuBa.engine.visitor.TermVisitor;
import tyRuBa.modes.BindingMode;
import tyRuBa.modes.Factory;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TupleType;
import tyRuBa.modes.TypeModeError;
import tyRuBa.util.ObjectTuple;
import tyRuBa.util.TwoLevelKey;

public class RBTuple extends RBTerm implements TwoLevelKey {

	RBTerm[] subterms;

	public RBTuple(RBTerm t1) {
		subterms = new RBTerm[1];
		subterms[0] = t1;
	}

	public RBTuple(RBTerm t1, RBTerm t2) {
		subterms = new RBTerm[] { t1, t2 };
	}

	public RBTuple(RBTerm t1, RBTerm t2, RBTerm t3) {
		subterms = new RBTerm[] { t1, t2, t3 };
	}

	public RBTuple(RBTerm t1, RBTerm t2, RBTerm t3, RBTerm t4) {
		subterms = new RBTerm[] { t1, t2, t3, t4 };
	}

	public RBTuple(RBTerm t1, RBTerm t2, RBTerm t3, RBTerm t4, RBTerm t5) {
			subterms = new RBTerm[] { t1, t2, t3, t4 , t5 };
		}

	public static RBTuple theEmpty = new RBTuple(new RBTerm[0]);
	
	public static RBTuple make(ArrayList terms) {
	    return make((RBTerm[])terms.toArray(new RBTerm[terms.size()]));
	}
	
	public static RBTuple make(RBTerm[] terms) {
	    if (terms.length==0)
	        return theEmpty;
	    else
	        return new RBTuple(terms);
	}
	
	private RBTuple(RBTerm[] terms) {
		subterms = (RBTerm[]) terms.clone();
	}

    public static RBTuple makeSingleton(RBTerm term) {
        return new RBTuple(new RBTerm[] { term } );
    }

	public int getNumSubterms() {
		return subterms.length;
	}

	public RBTerm getSubterm(int i) {
		return subterms[i];
	}

	public boolean equals(Object x) {
		if (!(x.getClass() == this.getClass()))
			return false;
		RBTuple cx = (RBTuple) x;
		if (cx.subterms.length != this.subterms.length)
			return false;
		for (int i = 0; i < subterms.length; i++) {
			if (!(subterms[i].equals(cx.subterms[i])))
				return false;
		}
		return true;
	}

	public int formHashCode() {
		int hash = subterms.length;
		for (int i = 0; i < subterms.length; i++)
			hash = hash * 19 + subterms[i].formHashCode();
		return hash;
	}

	public int hashCode() {
		int hash = subterms.length;
		for (int i = 0; i < subterms.length; i++)
			hash = hash * 19 + subterms[i].hashCode();
		return hash;
	}

	boolean freefor(RBVariable v) {
		for (int i = 0; i < subterms.length; i++) {
			if (!subterms[i].freefor(v))
				return false;
		}
		return true;
	}

	public BindingMode getBindingMode(ModeCheckContext context) {
		for (int i = 0; i < getNumSubterms(); i++) {
			if (! (getSubterm(i).getBindingMode(context).isBound()))
				return Factory.makePartiallyBound();
		}
		return Factory.makeBound();
	}
	
	public boolean isGround() {
		for (int i = 0; i < getNumSubterms(); i++) {
			if (! getSubterm(i).isGround()) {
				return false;
			}
		}
		return true;
	}

	public boolean sameForm(RBTerm other, Frame lr, Frame rl) {
		if (other.getClass() != this.getClass()) 
			return false;
		RBTuple cother = (RBTuple) other;
		if (this.getNumSubterms() != cother.getNumSubterms())
			return false;
		for (int i = 0; i < subterms.length; i++) {
			if (!subterms[i].sameForm(cother.subterms[i], lr, rl))
				return false;
		}
		return true;
	}

	public Frame unify(RBTerm other, Frame f) {
		if (!(other instanceof RBTuple))
			if (other instanceof RBVariable)
				return other.unify(this, f);
			else
				return null;
		RBTuple cother = (RBTuple) other;
		int sz = this.getNumSubterms();
		if (sz != cother.getNumSubterms())
			return null;
		for (int i = 0; i < sz; i++) {
			f = this.subterms[i].unify(cother.subterms[i], f);
			if (f == null)
				return null;
		}
		return f;
	}

	public String toString() {
		StringBuffer result = new StringBuffer("<");
		for (int i = 0; i < subterms.length; i++) {
			if (i > 0)
				result.append(",");
			result.append(subterms[i].toString());
		}
		result.append(">");
		return result.toString();
	}

	protected Type getType(TypeEnv env) throws TypeModeError {
		TupleType tlst = Factory.makeTupleType();
		for (int i = 0; i < subterms.length; i++) { 
			tlst.add(subterms[i].getType(env));
		}
		return tlst;
	}

	public void makeAllBound(ModeCheckContext context) {
		for (int i = 0; i < getNumSubterms(); i++) {
			getSubterm(i).makeAllBound(context);
		}
	}

	public Object accept(TermVisitor v) {
		return v.visit(this);
	}

    public RBTuple append(RBTuple other) {
        RBTerm[] parts = new RBTerm[this.getNumSubterms()+other.getNumSubterms()];
        for (int i = 0; i < this.getNumSubterms(); i++) {
            parts[i]=this.getSubterm(i);
        }
        for (int i = 0; i < other.getNumSubterms(); i++) {
            parts[i+this.getNumSubterms()]=this.getSubterm(i);
        }
        return FrontEnd.makeTuple(parts);
    }

    /**
     * @see tyRuBa.util.TwoLevelKey#getFirst()
     */
    public String getFirst() {
        if (subterms.length > 0) {
            return subterms[0].getFirst();
        } else {
            return "";
        }
    }

    /**
     * @see tyRuBa.util.TwoLevelKey#getSecond()
     */
    public Object getSecond() {
        if (subterms.length > 1) {
            Object[] objs;
            Object second = subterms[0].getSecond();
            objs = new Object[subterms.length];
            objs[0] = second;
            for (int i = 1; i < subterms.length; i++) {
                if (subterms[i] instanceof RBRepAsJavaObjectCompoundTerm) {
                    objs[i] = ((RBRepAsJavaObjectCompoundTerm)subterms[i]).getValue();
                } else if (subterms[i] instanceof RBJavaObjectCompoundTerm) {
                    objs[i] = ((RBJavaObjectCompoundTerm)subterms[i]).getObject();                    
                } else {
                    objs[i] = subterms[i];
                }
            }
            return ObjectTuple.make(objs);
        } else if (subterms.length > 0) {
            return subterms[0].getSecond();
        } else {
            return ObjectTuple.theEmpty;
        }
    }

	public Object up() {
		Object[] objs = new Object[subterms.length];
		for (int i = 0; i < objs.length; i++) {
			objs[i] = subterms[i].up();
		}
		return objs;
	}
}
