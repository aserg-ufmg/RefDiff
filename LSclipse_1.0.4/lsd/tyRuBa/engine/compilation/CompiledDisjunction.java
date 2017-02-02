package tyRuBa.engine.compilation;

import tyRuBa.engine.RBContext;
import tyRuBa.util.ElementSource;

public class CompiledDisjunction extends Compiled {

	private Compiled right;
	private Compiled left;

	public CompiledDisjunction(Compiled left, Compiled right) {
		super(left.getMode().add(right.getMode()));
		this.left = left;
		this.right = right;
	}

	public ElementSource runNonDet(Object input, RBContext context) {
		return left.runNonDet(input, context).append(right.runNonDet(input, context));
	}
	
	public Compiled negate() {
		return left.negate().conjoin(right.negate());
	}

	public String toString() {
		return "(" + right + " + " + left + ")";
	}

}
