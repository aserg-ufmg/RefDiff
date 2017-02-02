package tyRuBa.engine;

import java.util.Collection;

import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.engine.compilation.CompiledFindAll;
import tyRuBa.engine.visitor.ExpressionVisitor;
import tyRuBa.modes.ErrorMode;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Mode;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.PredInfoProvider;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeModeError;

public class RBFindAll extends RBExpression {

	private RBExpression query;
	private RBTerm extract;
	private RBTerm result;

	public RBFindAll(RBExpression q, RBTerm e, RBTerm r) {
		query = q;
		extract = e;
		result = r;
	}

	public RBExpression getQuery() {
		return query;
	}
	
	public RBTerm getExtract() {
		return extract;
	}
	public RBTerm getResult() {
		return result; 
	}

	public String toString() {
		return "FINDALL(" + getQuery() + "," + getExtract() + "," 
			+ getResult() + ")";
	}

	public Compiled compile(CompilationContext c) {
		return new CompiledFindAll(getQuery().compile(c),getExtract(),getResult());
	}

	public TypeEnv typecheck(PredInfoProvider predinfo, TypeEnv startEnv) throws TypeModeError {
		try {
			TypeEnv afterQueryEnv = getQuery().typecheck(predinfo, startEnv);
			Type extractType = getExtract().getType(afterQueryEnv);
			Type inferredResultType = Factory.makeListType(extractType);
			TypeEnv resultTypeEnv = Factory.makeTypeEnv();
			Type resultType = getResult().getType(resultTypeEnv);
			resultType.checkEqualTypes(inferredResultType);
//			resultType.checkEqualTypes(getResult().getType(afterQueryEnv));
			return afterQueryEnv.intersect(resultTypeEnv);
		} catch (TypeModeError e) {
			throw new TypeModeError(e, this);
		}
	}

	public RBExpression convertToMode(ModeCheckContext context, boolean rearrange) throws TypeModeError {
		Collection freevars = query.getFreeVariables(context);
		Collection extractedVars = getExtract().getVariables();
		freevars.removeAll(extractedVars);
		
		if (!freevars.isEmpty()) {
			return Factory.makeModedExpression(
				this,
				new ErrorMode("Variables improperly left unbound in FINDALL: " 
					+ freevars),
				context);
		} else {
			RBExpression convQuery = query.convertToMode(context, rearrange);
			Mode convertedMode = convQuery.getMode();
			if (convertedMode instanceof ErrorMode) { 
				return Factory.makeModedExpression(this, convQuery.getMode(), 
					convQuery.getNewContext());
			} else {
				ModeCheckContext newContext = (ModeCheckContext)context.clone();
				result.makeAllBound(newContext);
				return Factory.makeModedExpression(
					new RBFindAll(convQuery, getExtract(), result),
					convertedMode.findAll(), newContext);
			}
		}
	}
	
	public RBExpression convertToNormalForm(boolean negate) {
		RBExpression result = 
			new RBFindAll(getQuery().convertToNormalForm(false), 
			getExtract(), getResult());
		if (negate) {
			return new RBNotFilter(result);
		} else {
			return result;
		}
	}

	public Object accept(ExpressionVisitor v) {
		return v.visit(this);
	}

}
