package refdiff.core.diff;

import refdiff.core.cst.CstNode;

public class CstComparatorDebugger implements CstComparatorMonitor {
	
	@Override
	public void reportMatchDiscardedBySimilarity(CstNode n1, CstNode n2, double score, double threshold) {
		System.out.println(String.format("Discarded match with score %.3f <= %.3f\t {%s} {%s}", score, threshold, n1, n2));
	}
	
	@Override
	public void reportMatchDiscardedByConflict(CstNode n1, CstNode n2) {
		System.out.println(String.format("Discarded match by conflict\t {%s} {%s}", n1, n2));
	}
	
	@Override
	public void reportExtractDiscardedBySimilarity(CstNode n1, CstNode n2, double score, double threshold) {
		System.out.println(String.format("Discarded EXTRACT with score %.3f <= %.3f\t {%s} {%s}", score, threshold, n1, n2));
	}
	
	@Override
	public void reportInlineDiscardedBySimilarity(CstNode n1, CstNode n2, double score, double threshold) {
		System.out.println(String.format("Discarded INLINE with score %.3f <= %.3f\t {%s} {%s}", score, threshold, n1, n2));
	}
}
