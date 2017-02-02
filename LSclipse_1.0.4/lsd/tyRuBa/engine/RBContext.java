package tyRuBa.engine;

/** A context is information passed around in eval and unify 
    */
public class RBContext {

	public RBContext() {
	}

	/*
	public void assert(RBTerm fact) {
	rb.insert(new RBFact(fact));
	}
	
	public void retract(RBTerm fact) {
	rb.retract(new RBFact(fact));
	}
	*/
	public String toString() {
		return "-- RBContext --";
	}
	/*
	RBContext removeFailAvoiders() {
	  return this;
	}
	*/
	//  RuleBase ruleBase() { return rb;}

	int depth() {
		return 0;
	}

	/** Unify this with other 
	public final ElementSource unify(RBTerm other,RBContext context) {
	  return rb.unify(other,context);
	}
	*/

}
