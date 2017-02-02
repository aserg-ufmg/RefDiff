package tyRuBa.engine.compilation;

import tyRuBa.engine.Frame;
import tyRuBa.engine.RBAvoidRecursion;
import tyRuBa.engine.RBContext;
import tyRuBa.engine.RBRule;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBTuple;

public class SemiDetCompiledRule extends SemiDetCompiled {

	RBTuple args;
	SemiDetCompiled compiledCond;
	RBRule rule;

	public SemiDetCompiledRule(RBRule rule, RBTuple args,
	SemiDetCompiled compiledCond) {
		super(rule.getMode());
		this.args = args;
		this.compiledCond = compiledCond;
		this.rule = rule;
	}

	public Frame runSemiDet(Object input, RBContext context) {
		RBTerm goaL = (RBTerm) input;
//		System.err.println("         Goal: " + goaL);
//		System.err.println("Checking Rule: " + rule);
		final Frame callFrame = new Frame();
		// Rename all the variables in the goal to avoid name conflicts.
		final RBTuple goal = (RBTuple) goaL.instantiate(callFrame);
//		System.err.println("    Unifying : " + goal);
//		System.err.println("        with : " + args);
		final Frame fc = goal.unify(args, new Frame());
		if (fc == null) {
			return null;
		} else {
			// System.err.println("        resu : " + fc);
			final RBRule r = rule.substitute(fc);
			context = new RBAvoidRecursion(context, r);
			Frame result = compiledCond.runSemiDet(fc, context);
			if (result == null) {
				return null;
			} else {
				return callFrame.callResult(result);
			}
		}
	}
	
	public String toString() {
		return "RULE(" + args + " :- " + compiledCond + ")";
	}

}
