package tyRuBa.engine;

import java.util.NoSuchElementException;

import tyRuBa.engine.visitor.TermVisitor;
import tyRuBa.modes.ConstructorType;
import tyRuBa.modes.BindingMode;
import tyRuBa.modes.Factory;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.RepAsJavaConstructorType;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeConstructor;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeMapping;
import tyRuBa.modes.TypeModeError;
import tyRuBa.modes.UserDefinedTypeConstructor;
import tyRuBa.util.ObjectTuple;

/** 
 * All instance of RBCompoundTerm must be created through calling
 * ConstructorType.apply
 */
public abstract class RBCompoundTerm extends RBTerm {

    public static RBCompoundTerm make(ConstructorType constructorType, RBTerm term) {
        return new RBGenericCompoundTerm(constructorType,term);
    }
    
    public static RBTerm makeRepAsJava(RepAsJavaConstructorType type, Object obj) {
        return new RBRepAsJavaObjectCompoundTerm(type,obj);
    }
    
    public static RBTerm makeJava(Object o) {
        if (o instanceof Object[]) {
            // Turn arrays into tyruba lists
            Object[] array = (Object[]) o;
            RBTerm[] terms = new RBTerm[array.length];
            for (int i = 0; i < array.length; i++)
                terms[i] = makeJava(array[i]);
            return FrontEnd.makeList(terms);
        }
        if (o instanceof UppedTerm) {
            return ((UppedTerm)o).down();
        } else {
            return new RBJavaObjectCompoundTerm(o);
        }
    }

	public int getNumArgs() {
		return getConstructorType().getArity();
	}

	public RBTerm getArg(int i) {
		if (getArg() instanceof RBTuple) {
			return ((RBTuple)getArg()).getSubterm(i);
		} else if (i == 0) {
			return getArg();
		} else { 
			throw new NoSuchElementException();
		}
	}


    public abstract RBTerm getArg();

    public abstract ConstructorType getConstructorType();

	public boolean equals(Object x) {
		if (!(x instanceof RBCompoundTerm)) {
			return false;
		} else {
			RBCompoundTerm cx = (RBCompoundTerm) x;
			if (cx.getConstructorType().equals(this.getConstructorType())) {
				return cx.getArg().equals(this.getArg());
			} else {
				return false;
			}
		}
	}
	
	public int formHashCode() {
		return getConstructorType().hashCode() * 19 + getArg().formHashCode();
	}

	public int hashCode() {
		return getConstructorType().hashCode() * 19 + getArg().hashCode();
	}

	boolean freefor(RBVariable v) {
		return getArg().freefor(v);
	}

	public BindingMode getBindingMode(ModeCheckContext context) {
		BindingMode bm = getArg().getBindingMode(context);
		if (bm.isBound()) {
			return bm;
		} else {
			return Factory.makePartiallyBound();
		}
	}
	
	public boolean isGround() {
		return getArg().isGround();
	}

	protected boolean sameForm(RBTerm other, Frame lr, Frame rl) {
		if (!(other instanceof RBCompoundTerm)) {
			return false;
		} else {
			RBCompoundTerm cother = (RBCompoundTerm) other;
			if (this.getConstructorType().equals(cother.getConstructorType())) {
				return this.getArg().sameForm(cother.getArg(), lr, rl);
			} else {
				return false;
			}
		}
	}

	public Frame unify(RBTerm other, Frame f) {
		if (!(other instanceof RBCompoundTerm)) {
			if (other instanceof RBVariable) {
				return other.unify(this, f);
			} else {
				return null;
			}
		} else {
			RBCompoundTerm cother = (RBCompoundTerm) other;
			if (! cother.getConstructorType().equals(this.getConstructorType())) {
				return null;
			} else {
				return this.getArg().unify(cother.getArg(), f);
			}
		}
	}

	public String toString() {
		return getConstructorType().getFunctorId().toString() + getArg();
	}

	protected Type getType(TypeEnv env) throws TypeModeError {
		Type argType = getArg().getType(env);
		return getConstructorType().apply(argType);
	}

	public void makeAllBound(ModeCheckContext context) {
        getArg().makeAllBound(context);
	}
	
	public Object up() {
		TypeConstructor tc = getTypeConstructor();
		if (tc instanceof UserDefinedTypeConstructor) {
		    TypeMapping mapping = ((UserDefinedTypeConstructor)tc).getMapping();
		    if (mapping != null) {
		    		return mapping.toJava(getArg().up());         
		    	}
		}
		return super.up();
	}
	
	public final TypeConstructor getTypeConstructor() {
		return getConstructorType().getTypeConst();
	}

	public Object accept(TermVisitor v) {
		return v.visit(this);
	}

    /**
     * @see tyRuBa.util.TwoLevelKey#getFirst()
     */
    public String getFirst() {
        if (getNumArgs() > 0) {
            return getConstructorType().getFunctorId().getName() + getArg(0).getFirst();
        } else {
            return "";
        }
    }

    /**
     * @see tyRuBa.util.TwoLevelKey#getSecond()
     */
    public Object getSecond() {
        int numArgs = getNumArgs();
        if (numArgs > 1) {
            Object[] objs;
            Object second = getArg(0).getSecond();
            objs = new Object[numArgs];
            objs[0] = second;
            for (int i = 1; i < numArgs; i++) {
                RBTerm arg = getArg(i);
                if (arg instanceof RBRepAsJavaObjectCompoundTerm) {
                    objs[i] = ((RBRepAsJavaObjectCompoundTerm)arg).getValue();
                } else if (arg instanceof RBJavaObjectCompoundTerm) {
                    objs[i] = ((RBJavaObjectCompoundTerm)arg).getObject();                    
                } else {
                    objs[i] = arg;
                }
            }
            return ObjectTuple.make(objs);
        } else if (numArgs > 0) {
            return getArg(0).getSecond();
        } else {
            return ObjectTuple.theEmpty;
        }
    }

    public boolean isOfType(TypeConstructor t) {
        return t.isSuperTypeOf(getConstructorType().getTypeConst());
    }

}
