package tyRuBa.engine;

import tyRuBa.engine.visitor.TermVisitor;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeModeError;

public class RBQuoted extends RBAbstractPair {

	private static final RBTerm quotedName = FrontEnd.makeName("{}");

	public RBQuoted(RBTerm listOfParts) {
		super(quotedName, listOfParts);
	}

	public Object up() {
		return quotedToString();
	}
	
	public String toString() {
		return "{" + getQuotedParts().quotedToString() + "}";
	}

	public String quotedToString() {
		return getQuotedParts().quotedToString();
	}

	public RBTerm getQuotedParts() {
		return getCdr();
	}
	
	protected Type getType(TypeEnv env) throws TypeModeError {
		return Factory.makeSubAtomicType(Factory.makeTypeConstructor(String.class));
	}

	public Object accept(TermVisitor v) {
		return v.visit(this);
	}

    /**
     * @see tyRuBa.util.TwoLevelKey#getFirst()
     */
    public String getFirst() {
        return getCdr().getFirst();
    }

    /**
     * @see tyRuBa.util.TwoLevelKey#getSecond()
     */
    public Object getSecond() {
        return getCdr().getSecond();
    }
}
