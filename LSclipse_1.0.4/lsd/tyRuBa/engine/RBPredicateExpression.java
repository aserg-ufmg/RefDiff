package tyRuBa.engine;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.engine.compilation.CompiledPredicateExpression;
import tyRuBa.engine.compilation.SemiDetCompiledPredicateExpression;
import tyRuBa.engine.visitor.ExpressionVisitor;
import tyRuBa.modes.BindingList;
import tyRuBa.modes.ErrorMode;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Mode;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.Multiplicity;
import tyRuBa.modes.PredInfo;
import tyRuBa.modes.PredInfoProvider;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TupleType;
import tyRuBa.modes.TypeModeError;

/** 
 * This is an expression that represents a call to a predicate *before* it has
 * been mode checked. After it has been modechecked it will be replaced by an
 * appropriate RBModedPredicateExpression.
 */

public class RBPredicateExpression extends RBExpression {

	protected PredicateIdentifier pred;
	private RBTuple args;
    private RuleBase rules = null; 
      // This is set by modeChecking and points to the moded rulebase for this predicate's 
      // execution.

	/** Constructor */
	public RBPredicateExpression(String predName, ArrayList argTerms) {
		this(predName, RBTuple.make(argTerms));
	}
	
	public RBPredicateExpression withNewArgs(RBTuple newArgs) {
		return new RBPredicateExpression(pred,newArgs);
	}

	public RBPredicateExpression(String predName, RBTuple args) {
		this.pred = new PredicateIdentifier(predName, args.getNumSubterms());
		this.args = args;
	}

	public RBPredicateExpression(PredicateIdentifier pred, RBTuple args) {
		this.pred = pred;
		this.args = args;
	}

	/** Constructor */
	RBPredicateExpression(String predName, RBTerm[] argTerms) {
		this(predName, RBTuple.make(argTerms));
	}

    public Compiled compile(CompilationContext c) {
        Assert.assertNotNull("Must be mode checked first!",getMode());
        if (getMode().hi.compareTo(Multiplicity.one) <= 0) {
            return new SemiDetCompiledPredicateExpression(getMode(), rules, getArgs());
        }
        else {          
            return new CompiledPredicateExpression(getMode(), rules, getArgs());
        }
    }

	public PredicateIdentifier getPredId() {
		return pred;
	}

	public String getPredName() {
		return pred.name;
	}

	public RBTuple getArgs() {
		return args;
	}

	public int getNumArgs() {
		return args.getNumSubterms();
	}

	public RBTerm getArgAt(int pos) {
		return args.getSubterm(pos);
	}
	
	public Object clone() {
		try {
			RBPredicateExpression cl = (RBPredicateExpression) super.clone();
			cl.args = (RBTuple)args.clone();
			return cl;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			throw new Error("This should not happen");
		}
	}

	public String toString() {
		StringBuffer result = new StringBuffer(getPredName());
		int numSubterms = args.getNumSubterms();
		result.append("(");
		for (int i = 0; i < numSubterms; i++) {
			if (i>0) result.append(",");
			result.append(args.getSubterm(i));
		}
		result.append(")");
		
	    if (getMode()!=null) {
            result.append(" {");
            if (rules == null) {
                result.append("MODE ERROR");
            } else {
                result.append(rules.getPredMode());
            }
            result.append("}");
        }
        return result.toString();
    }

	public TypeEnv typecheck(PredInfoProvider predinfo, TypeEnv startEnv) 
	throws TypeModeError {
		try {
			TypeEnv myEnv = Factory.makeTypeEnv();
			PredicateIdentifier pred = getPredId();
			PredInfo pInfo = predinfo.getPredInfo(pred);
			if (pInfo == null) {
				throw new TypeModeError("Unknown predicate " + pred);
			} else {
				TupleType predType = pInfo.getTypeList();

				for (int i = 0; i < getNumArgs(); i++) {
					Type argType = getArgAt(i).getType(myEnv);
					argType.checkEqualTypes(predType.get(i));
				}
				return startEnv.intersect(myEnv);
			}
		} catch (TypeModeError e) {
			throw new TypeModeError(e, this);
		}
	}

	public RBExpression convertToMode(ModeCheckContext context, 
	boolean rearrange) throws TypeModeError {
		ModeCheckContext resultContext = (ModeCheckContext) context.clone();
		BindingList bindings = Factory.makeBindingList();
		RuleBase bestRuleBase =
			resultContext.getBestRuleBase(getPredId(), getArgs(), bindings);
		if (bestRuleBase == null) {
			return Factory.makeModedExpression(
				this, 
				new ErrorMode("there is no rulebase that allows " + getPredName()
					+ bindings), 
				resultContext);
		} else {
			Mode resultMode = bestRuleBase.getMode();
			Collection vars = getVariables();
			resultContext.removeAllBound(vars);
			if (vars.isEmpty()) {
				// This check will change the mode to semidet if the
				// predicate was called with unbound variables, but they are
				// IgnoredVariables only.
				resultMode = Mode.makeSemidet();
			} else {			
				resultContext.bindVars(vars);
				resultMode.setPercentFree(bindings);
			}
		 
			return Factory.makeModedExpression(this,resultMode, resultContext, bestRuleBase);
		}
	}

	public Object accept(ExpressionVisitor v) {
		return v.visit(this);
	}

    public RBExpression makeModed(Mode resultMode, ModeCheckContext resultContext, RuleBase bestRuleBase) {
        RBPredicateExpression modedExp = (RBPredicateExpression) this.makeModed(resultMode,resultContext);
        modedExp.setRuleBase(bestRuleBase);
        return modedExp;
    }

    private void setRuleBase(RuleBase bestRuleBase) {
        Assert.assertNull(rules);
        this.rules = bestRuleBase;
    }

}
