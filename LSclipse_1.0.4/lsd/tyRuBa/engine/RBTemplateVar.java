/*
 * Created on Feb 18, 2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package tyRuBa.engine;

import tyRuBa.engine.visitor.TermVisitor;
import tyRuBa.modes.BindingMode;
import tyRuBa.modes.Bound;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeModeError;

/**
 * An RBTemplateVar is a vriable starting with a "!" in its name. These
 * variables are used as part of the JDBC-like interface in the construction
 * of "prepared" statements where they serve as placeholders for values that
 * need to be provide before the statement is executed.
 * 
 * Therefore, templateVars are a funny mix of behaviors of variable and constant.
 * 
 * At runtime they should not exist anymore and be replaced by constants. Yet
 * for some aspects of type checking they work similar to (always bound) variables.
 * They also support substitution (but should be used only before execution).
 */
public class RBTemplateVar extends RBSubstitutable {

	public RBTemplateVar(String name) {
		super(name.intern());
	}

	public Frame unify(RBTerm other, Frame f) {
		// This method is called only during execution.
		// No templateVars should remain by the time something is executing.
		throw new Error("Unsupported operation");
	}

	/* (non-Javadoc)
	 * @see tyRuBa.engine.RBTerm#freefor(tyRuBa.engine.RBVariable)
	 */
	boolean freefor(RBVariable v) {
		// This method is called only during execution.
		// No templateVars should remain by the time something is executing.
		throw new Error("Unsupported operation");
	}

	boolean isGround() {
		// Basically should be treated as constant in this case.
		return true;
	}

	public BindingMode getBindingMode(ModeCheckContext context) {
		// Basically should be treated as constant in this case: always bound.
		return Bound.the;
	}

	protected boolean sameForm(
		RBTerm other,
		Frame leftToRight,
		Frame rightToLeft) {
			// This method is called only during execution.
			// No templateVars should remain by the time something is executing.
			throw new Error("Unsupported operation");
	}

	public int formHashCode() {
		// This method is called only during execution.
		// No templateVars should remain by the time something is executing.
		throw new Error("Unsupported operation");
	}

	protected Type getType(TypeEnv env) throws TypeModeError {
		// Just like a variable.
		return env.get(this);
	}

	public void makeAllBound(ModeCheckContext context) {
		// Just like a constant. Nothing to do!
	}

	public Object accept(TermVisitor v) {
		return v.visit(this);
	}

    /**
     * @see tyRuBa.util.TwoLevelKey#getFirst()
     */
    public String getFirst() {
        throw new Error("Variables cannot be two level keys");
    }

    /**
     * @see tyRuBa.util.TwoLevelKey#getSecond()
     */
    public Object getSecond() {
        throw new Error("Variables cannot be two level keys");
    }
}
