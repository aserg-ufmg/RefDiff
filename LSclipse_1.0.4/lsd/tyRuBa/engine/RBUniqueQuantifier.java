package tyRuBa.engine;

import java.util.Collection;
import java.util.HashSet;

import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.engine.compilation.CompiledUnique;
import tyRuBa.engine.visitor.ExpressionVisitor;
import tyRuBa.modes.ErrorMode;
import tyRuBa.modes.Factory;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.PredInfoProvider;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeModeError;

/**
 * An RBUniqueQuantifier succeeds if each quantified variable is bound to
 * exactly one value after evaluating the UNIQUE expression. If the
 * quantified variables are already bound before evaluation of the UNIQUE
 * expression, then the bindings are ignored while evaluating the UNIQUE
 * expression, then the bindings from the evaluation are compared to the
 * original bindings.
 */
public class RBUniqueQuantifier extends RBExpression {

	private RBExpression exp;
	private RBVariable[] vars;

	public RBUniqueQuantifier(Collection variables, RBExpression exp) {
		this.exp = exp;
		vars =(RBVariable[])variables.toArray(new RBVariable[variables.size()]);
	}

	public RBUniqueQuantifier(RBVariable[] vars, RBExpression exp) {
		this.exp = exp;
		this.vars = vars;
	}

	public RBExpression getExp() {
		return exp;
	}

	public int getNumVars() {
		return vars.length;
	}

	public RBVariable getVarAt(int pos) {
		return vars[pos];
	}
	
	public RBVariable[] getQuantifiedVars() {
		return vars;
	}

	public String toString() {
		StringBuffer result = new StringBuffer("(UNIQUE ");
		for (int i = 0; i < vars.length; i++) {
			if (i > 0)
				result.append(",");
			result.append(vars[i].toString());
		}
		result.append(" : " + getExp() + ")");
		return result.toString();
	}

	public TypeEnv typecheck(PredInfoProvider predinfo, TypeEnv startEnv) throws TypeModeError {
		try {
			return getExp().typecheck(predinfo, startEnv);
		} catch (TypeModeError e) {
			throw new TypeModeError(e, this);
		}
	}

	public RBExpression convertToMode(ModeCheckContext context, boolean rearrange) throws TypeModeError {
		ModeCheckContext resultContext = (ModeCheckContext)context.clone();
		Collection boundedVars = exp.getVariables();
		Collection vars = new HashSet();
		
		for (int i = 0; i < getNumVars(); i++) {
			RBVariable currVar = getVarAt(i);
			if (!boundedVars.contains(currVar)) {
				return Factory.makeModedExpression(
					this,
					new ErrorMode("UNIQUE variable " + currVar +
						" must become bound in " + exp),
					context);
			} else {
				vars.add(currVar);
			}
		}
		
		Collection freeVars = getFreeVariables(resultContext);
		
		if (freeVars.isEmpty()) {
			RBExpression converted = exp.convertToMode(context, rearrange);
			return Factory.makeModedExpression(
				new RBUniqueQuantifier(vars, converted), 
					converted.getMode().unique(), converted.getNewContext());
		} else {
			return Factory.makeModedExpression(
				this,
				new ErrorMode("Variables improperly left unbound in UNIQUE: " 
					+ freeVars), 
				resultContext);
		}
	}
	
	public RBExpression convertToNormalForm(boolean negate) {
		RBExpression result = new RBUniqueQuantifier(
			vars, exp.convertToNormalForm(false));
		if (negate) {
			return new RBNotFilter(result);
		} else {
			return result;
		}
	}

	public Object accept(ExpressionVisitor v) {
		return v.visit(this);
	}

	public Compiled compile(CompilationContext c) {
		return new CompiledUnique(vars, exp.compile(c));
	}

}
