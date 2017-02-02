package tyRuBa.engine.compilation;

import tyRuBa.engine.Frame;
import tyRuBa.engine.RBContext;

public class CompiledNot extends SemiDetCompiled {

	//TODO: Find a way to turn the negated predicate into nondet mode and then take advantage of that.
	private Compiled negated;

	public CompiledNot(Compiled negated) {
		super(negated.getMode().negate());
		this.negated = negated;
	}

	public Frame runSemiDet(Object input, RBContext context) {
		if (negated.runNonDet(input, context).hasMoreElements())
			return null;
		else {
			return (Frame)input;
		}
	}
	
	public Compiled negate() {
		return new CompiledTest(negated);
	}

	public String toString() {
		return "NOT(" + negated + ")";
	}

}
