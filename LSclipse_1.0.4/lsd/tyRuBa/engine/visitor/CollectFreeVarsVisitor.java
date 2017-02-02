package tyRuBa.engine.visitor;

import java.util.HashSet;

import tyRuBa.engine.RBCountAll;
import tyRuBa.engine.RBDisjunction;
import tyRuBa.engine.RBExistsQuantifier;
import tyRuBa.engine.RBFindAll;
import tyRuBa.engine.RBIgnoredVariable;
import tyRuBa.engine.RBNotFilter;
import tyRuBa.engine.RBTemplateVar;
import tyRuBa.engine.RBTestFilter;
import tyRuBa.engine.RBUniqueQuantifier;
import tyRuBa.engine.RBVariable;
import tyRuBa.modes.ModeCheckContext;

/**
 * This visitor visits RBExpression and collects *all* Free variables.
 * 
 * The main difference with CollectVars is that collect vars only returns
 * variables to which values will be bound by this expression. Thus
 * this visitor behaves different in some situations, for example
 * in the processing of Disjunction expressions.
 */
public class CollectFreeVarsVisitor extends AbstractCollectVarsVisitor {

	public CollectFreeVarsVisitor(ModeCheckContext context) {
		super(new HashSet(), context);
	}
	
	public Object visit(RBDisjunction disjunction) {
		for (int i = 0; i < disjunction.getNumSubexps(); i++) {
			disjunction.getSubexp(i).accept(this);
		}
		return null;
	}

	public Object visit(RBExistsQuantifier exists) {
		exists.getExp().accept(this);
		for (int i = 0; i < exists.getNumVars(); i++) {
			vars.remove(exists.getVarAt(i));
		}
		return null;
	}

	public Object visit(RBFindAll findAll) {
		findAll.getQuery().accept(this);
		findAll.getResult().accept(this);
		return null;
	}

	public Object visit(RBCountAll count) {
		count.getQuery().accept(this);
		count.getResult().accept(this);
		return null;
	}

	public Object visit(RBNotFilter notFilter) {
		return notFilter.getNegatedQuery().accept(this);
	}

	public Object visit(RBTestFilter testFilter) {
		return testFilter.getQuery().accept(this);
	}

	public Object visit(RBUniqueQuantifier unique) {
		unique.getExp().accept(this);
		for (int i = 0; i < unique.getNumVars(); i++) {
			vars.remove(unique.getVarAt(i));
		}
		return null;
	}

	public Object visit(RBVariable var) {
		if (! var.getBindingMode(context).isBound()) {
			vars.add(var);
		}
		return null;
	}
	
	public Object visit(RBIgnoredVariable ignoredVar) {
		return null;
	}

	public Object visit(RBTemplateVar ignoredVar) {
		return null;
	}

}
