package refdiff.core.diff;

import refdiff.core.cst.CstNode;

public class PotentialMatch implements Comparable<PotentialMatch> {
	
	private final CstNode nodeBefore;
	private final CstNode nodeAfter;
	private final int maxDepth;
	private final double score;
	
	public PotentialMatch(CstNode nodeBefore, CstNode nodeAfter, int maxDepth, double score) {
		this.nodeBefore = nodeBefore;
		this.nodeAfter = nodeAfter;
		this.maxDepth = maxDepth;
		this.score = score;
	}
	
	public CstNode getNodeBefore() {
		return nodeBefore;
	}
	
	public CstNode getNodeAfter() {
		return nodeAfter;
	}
	
	public double getScore() {
		return score;
	}
	
	public double getMaxDepth() {
		return score;
	}
	
	@Override
	public String toString() {
		return String.format("(%s, %s)", this.nodeBefore, this.nodeAfter);
	}
	
	@Override
	public int compareTo(PotentialMatch o) {
		int c1 = -Double.compare(score, o.score);
		int c2 = Integer.compare(maxDepth, o.maxDepth);
		int c3 = Integer.compare(nodeBefore.getId(), o.nodeBefore.getId());
		return c1 != 0 ? c1 : c2 != 0 ? c2 : c3;
	}
}
