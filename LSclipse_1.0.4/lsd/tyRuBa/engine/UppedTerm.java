package tyRuBa.engine;

/** This is wrapper for RBTerms that get passed to Java functions. 
    Is used for RBTerm objects that don't really know how to map
    themselves onto a Java equivalent.  At least this preserves
    the x.up().down() = x  
*/

public class UppedTerm {
	RBTerm term;

	public UppedTerm(RBTerm t) {
		term = t;
	}

	public RBTerm down() {
		return term;
	}

	public String toString() {
		return term.toString();
	}
}
