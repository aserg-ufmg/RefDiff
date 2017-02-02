package tyRuBa.engine.visitor;

import tyRuBa.engine.RBCompoundTerm;
import tyRuBa.engine.RBIgnoredVariable;
import tyRuBa.engine.RBPair;
import tyRuBa.engine.RBQuoted;
import tyRuBa.engine.RBTemplateVar;
import tyRuBa.engine.RBTuple;
import tyRuBa.engine.RBVariable;

public interface TermVisitor {

	Object visit(RBCompoundTerm compoundTerm);

	Object visit(RBIgnoredVariable ignoredVar);

	Object visit(RBPair pair);

	Object visit(RBQuoted quoted);
	
	Object visit(RBTuple tuple);

	Object visit(RBVariable var);

	Object visit(RBTemplateVar var);    
}
