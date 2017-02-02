package tyRuBa.engine.compilation;

import tyRuBa.engine.Frame;
import tyRuBa.engine.RBContext;

public class CompiledConjunction_SemiDet_SemiDet extends SemiDetCompiled {

	private final SemiDetCompiled left;
	private final SemiDetCompiled right;

	public CompiledConjunction_SemiDet_SemiDet(SemiDetCompiled left, SemiDetCompiled right) {
		super(left.getMode().multiply(right.getMode()));
		this.left = left;
		this.right = right;
	}

	public Frame runSemiDet(Object input, RBContext context) {
		Frame leftResult = left.runSemiDet(input, context);
		if (leftResult == null)
			return null;
		else
			return right.runSemiDet(leftResult, context);
	}

	public String toString() {
		return "(" + right + " ==> " + left + ")";
	}

}
