package tyRuBa.engine.visitor;

import tyRuBa.engine.RBConjunction;
import tyRuBa.engine.RBCountAll;
import tyRuBa.engine.RBDisjunction;
import tyRuBa.engine.RBExistsQuantifier;
import tyRuBa.engine.RBFindAll;
import tyRuBa.engine.RBModeSwitchExpression;
import tyRuBa.engine.RBNotFilter;
import tyRuBa.engine.RBPredicateExpression;
import tyRuBa.engine.RBTestFilter;
import tyRuBa.engine.RBUniqueQuantifier;

public interface ExpressionVisitor {

	public Object visit(RBConjunction conjunction);

	public Object visit(RBDisjunction disjunction);

	public Object visit(RBExistsQuantifier exists);

	public Object visit(RBFindAll findAll);

	public Object visit(RBCountAll count);

	public Object visit(RBModeSwitchExpression modeSwitch);

	public Object visit(RBNotFilter notFilter);

	public Object visit(RBPredicateExpression predExp);

	public Object visit(RBTestFilter testFilter);

	public Object visit(RBUniqueQuantifier unique);

}
