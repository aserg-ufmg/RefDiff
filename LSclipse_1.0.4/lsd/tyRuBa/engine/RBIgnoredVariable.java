package tyRuBa.engine;

import java.io.ObjectStreamException;

import tyRuBa.engine.visitor.TermVisitor;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeEnv;

/** A variable who's binding is totally ignored like prologs _ variable */
public class RBIgnoredVariable extends RBVariable {

	public static final RBIgnoredVariable the = new RBIgnoredVariable();

	private RBIgnoredVariable() {
		super("?");
	}

	public Frame unify(RBTerm other, Frame f) {
		return f;
	}

	boolean freefor(RBVariable v) {
		return true;
	}

	protected boolean sameForm(RBTerm other, Frame lr, Frame rl) {
		return this == other;
	}

	public int formHashCode() {
		return 1;
	}

	public boolean equals(Object obj) {
		return obj == this;
	}

	public int hashCode() {
		return 66727982;
	}

	public Object clone() {
		return this;
	}
	
	protected Type getType(TypeEnv env) {
		return Factory.makeTVar("");
	}
	
	public Object accept(TermVisitor v) {
		return v.visit(this);
	}

	public Object readResolve() throws ObjectStreamException {
		return the; // this is a singleton class, should not allow
		            // creation of copies, not even by deserialization
	}
	
}
