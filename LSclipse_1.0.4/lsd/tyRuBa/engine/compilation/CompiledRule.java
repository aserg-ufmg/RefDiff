package tyRuBa.engine.compilation;

import tyRuBa.engine.Frame;
import tyRuBa.engine.RBAvoidRecursion;
import tyRuBa.engine.RBContext;
import tyRuBa.engine.RBRule;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBTuple;
import tyRuBa.modes.Mode;
import tyRuBa.modes.Multiplicity;
import tyRuBa.util.Action;
import tyRuBa.util.ElementSource;

public class CompiledRule extends Compiled {

	RBTuple args;
	Compiled compiledCond;
	RBRule rule;

	public CompiledRule(RBRule rule, RBTuple args, Compiled compiledCond) {
		super(rule.getMode());
		this.args = args;
		this.compiledCond = compiledCond;
		this.rule = rule;
	}

	static private void debugInfo(RBRule r, RBTerm goal, RBTerm callGoal,
	Frame callFrame, Frame result, Frame callResult) {
		System.err.println("Rule invoked: " + r);
		System.err.println("For goal: " + goal);
		System.err.println("Call goal: " + callGoal);
		System.err.println("CallFrame: " + callFrame);
		System.err.println("Result: " + result);
		System.err.println("CallResult: " + callResult);
	}

	public ElementSource runNonDet(Object input, RBContext context) {
		final RBTerm goaL = (RBTerm) input;
//		System.err.println("         Goal: " + goaL);
//		System.err.println("Checking Rule: " + rule);
		final Frame callFrame = new Frame();
		// Rename all the variables in the goal to avoid name conflicts.
		final RBTuple goal = (RBTuple) goaL.instantiate(callFrame);
//		System.err.println("    Unifying : " + goal);
//		System.err.println("        with : " + args);
		final Frame fc = goal.unify(args, new Frame());
		if (fc == null) {
			return ElementSource.theEmpty;
		} else {
//			System.err.println("        resu : " + fc);
			final RBRule r = rule.substitute(fc);
			context = new RBAvoidRecursion(context, r);
			return compiledCond.runNonDet(fc, context).map(new Action() {
				public Object compute(Object resultFrame) {
					Frame result = callFrame.callResult((Frame) resultFrame);
//					 debugInfo(r, goaL, goal, callFrame, (Frame)resultFrame, result);
					return result;
				}
				public String toString() {
					return "callFrame" + callFrame;
				}
			});
		}
	}

	public static Compiled make(RBRule rule, RBTuple args, Compiled compiledCond) {
		Mode mode = rule.getMode();
		if (mode.hi.compareTo(Multiplicity.one) <= 0)
			return new SemiDetCompiledRule(rule, args, 
				(SemiDetCompiled)compiledCond);
		else
			return new CompiledRule(rule, args, compiledCond);
	}

	public String toString() {
		return "RULE(" + args + " :- " + compiledCond + ")";
	}

}
