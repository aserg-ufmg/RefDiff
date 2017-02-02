package tyRuBa.engine;

import java.util.ArrayList;
import java.util.Vector;

import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.engine.visitor.ExpressionVisitor;
import tyRuBa.modes.ErrorMode;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Mode;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.PredInfoProvider;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeModeError;

public class RBConjunction extends RBCompoundExpression {

	public RBConjunction() {
		super();
	}

	public RBConjunction(Vector exps) {
		super(exps);
	}

	public RBConjunction(Object[] exps) {
		super(exps);
	}

	public RBConjunction(RBExpression e1, RBExpression e2) {
		super(e1, e2);
	}
	
	/** Evaluate this expression with a given frame in a given rulebase */
	public final Compiled compile(CompilationContext c) {
		Compiled res = Compiled.succeed;
		for (int i = 0; i < getNumSubexps(); i++) {
			res = res.conjoin(getSubexp(i).compile(c));
		}
		return res;
	}
	
	protected String separator() {
		return ",";
	}

	public TypeEnv typecheck(PredInfoProvider predinfo, TypeEnv startEnv) 
	throws TypeModeError {
		TypeEnv resultEnv = startEnv;
		for (int i = 0; i < getNumSubexps(); i++) {
			try {
				resultEnv = resultEnv.intersect(
					getSubexp(i).typecheck(predinfo, resultEnv));
			} catch (TypeModeError e) {
				throw new TypeModeError(e, this);
			}
		}
		return resultEnv;
	}

	public RBExpression convertToMode(ModeCheckContext context,
	boolean rearrangeAllowed) throws TypeModeError {
		RBConjunction result = new RBConjunction();
		Mode resultMode = Mode.makeDet();

		ArrayList toBeChecked = getSubexpsArrayList();
	
		while (!toBeChecked.isEmpty()) {
			RBExpression best;
			int bestPos = 0;
			if (rearrangeAllowed) {
				best = ((RBExpression)toBeChecked.get(0))
					.convertToMode(context, true);
				for (int i = 1; i < toBeChecked.size(); i++) {
					RBExpression exp =  (RBExpression) toBeChecked.get(i);
					RBExpression converted = exp.convertToMode(context, true);
					if (converted.isBetterThan(best)) {
						best = converted;
						bestPos = i;
					}
				}
				if (best.getMode() instanceof ErrorMode) {
					return Factory.makeModedExpression(
						this, 
						best.getMode(),
						context);
				}
			} else {
				best = ((RBExpression)toBeChecked.get(0))
					.convertToMode(context, false);
			}
			result.addSubexp(best);
			toBeChecked.remove(bestPos);
			resultMode = resultMode.multiply(best.getMode());
			context = best.getNewContext();
		}

		return Factory.makeModedExpression(result, resultMode, context);
	}

	public RBExpression convertToNormalForm(boolean negate) {
		if (negate) {
			RBDisjunction result = new RBDisjunction();
			for (int i = 0; i < getNumSubexps(); i++) {
				result.addSubexp(getSubexp(i).convertToNormalForm(true));
			}
			return result;
			
		} else {
			RBExpression result = null;
			for (int i = 0; i < getNumSubexps(); i++) {
				RBExpression converted = 
					getSubexp(i).convertToNormalForm(false);
				if (result == null) {
					result = converted;
				} else {
					result = result.crossMultiply(converted);
				}
			}
			return result;
		}
	}
	
	public RBExpression crossMultiply(RBExpression other) {
		if (other instanceof RBDisjunction)
			return other.crossMultiply(this);
		
		RBConjunction result = (RBConjunction)this.clone();
		if (other instanceof RBConjunction) {
			RBConjunction cother = (RBConjunction) other;
			for (int i = 0; i < cother.getNumSubexps(); i++)
				result.addSubexp(cother.getSubexp(i));
		} else
			result.addSubexp(other);
		return result;
	}

	public Object accept(ExpressionVisitor v) {
		return v.visit(this);
	}
}
