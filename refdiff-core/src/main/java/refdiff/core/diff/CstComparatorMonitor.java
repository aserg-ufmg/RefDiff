package refdiff.core.diff;

import refdiff.core.diff.CstComparator.DiffBuilder;
import refdiff.core.cst.CstNode;

public interface CstComparatorMonitor {
	
	default void beforeCompare(CstRootHelper<?> before, CstRootHelper<?> after) {}
	
	default void reportDiscardedMatch(CstNode n1, CstNode n2, double score) {}
	
	default void reportDiscardedConflictingMatch(CstNode nBefore, CstNode nAfter) {}
	
	default void reportDiscardedExtract(CstNode n1, CstNode n2, double score) {}
	
	default void reportDiscardedInline(CstNode n1, CstNode n2, double score) {}

	default void afterCompare(long elapsedTime, DiffBuilder<?> diffBuilder) {}
}
