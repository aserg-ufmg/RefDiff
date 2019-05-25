package refdiff.evaluation;

import refdiff.core.diff.CstComparatorMonitor;
import refdiff.core.cst.CstNode;

public class CstComparatorDebbuger implements CstComparatorMonitor {
	
	@Override
	public void reportDiscardedMatch(CstNode n1, CstNode n2, double score) {
		System.out.println(String.format("Discarded %s %s with threshold %f", n1, n2, score));
	}
	
}
