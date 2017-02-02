/*
 * Created on Jul 3, 2003
 */
package tyRuBa.engine.visitor;

import java.util.Collection;
import java.util.HashSet;

import tyRuBa.engine.Frame;
import tyRuBa.engine.RBExpression;
import tyRuBa.engine.RBTemplateVar;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBUniqueQuantifier;
import tyRuBa.engine.RBVariable;

public class InstantiateVisitor extends SubstituteOrInstantiateVisitor {

	public InstantiateVisitor(Frame frame) {
		super(frame);
	}

	public Object visit(RBUniqueQuantifier unique) {
		RBExpression exp = (RBExpression) unique.getExp().accept(this);
		Collection vars = new HashSet();
		for (int i = 0; i < unique.getNumVars(); i++) {
			vars.add(unique.getVarAt(i).accept(this));
		}
		return new RBUniqueQuantifier(vars, exp);
	}

	public Object visit(RBVariable var) {
		RBTerm val = (RBTerm) getFrame().get(var);
		if (val == null) {
			val = (RBVariable) var.clone();
			getFrame().put(var, val);
			return val;
		} else {
			return val;
		}
	}

	public Object visit(RBTemplateVar templVar) {
		//Instantiation only happens at runtime. TemplateVar should not
		//exsit any more at runtime so...
		throw new Error("Unsupported operation");
	}

}
