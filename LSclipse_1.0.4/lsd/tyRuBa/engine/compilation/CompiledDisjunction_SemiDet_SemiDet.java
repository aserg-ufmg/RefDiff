package tyRuBa.engine.compilation;

import tyRuBa.engine.Frame;
import tyRuBa.engine.RBContext;
import tyRuBa.util.ElementSource;

public class CompiledDisjunction_SemiDet_SemiDet extends Compiled {

	private final SemiDetCompiled left;
	private final SemiDetCompiled right;

	public CompiledDisjunction_SemiDet_SemiDet(SemiDetCompiled left,
	SemiDetCompiled right) {
		super(left.getMode().add(right.getMode()));
		this.left = left;
		this.right = right;
	}

	public ElementSource runNonDet(Object input, RBContext context) {
		Frame leftResult = left.runSemiDet(input, context);
		Frame rightResult = right.runSemiDet(input, context);
		if (leftResult == null && rightResult == null) {
			return ElementSource.theEmpty;
		} else {
			if (leftResult == null) {
//				PoormansProfiler.countSingletonsFromDisjunctionSemiDetSemiDet++;
				return ElementSource.singleton(rightResult);
			} else if (rightResult == null) {
//				PoormansProfiler.countSingletonsFromDisjunctionSemiDetSemiDet++;
				return ElementSource.singleton(leftResult);
			} else {
				return ElementSource.with(new Object[] {leftResult, rightResult});
			}
		}
	}

	public SemiDetCompiled first() {
		return new SemiDetCompiledDisjunction(left,right);
	}

	public String toString() {
		return "(" + right + " + " + left + ")";
	}

}
