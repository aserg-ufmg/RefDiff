package tyRuBa.engine;

import java.util.Collection;

import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.engine.visitor.ExpressionVisitor;
import tyRuBa.modes.ErrorMode;
import tyRuBa.modes.Factory;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.PredInfoProvider;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeModeError;

public class RBNotFilter extends RBExpression {

	private RBExpression negated_q;

	public RBNotFilter(RBExpression not_q) {
		negated_q = not_q;
	}

	public RBExpression getNegatedQuery() {
		return negated_q;
	}

	public String toString() {
		return "NOT(" + getNegatedQuery() + ")";
	}

	public Compiled compile(CompilationContext c) {
		return getNegatedQuery().compile(c).negate();
	}


	public TypeEnv typecheck(PredInfoProvider predinfo, TypeEnv startEnv) throws TypeModeError {
		try {
			getNegatedQuery().typecheck(predinfo, startEnv);
			return startEnv;
		} catch (TypeModeError e) {
			throw new TypeModeError(e, this);
		}
	}

	public RBExpression convertToMode(ModeCheckContext context, boolean rearrange)
	throws TypeModeError {
		Collection vars = negated_q.getFreeVariables(context);
		
		if (vars.isEmpty()) {
			RBExpression converted = negated_q.convertToMode(context, rearrange); 
			return Factory.makeModedExpression(
				new RBNotFilter(converted),
				converted.getMode().negate(),
				context);
		} else {
			return Factory.makeModedExpression(
				this,
				new ErrorMode("Variables improperly left unbound in NOT: " + vars),
				context);
		}
	}

	public RBExpression convertToNormalForm(boolean negate) {
		if (negate) {
			return getNegatedQuery().convertToNormalForm(false);
		} else {
			return getNegatedQuery().convertToNormalForm(true);
		}
	}

	public Object accept(ExpressionVisitor v) {
		return v.visit(this);
	}

}
