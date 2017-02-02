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

/**
 * An RBExistsQuantifier evalutes the same way as an RBPredicateExpression
 * except that the quantified variables are treated as brand new variables
 * inside the EXISTS expression. It is used mostly inside NOT expressions.
 */
public class RBExistsQuantifier extends RBExpression {

	private RBExpression exp;
	private RBVariable[] vars;

	public RBExistsQuantifier(Collection variables, RBExpression exp) {
		this.exp = exp;
		vars =(RBVariable[])variables.toArray(new RBVariable[variables.size()]);
	}

	RBExistsQuantifier(RBVariable[] vars, RBExpression exp) {
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

	public TypeEnv typecheck(PredInfoProvider predinfo, TypeEnv startEnv) throws TypeModeError {
		try {
			return getExp().typecheck(predinfo, startEnv);
		} catch (TypeModeError e) {
			throw new TypeModeError(e, this);
		}
	}

	public RBExpression convertToMode(ModeCheckContext context, boolean rearrange)
	throws TypeModeError {
		ModeCheckContext resultContext = (ModeCheckContext) context.clone();
		Collection boundedVars = getVariables();
		
		for (int i = 0; i < getNumVars(); i++) {
			RBVariable currVar = getVarAt(i);
			if (!boundedVars.contains(currVar)) {
				return Factory.makeModedExpression(
					this,
					new ErrorMode("Existentially quantified variable " + currVar +
						" must become bound in " + getExp()),
					context);
			}
		}
		
		RBExpression converted = getExp().convertToMode(context, rearrange); 
		return Factory.makeModedExpression(
			new RBExistsQuantifier(vars, converted), 
			converted.getMode(), resultContext);
	}

	public String toString() {
		StringBuffer result = new StringBuffer("(EXISTS ");
		for (int i = 0; i < vars.length; i++) {
			if (i > 0)
				result.append(",");
			result.append(vars[i].toString());
		}
		result.append(" : " + getExp() + ")");
		return result.toString();
	}

	public Compiled compile(CompilationContext c) {
		return getExp().compile(c);
	}

	public RBExpression convertToNormalForm(boolean negate) {
		Frame varRenaming = new Frame();
		RBVariable[] newVars = new RBVariable[vars.length];
		for (int i = 0; i < vars.length; i++) {
			newVars[i]=(RBVariable)vars[i].instantiate(varRenaming);
		}
		RBExpression convertedExp = exp.substitute(varRenaming).convertToNormalForm(false);
		return convertedExp.addExistsQuantifier(newVars, negate);
		
	}

	public Object accept(ExpressionVisitor v) {
		return v.visit(this);
	}
	
}
