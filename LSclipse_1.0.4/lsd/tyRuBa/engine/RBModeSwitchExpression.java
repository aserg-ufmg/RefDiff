package tyRuBa.engine;

import java.util.Collection;
import java.util.Vector;

import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.engine.visitor.ExpressionVisitor;
import tyRuBa.modes.ErrorMode;
import tyRuBa.modes.Factory;
import tyRuBa.modes.ModeCase;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.PredInfoProvider;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeModeError;

/**
 * An RBModeSwitchExpression specifies different ways to evaluate an
 * expression when different variables are bound during execution. Once a
 * switch case has been determined, the order of evaluation will not be
 * rearranged. All RBModeSwitchExpressions should have been converted to
 * their switch case expressions before insertion.
 */
public class RBModeSwitchExpression extends RBExpression {
	
	Vector modeCases = new Vector();
	RBExpression defaultExp = null;
	
	public RBModeSwitchExpression(ModeCase mc) {
		modeCases.add(mc);
	}
		
	public void addModeCase(ModeCase mc) {
		modeCases.add(mc);
	}
	
	public void addDefaultCase(RBExpression exp) {
		defaultExp = exp;
	}
	
	public ModeCase getModeCaseAt(int pos) {
		return (ModeCase) modeCases.elementAt(pos);
	}
	
	public int getNumModeCases() {
		return modeCases.size();
	}
	
	public boolean hasDefaultExp() {
		return defaultExp != null;
	}

	public RBExpression getDefaultExp() {
		return defaultExp;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < getNumModeCases(); i++) {
			if (i > 0) {
				result.append("\n| ");
			}
			result.append(getModeCaseAt(i).toString());
		}
		if (defaultExp != null) {
			result.append("\n| DEFAULT: " + defaultExp);
		}
		return result.toString();
	}

	public Compiled compile(CompilationContext c) {
		throw new Error("Should not happen: a mode case should have been selected" +
			" before any compilation is performed");
	}

	public TypeEnv typecheck(PredInfoProvider predinfo, TypeEnv startEnv) throws TypeModeError {
		try {
			TypeEnv resultEnv = null;
			for (int i = 0; i < getNumModeCases(); i++) {
				RBExpression currExp = getModeCaseAt(i).getExp();
				TypeEnv currEnv = currExp.typecheck(predinfo, startEnv);

				if (resultEnv == null) {
					resultEnv = currEnv;
				} else {
					resultEnv = resultEnv.union(currEnv);
				}
			}
		
			if (hasDefaultExp()) {
				TypeEnv currEnv = getDefaultExp().typecheck(predinfo, startEnv);
				if (resultEnv == null) {
					resultEnv = currEnv;
				} else {
					resultEnv = resultEnv.union(currEnv);
				}
			}
			return resultEnv;
			
		} catch (TypeModeError e) {
			throw new TypeModeError(e, this);
		}
	}

	public RBExpression convertToMode(ModeCheckContext context, boolean rearrange)
	throws TypeModeError {
		for (int i = 0; i < getNumModeCases(); i++) {
			ModeCase currModeCase = getModeCaseAt(i);
			Collection boundVars = currModeCase.getBoundVars();
			if (context.checkIfAllBound(boundVars)) {
				RBExpression converted =
					currModeCase.getExp().convertToMode(context, false); 
				return Factory.makeModedExpression(converted, 
					converted.getMode(), converted.getNewContext());
			}
		}
		if (hasDefaultExp()) {
			RBExpression converted =
				getDefaultExp().convertToMode(context, rearrange); 
			return Factory.makeModedExpression(
				converted, converted.getMode(), converted.getNewContext());
		} else {
			return Factory.makeModedExpression(
				this,
				new ErrorMode("There is a missing mode case in " + this),
				context);
		}
	}

	public Object accept(ExpressionVisitor v) {
		return v.visit(this);
	}

}
