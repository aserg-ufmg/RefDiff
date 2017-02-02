package tyRuBa.engine.compilation;

import tyRuBa.engine.Frame;
import tyRuBa.engine.RBContext;
import tyRuBa.util.ElementSource;

public class CompiledDisjunction_SemiDet_NonDet extends Compiled {

	SemiDetCompiled left;
	Compiled right;

	public CompiledDisjunction_SemiDet_NonDet(SemiDetCompiled left,
	Compiled right) {
		super(left.getMode().add(right.getMode()));
		this.left = left;
		this.right = right;
	}

	public ElementSource runNonDet(Object input, RBContext context) {
		final Frame leftResult = left.runSemiDet(input, context);
		ElementSource rightResult = right.runNonDet(input, context);
		if (leftResult == null) {
			return rightResult;
		} else {
//			PoormansProfiler.countSingletonsFromDisjunctionSemiDetNonDet++;
			return ElementSource.singleton(leftResult).append(rightResult);
		}
	}


	public SemiDetCompiled first() {
		return new SemiDetCompiledDisjunction(left,right.first());
	}

	public String toString() {
		return "(" + left + " + " + right + ")";
	}

}
