package refdiff.core.diff;

import refdiff.core.diff.CstComparator.DiffBuilder;
import refdiff.core.cst.CstNode;

public interface CstComparatorMonitor {
	
	default void beforeCompare(CstRootHelper<?> before, CstRootHelper<?> after) {}
	
	default void reportMatchDiscardedBySimilarity(CstNode n1, CstNode n2, double score, double threshold) {}
	
	default void reportMatchDiscardedByConflict(CstNode nBefore, CstNode nAfter) {}
	
	default void reportExtractDiscardedBySimilarity(CstNode n1, CstNode n2, double score, double threshold) {}
	
	default void reportInlineDiscardedBySimilarity(CstNode n1, CstNode n2, double score, double threshold) {}

	default void afterCompare(long elapsedTime, DiffBuilder<?> diffBuilder) {}
}
