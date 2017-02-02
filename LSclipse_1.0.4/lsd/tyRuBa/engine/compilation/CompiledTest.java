package tyRuBa.engine.compilation;

import tyRuBa.engine.Frame;
import tyRuBa.engine.RBContext;

public class CompiledTest extends SemiDetCompiled {

	private Compiled tested;

	public CompiledTest(Compiled tested) {
		super(tested.getMode().first());
		this.tested = tested;
	}

	public Frame runSemiDet(Object input, RBContext context) {
		if (tested.runNonDet(input, context).hasMoreElements())
			return (Frame)input;
		else 
			return null;
	}
	
	public Compiled negate() {
		return new CompiledNot(tested);
	}

	public String toString() {
		return "TEST(" + tested + ")";
	}

}
