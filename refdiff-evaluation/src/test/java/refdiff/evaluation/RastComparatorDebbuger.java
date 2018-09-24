package refdiff.evaluation;

import refdiff.core.diff.RastComparatorMonitor;
import refdiff.core.rast.RastNode;

public class RastComparatorDebbuger implements RastComparatorMonitor {
	
	@Override
	public void reportDiscardedMatch(RastNode n1, RastNode n2, double score) {
		System.out.println(String.format("Discarded %s %s with threshold %f", n1, n2, score));
	}
	
}
