package tyRuBa.engine.compilation;

import tyRuBa.engine.RBContext;
import tyRuBa.util.Action;
import tyRuBa.util.ElementSource;

public class CompiledConjunction_Nondet_Semidet extends Compiled {

	Compiled left;
	SemiDetCompiled right;

	public CompiledConjunction_Nondet_Semidet(Compiled left,
	SemiDetCompiled right) {
		super(left.getMode().multiply(right.getMode()));
		this.left = left;
		this.right = right;
	}

	public ElementSource runNonDet(Object input, final RBContext context) {
		return left.runNonDet(input, context).map(new Action() {
			public Object compute(Object arg) {
				return right.runSemiDet(arg, context); 
			}
		});
	}
	
	public String toString() {
		return "(" + right + " ==> " + left + ")";
	}

}
