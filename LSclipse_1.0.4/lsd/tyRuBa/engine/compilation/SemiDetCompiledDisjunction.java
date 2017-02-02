package tyRuBa.engine.compilation;

import tyRuBa.engine.Frame;
import tyRuBa.engine.RBContext;

public class SemiDetCompiledDisjunction extends SemiDetCompiled {

	private SemiDetCompiled right;
	private SemiDetCompiled left;

	public SemiDetCompiledDisjunction(SemiDetCompiled left, SemiDetCompiled right) {
		this.left = left;
		this.right = right;
	}

	public Frame runSemiDet(Object input, RBContext context) {
		Frame result = left.runSemiDet(input, context);
		if (result == null)
			return right.runSemiDet(input, context);
		else
			return result;
	}
	
	public String toString() {
		return "SEMIDET(" + left + " + " + right + ")";
	}

}
