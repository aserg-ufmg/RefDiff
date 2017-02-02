package tyRuBa.engine.visitor;

import java.util.Collection;
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

/**
 * This visitor visits RBExpression and collects all variables that will
 * become bound after evaluation of the expression.
 */
public class CollectVarsVisitor extends AbstractCollectVarsVisitor {

	public CollectVarsVisitor(Collection vars) {
		super(vars, null);
	}
	
	public CollectVarsVisitor() {
		super(new HashSet(), null);
	}

	public Object visit(RBDisjunction disjunction) {
		Collection oldVars = getVars();
		Collection intersection = null;
		for (int i = 0; i < disjunction.getNumSubexps(); i++) {
			Collection next = disjunction.getSubexp(i).getVariables();
			if (intersection==null)
				intersection = next;
			else
				intersection.retainAll(next);
		}
		if (intersection != null) {
			oldVars.addAll(intersection);
		}
		return null;
	}

	public Object visit(RBExistsQuantifier exists) {
		return exists.getExp().accept(this);
	}

	public Object visit(RBFindAll findAll) {
		return findAll.getResult().accept(this);
	}

	public Object visit(RBCountAll count) {
		return count.getResult().accept(this);
	}

	public Object visit(RBNotFilter notFilter) {
		return null;
	}

	public Object visit(RBTestFilter testFilter) {
		return null;
	}

	public Object visit(RBUniqueQuantifier unique) {
		return unique.getExp().accept(this);
	}

	public Object visit(RBVariable var) {
		getVars().add(var);
		return null;
	}
	
	public Object visit(RBIgnoredVariable ignoredVar) {
		getVars().add(ignoredVar);
		return null;
	}

	public Object visit(RBTemplateVar templVar) {
		return null;
	}
    
}
