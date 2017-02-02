package tyRuBa.engine.visitor;

import tyRuBa.engine.Frame;
import tyRuBa.engine.RBCompoundTerm;
import tyRuBa.engine.RBIgnoredVariable;
import tyRuBa.engine.RBPair;
import tyRuBa.engine.RBQuoted;
import tyRuBa.engine.RBTemplateVar;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBTuple;
import tyRuBa.engine.RBVariable;
import tyRuBa.modes.ConstructorType;

public class SubstantiateVisitor implements TermVisitor {

	Frame subst;
	Frame inst;

	public SubstantiateVisitor(Frame subst, Frame inst) {
		this.subst = subst;
		this.inst = inst;
	}

	public Object visit(RBCompoundTerm compoundTerm) {
		ConstructorType typeConst = compoundTerm.getConstructorType();
		return typeConst.apply(
			(RBTerm) compoundTerm.getArg().accept(this));
//		PredicateIdentifier pred = compoundTerm.getPredId();
//		return RBCompoundTerm.makeForVisitor(pred,
//			(RBTerm)compoundTerm.getArgsForVisitor().accept(this));
	}

	public Object visit(RBTuple tuple) {
		RBTerm[] subterms = new RBTerm[tuple.getNumSubterms()];
		for (int i = 0; i < subterms.length; i++) {
			subterms[i] = (RBTerm)tuple.getSubterm(i).accept(this);
		}
		return RBTuple.make(subterms);
	}

	public Object visit(RBPair pair) {
		RBPair head = new RBPair((RBTerm)pair.getCar().accept(this));
		
		RBPair next;
		RBPair prev = head;
		
		RBTerm cdr = (RBTerm)pair.getCdr();
		
		while(cdr instanceof RBPair) {
			pair = (RBPair)cdr;
			next = new RBPair((RBTerm)pair.getCar().accept(this));
			prev.setCdr(next);
			prev = next;
			cdr = pair.getCdr();
		}
		
		prev.setCdr((RBTerm)cdr.accept(this));
		
		return head;
	}

	public Object visit(RBQuoted quoted) {
		return new RBQuoted(
			(RBTerm)quoted.getQuotedParts().accept(this));
	}

	public Object visit(RBVariable var) {
		RBTerm val = (RBTerm) subst.get(var);
		if (val == null) {
			return (RBTerm) var.accept(new InstantiateVisitor(inst));
		} else {
			return (RBTerm) val.accept(this);
		}
	}

	public Object visit(RBIgnoredVariable ignoredVar) {
		return ignoredVar;
	}
	
	public Object visit(RBTemplateVar templVar) {
		//Instantiation only happens at runtime. TemplateVar should not
		//exsit any more at runtime so...
		throw new Error("Unsupported operation");
	}

}
