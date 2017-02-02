package tyRuBa.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.engine.compilation.CompiledRule;
import tyRuBa.modes.BindingList;
import tyRuBa.modes.ErrorMode;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Mode;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.PredInfo;
import tyRuBa.modes.PredInfoProvider;
import tyRuBa.modes.PredicateMode;
import tyRuBa.modes.TVar;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TupleType;
import tyRuBa.modes.TypeModeError;

public class RBRule extends RBComponent implements Cloneable {

	private PredicateIdentifier pred;
	private RBTuple args;
	private RBExpression cond;
	Mode mode = null; // is only set for rules that have been mode converted.
	
	public Mode getMode() {
		return mode;
	}

	public RBRule(RBPredicateExpression pred, RBExpression exp) {
		this.pred = pred.getPredId();
		this.args = pred.getArgs();
		this.cond = exp;
	}
	
	RBRule(PredicateIdentifier pred, RBTuple args, RBExpression cond) {
		this.pred = pred;
		this.args = args;
		this.cond = cond;
	}

	RBRule(PredicateIdentifier pred, RBTuple args, RBExpression cond, Mode resultMode) {
		this(pred,args,cond);
		mode = resultMode;
	}

	public PredicateIdentifier getPredId() {
		return pred;
	}
	
	public RBTuple getArgs() {
		return args;
	}

	public final RBExpression getCondition() {
		return cond;
	}

	public RBComponent addCondition(RBExpression e) {
		return new RBRule(pred, args, FrontEnd.makeAnd(e, cond));
	}

	public Object clone() {
		try {
			RBRule cl = (RBRule) super.clone();
			return cl;
		} catch (CloneNotSupportedException e) {
			throw new Error("This shouldn't happen!");
		}
	}

	public RBRule substitute(Frame frame) {
		RBRule r = (RBRule) clone();
		r.args = (RBTuple) args.substitute(frame);
		r.cond = cond.substitute(frame);
		return r;
	}

	public String conclusionToString() {
		return getPredName() + getArgs();
	}

	public String toString() {
		StringBuffer result = new StringBuffer(conclusionToString());
		if (cond != null) {
			result.append(" :- " + cond);
		}
		result.append(".");
		return result.toString();
	}

	public TupleType typecheck(PredInfoProvider predinfo) 
	throws TypeModeError {
		try {
			TypeEnv startEnv = new TypeEnv();
			PredicateIdentifier pred = getPredId();
			PredInfo pInfo = predinfo.getPredInfo(pred);
			if (pInfo == null)
				throw new TypeModeError("Unknown predicate " + pred);

			TupleType predTypes = pInfo.getTypeList();
			RBTuple args = getArgs();
			int numArgs = args.getNumSubterms();
			TupleType startArgTypes = Factory.makeTupleType();
			for (int i = 0; i < numArgs; i++) {
				Type currStrictPart = predTypes.get(i).copyStrictPart();
				Type argType = args.getSubterm(i).getType(startEnv);
				if (!(currStrictPart instanceof TVar)) {
					argType.checkEqualTypes(currStrictPart, false);
				}
				startArgTypes.add(argType);
			}

			TypeEnv inferredTypeEnv = getCondition().typecheck(predinfo, startEnv);

			TupleType argTypes = Factory.makeTupleType();
			Map varRenamings = new HashMap();			
			for (int i = 0; i < numArgs; i++) {
				argTypes.add(args.getSubterm(i).getType(inferredTypeEnv));
			}
			if (!argTypes.isSubTypeOf(predTypes, varRenamings))
				throw new TypeModeError("Inferred types " +
					argTypes + " incompatible with declared types " + predTypes);
			else
				return argTypes;
		
		} catch (TypeModeError e) {
			throw new TypeModeError(e, this);
		}
	}

	public RBComponent convertToNormalForm() {
		return new RBRule(pred, args, cond.convertToNormalForm());
	}

	public RBComponent convertToMode(PredicateMode predMode, ModeCheckContext context) 
	throws TypeModeError {
		BindingList paramModes = predMode.getParamModes();
		boolean toBeCheck = predMode.toBeCheck();
		int numArgs = args.getNumSubterms();
		
		for (int i = 0; i < numArgs; i++) {
			RBTerm currArg = args.getSubterm(i);
			if (paramModes.get(i).isBound()) {
				currArg.makeAllBound(context);
			}
		}

		RBExpression converted = cond.convertToMode(context);
		
		if (converted.getMode() instanceof ErrorMode) {
			throw new TypeModeError("cannot convert " + conclusionToString()
				+ ":-" + converted
				+ " to any legal mode\n" 
				+ "    " + converted.getMode());
		} else  {
			Mode resultMode = converted.getMode();
			Collection vars = args.getVariables();
			context.removeAllBound(vars);
			if (vars.isEmpty()) {
				resultMode = resultMode.first();
			} else if (toBeCheck) {
				if (!resultMode.compatibleWith(predMode.getMode())) {
					throw new TypeModeError("cannot convert " + conclusionToString()
						+ ":-" + converted
						+ " to the declared mode " + predMode.getMode() + ".\n"
						+ "inferred mode was " + converted.getMode());
				} else {
					vars = args.getVariables();
					converted.getNewContext().removeAllBound(vars);
					if (! vars.isEmpty()) {
						throw new TypeModeError("Variables " + vars 
							+ " do not become bound in " + this);
					}
				}
			}
			else {
				resultMode = resultMode.restrictedBy(predMode.getMode()); 
				// in case the rule has a REALLY IS declaration, it is possible that
				// the declared mode is more resticted than the infered mode. 
			}
			
			RBRule convertedRule = new RBRule(pred, args, converted, resultMode);

			if (!RuleBase.silent) {
				System.err.println(predMode + " ==> " + convertedRule);
			}
			return convertedRule;
		}
	}

	public Compiled compile(CompilationContext c) {
		Compiled compiledCond = cond.compile(c);
		if (compiledCond.getMode().hi.compareTo(mode.hi) > 0) {
			compiledCond = compiledCond.first();
		}
		return CompiledRule.make(this, args, compiledCond);
	}

}
