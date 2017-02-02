package tyRuBa.engine.compilation;

import tyRuBa.engine.Frame;
import tyRuBa.engine.RBContext;
import tyRuBa.util.ElementSource;

public class CompiledConjunction_SemiDet_NonDet extends Compiled {

	private final SemiDetCompiled left;
	private final Compiled right;

	public CompiledConjunction_SemiDet_NonDet(SemiDetCompiled left, Compiled right) {
		super(left.getMode().multiply(right.getMode()));
		this.left = left;
		this.right = right;
	}

	public ElementSource runNonDet(Object input, RBContext context) {
		Frame leftResult = left.runSemiDet(input, context);
		if (leftResult == null)
			return ElementSource.theEmpty;
		else
			return right.runNonDet(leftResult, context);
	}
	
	public String toString() {
		return "(" + right + "==>" + left + ")";
	}

}
