package tyRuBa.engine.compilation;

import tyRuBa.engine.Frame;
import tyRuBa.engine.RBContext;
import tyRuBa.util.ElementSource;

public class CompiledFirst extends SemiDetCompiled {
	
	Compiled compiled;

	public CompiledFirst(Compiled compiled) {
		super(compiled.getMode().first());
		this.compiled = compiled;
	}

	public Frame runSemiDet(Object input, RBContext context) {
		ElementSource result = compiled.runNonDet(input, context);
		if (result.hasMoreElements()) {
			return (Frame)result.nextElement();
		}
		else
			return null;
	}

	public String toString() {
		return "FIRST(" + compiled + ")";
	}

}
