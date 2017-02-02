package tyRuBa.engine.compilation;

import tyRuBa.engine.Frame;
import tyRuBa.engine.RBContext;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBVariable;
import tyRuBa.util.ElementSource;

public class CompiledUnique extends SemiDetCompiled {

	RBVariable[] vars;
	Compiled exp;

	public CompiledUnique(RBVariable[] vars, Compiled exp) {
		super();
		this.vars = vars;
		this.exp = exp;
	}

	public Frame runSemiDet(Object input, RBContext context) {
		Frame f = (Frame) input;
		Frame newf = (Frame)f.clone();
		RBTerm[] vals = new RBTerm[vars.length];
		newf.removeVars(vars);
		for (int i = 0; i < vars.length; i++) {
			vals[i] = vars[i].substitute(f);
		}
		ElementSource result = exp.runNonDet(newf, context);
		if (!result.hasMoreElements())
			return null;
		else {
			while (result.hasMoreElements()) {
				Frame currentFrame = (Frame)result.nextElement();
				for (int i = 0; i < vals.length; i++) {
					newf = vals[i].unify(vars[i].substitute(currentFrame), newf);
					if (newf == null) {
						return null;
					}
				}
			}
			return newf;
		}
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer("UNIQUE(");
		for (int i = 0; i < vars.length; i++) {
			if (i > 0) {
				result.append(",");
			}
			result.append(vars[i]);
		}
		result.append(": " + exp + ")");
		return result.toString();
	}

}
