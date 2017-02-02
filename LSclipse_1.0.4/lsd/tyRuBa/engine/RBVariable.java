package tyRuBa.engine;

import java.io.IOException;

import tyRuBa.engine.visitor.TermVisitor;
import tyRuBa.modes.BindingMode;
import tyRuBa.modes.Factory;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeEnv;

public class RBVariable extends RBSubstitutable {

	static protected int gensymctr = 1;

	public static RBVariable makeUnique(String id) {
		return new RBVariable(new String(id));
	}

	public static RBVariable make(String id) {
		return new RBVariable(id.intern());
	}

	/** Not intended to be called by clients. Must call make or makeUnique
	instead. */
	protected RBVariable(String id) {
		super(id);
	}

	/** Is called at the time when an attempt is made to bind a previously
	unbound variabes. Default implementation just binds the variable. But
	    constrained variable (e.g. a regexp varaible) may perform additional checks. */
	protected Frame bind(RBTerm val, Frame f) {
		f.put(this, val);
		return f;
	}

	public Frame unify(RBTerm other, Frame f) {
		//System.err.println("** entering unify " + this + " to: " + other);
		if (other instanceof RBIgnoredVariable)
			return f;
		RBTerm val = f.get(this);
		if (val == null) {
			other = other.substitute(f);
			if (equals(other))
				return f;
			else if (other.freefor(this)) {
				return bind(other, f);
			} else
				return null;
		} else
			return val.unify(other, f);
	}

	boolean freefor(RBVariable v) {
		return ! equals(v);
	}

	public BindingMode getBindingMode(ModeCheckContext context) {
		if (context.isBound(this)) {
			return Factory.makeBound();
		} else {
			return Factory.makeFree();
		}
	}
	
	public boolean isGround() {
		return false;
	}

	protected boolean sameForm(RBTerm other, Frame lr, Frame rl) {
		if (!(other.getClass() == this.getClass()))
			return false;
		else {
			RBVariable binding = (RBVariable) lr.get(this);
			if (binding == null) {
				lr.put(this, other);
			} else if (!binding.equals(other)) {
				return false;
			}
			binding = (RBVariable) rl.get(other);
			if (binding == null) {
				rl.put(other, this);
				return true;
			} else {
				return this.equals(binding);
			}
		}
	}

	public int formHashCode() {
		return 1;
	}

	public Object clone() {
		return makeUnique(name);
	}

	protected Type getType(TypeEnv env) {
		return env.get(this);
	}

	public void makeAllBound(ModeCheckContext context) {
		context.makeBound(this);
	}

	public Object accept(TermVisitor v) {
		return v.visit(this);
	}
	
	//TODO: This implementation of readObject only works for
	//variables created by make, but not by makeUnique (uniqueness will be lost)
	private void readObject(java.io.ObjectInputStream in)
	throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		name = name.intern();	 
	}

	
    public String getFirst() {
        throw new Error("Variables cannot be two level keys");
    }

    public Object getSecond() {
        throw new Error("Variables cannot be two level keys");
    }

//    public String getFirstAsString() {
//        throw new Error("Variables cannot be two level keys");
//    }
		 
}
