package refdiff.core.diff;

import refdiff.core.diff.RastComparator.DiffBuilder;
import refdiff.core.rast.RastNode;

public interface RastComparatorMonitor {
	
	default void beforeCompare(RastRootHelper<?> before, RastRootHelper<?> after) {}
	
	default void reportDiscardedMatch(RastNode n1, RastNode n2, double score) {}
	
	default void reportDiscardedConflictingMatch(RastNode nBefore, RastNode nAfter) {}
	
	default void reportDiscardedExtract(RastNode n1, RastNode n2, double score) {}
	
	default void reportDiscardedInline(RastNode n1, RastNode n2, double score) {}

	default void afterCompare(long elapsedTime, DiffBuilder<?> diffBuilder) {}
}
