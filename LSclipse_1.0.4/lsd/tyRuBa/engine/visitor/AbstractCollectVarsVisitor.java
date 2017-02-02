package tyRuBa.engine.visitor;

import java.util.Collection;

import tyRuBa.engine.RBCompoundTerm;
import tyRuBa.engine.RBConjunction;
import tyRuBa.engine.RBModeSwitchExpression;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBPair;
import tyRuBa.engine.RBPredicateExpression;
import tyRuBa.engine.RBQuoted;
import tyRuBa.engine.RBTuple;
import tyRuBa.modes.ModeCheckContext;

public abstract class AbstractCollectVarsVisitor implements ExpressionVisitor, TermVisitor {

	Collection vars;
	protected ModeCheckContext context;
	
	public AbstractCollectVarsVisitor(Collection vars, ModeCheckContext context) {
		this.vars = vars;
		this.context = context;
	}

	public Collection getVars() {
		return vars;
	}

	public Object visit(RBConjunction conjunction) {
		for (int i = 0; i < conjunction.getNumSubexps(); i++) {
			conjunction.getSubexp(i).accept(this);
		}
		return null;
	}

	public Object visit(RBModeSwitchExpression modeSwitch) {
		for (int i = 0; i < modeSwitch.getNumModeCases(); i++) {
			modeSwitch.getModeCaseAt(i).getExp().accept(this);
		}
		if (modeSwitch.hasDefaultExp()) {
			modeSwitch.getDefaultExp().accept(this);
		}
		return null;
	}

	public Object visit(RBPredicateExpression predExp) {
		return predExp.getArgs().accept(this);
	}

	public Object visit(RBCompoundTerm compoundTerm) {
		compoundTerm.getArg().accept(this);
		return null;
	}

	public Object visit(RBTuple tuple) {
		for (int i = 0; i < tuple.getNumSubterms(); i++) {
			tuple.getSubterm(i).accept(this);
		}
		return null;
	}

	public Object visit(RBPair pair) {
		pair.getCar().accept(this);
		
		RBTerm cdr = (RBTerm)pair.getCdr();
		
		while(cdr instanceof RBPair) {
			pair = (RBPair)cdr;
			pair.getCar().accept(this);
			cdr = pair.getCdr();
		}
		
		cdr.accept(this);
		
		return null;
	}

	public Object visit(RBQuoted quoted) {
		return quoted.getQuotedParts().accept(this);
	}
	
}
