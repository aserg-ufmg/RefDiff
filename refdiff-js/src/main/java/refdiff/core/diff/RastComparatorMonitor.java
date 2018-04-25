package refdiff.core.diff;

import refdiff.core.rast.RastNode;

public interface RastComparatorMonitor {
	
	default void reportDiscardedMatch(RastNode n1, RastNode n2, double score) {}
	
	default void reportDiscardedConflictingMatch(RastNode nBefore, RastNode nAfter) {}
	
	default void reportDiscardedExtract(RastNode n1, RastNode n2, double score) {}
	
	default void reportDiscardedInline(RastNode n1, RastNode n2, double score) {}

	
}
