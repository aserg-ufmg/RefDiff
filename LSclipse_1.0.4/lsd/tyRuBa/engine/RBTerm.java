package tyRuBa.engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import tyRuBa.engine.visitor.CollectVarsVisitor;
import tyRuBa.engine.visitor.InstantiateVisitor;
import tyRuBa.engine.visitor.SubstantiateVisitor;
import tyRuBa.engine.visitor.SubstituteVisitor;
import tyRuBa.engine.visitor.TermVisitor;
import tyRuBa.modes.BindingMode;
import tyRuBa.modes.ConstructorType;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeConstructor;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeModeError;
import tyRuBa.util.TwoLevelKey;

public abstract class RBTerm implements Cloneable, Serializable, TwoLevelKey {

	/** Turn a tyruba object into a Java Object.  The default implementation
	wraps the term in an UppedTerm object. */
	public Object up() {
		return new UppedTerm(this);
	}

	public abstract Frame unify(RBTerm other, Frame f);

	abstract boolean freefor(RBVariable v);

	/** return true if this term does not contain any variables */
	abstract boolean isGround();
	
	/** return true if this term does not contain any unbound variables */
	public abstract BindingMode getBindingMode(ModeCheckContext context);

	public abstract boolean equals(Object x);
	
	public abstract int hashCode();

	/** Determine wether this and other have the "same form". 
		The frames are used to remember which variables are associated to 
		eachother. Binding from left -> righ go in one frame bindings
		from right to left in the other */
	protected abstract boolean sameForm(RBTerm other, Frame leftToRight,
		Frame rightToLeft);
		
	/** Determine whether this and other have the "same form". */
	public final boolean sameForm(RBTerm other) {
		return sameForm(other, new Frame(), new Frame());
	}

	/** formHashCode: hashCode which ignores variable names (to be used 
	 *  together with sameform) */
	public abstract int formHashCode();

	public String quotedToString() {
		return toString();
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error("This should not happen");
		}
	}

	/** Gets the integer value *if* this term *is* an RBJavaObject 
	 *  containing an Integer */
	public int intValue() {
		throw new Error("This is not an integer");
	}

	/**
	 * Returns a Set of variables bound by this query expression.
	 */
	public final Collection getVariables() {
		CollectVarsVisitor visitor = new CollectVarsVisitor();
		this.accept(visitor);
		Collection vars = visitor.getVars();
		vars.remove(RBIgnoredVariable.the);
		return vars;
	}
	
	/** Return the type of the term */
	protected abstract Type getType(TypeEnv env) throws TypeModeError;

	public String functorTypeConstructor() throws TypeModeError {
		throw new TypeModeError(this.toString() + " cannot be used as a functor");
	}
		
	public final RBVariable[] varMap() {
		ArrayList varlist = new ArrayList();
		CollectVarsVisitor visitor = new CollectVarsVisitor(varlist);
		this.accept(visitor);
		return (RBVariable[])varlist.toArray(new RBVariable[varlist.size()]);
	}
	
	/**
	 * Register in the ModeCheckContext that all variables in this term have 
	 * become bound.
	 */
	public abstract void makeAllBound(ModeCheckContext context);

	public RBTerm addTypeCast(TypeConstructor typeCast) throws TypeModeError {
		ConstructorType constructorType = typeCast.getConstructorType();
		if (constructorType==null) 
			throw new TypeModeError("Illegal cast: "+typeCast+" is not a concrete type");
		return constructorType.apply(this);
	}
	
	public abstract Object accept(TermVisitor v);
	
	public RBTerm substitute(Frame frame) {
		SubstituteVisitor visitor = new SubstituteVisitor(frame);
		return (RBTerm) accept(visitor);
	}
	
	public RBTerm instantiate(Frame frame) {
		InstantiateVisitor visitor = new InstantiateVisitor(frame);
		return (RBTerm) accept(visitor);
	}
	
	public RBTerm substantiate(Frame subst, Frame inst) {
		SubstantiateVisitor visitor = new SubstantiateVisitor(subst, inst);
		return (RBTerm) accept(visitor);
	}
	
	public abstract String toString();

    public boolean isOfType(TypeConstructor t) {
        return false;
    }
	
}
