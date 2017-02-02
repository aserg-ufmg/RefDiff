package tyRuBa.engine;

/** An "avoid recursion" context guards against infinite recursion. 

    When recursion gets "to deep" an error is thrown.
    Some debug information (the rule stack) is output to System.err.
*/
public class RBAvoidRecursion extends RBContext {
	/** The "parrent" context */
	protected RBContext guarded;
	/** The current rule (DEBUG info) */
	protected RBRule rule;

	private int depth;

	private static int maxDepth = 0;

	public static int depthLimit = 250;

	public RBAvoidRecursion(RBContext aContext, RBRule r) {
		rule = r;
		guarded = aContext;
		depth = aContext.depth() + 1;
		if (depth > maxDepth) {
			//System.err.println("DEPTH : "+depth);
			maxDepth = depth;
			if (depth == depthLimit) {
				System.err.print(this);
				throw new Error("To deep recursion in rule application");
			}
		}
	}

	int depth() {
		return depth;
	}

	public String toString() {
		StringBuffer result = new StringBuffer(rule + "\n");
		if (guarded instanceof RBAvoidRecursion)
			result.append(guarded.toString());
		else
			result.append("--------------------");
		return result.toString();
	}

}
