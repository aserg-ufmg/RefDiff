package refdiff.core.diff;

import java.util.Objects;

import refdiff.core.cst.CstNode;

public class Relationship {
	
	private final RelationshipType type;
	private final CstNode nodeBefore;
	private final CstNode nodeAfter;
	private final Double similarity;
	
	public Relationship(RelationshipType type, CstNode nodeBefore, CstNode nodeAfter) {
		this(type, nodeBefore, nodeAfter, null);
	}
	
	public Relationship(RelationshipType type, CstNode nodeBefore, CstNode nodeAfter, Double similarity) {
		this.type = type;
		this.nodeBefore = nodeBefore;
		this.nodeAfter = nodeAfter;
		this.similarity = similarity;
	}
	
	public RelationshipType getType() {
		return type;
	}
	
	public CstNode getNodeBefore() {
		return nodeBefore;
	}
	
	public CstNode getNodeAfter() {
		return nodeAfter;
	}
	
	public Double getSimilarity() {
		return similarity;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Relationship) {
			Relationship otherRelationship = (Relationship) obj;
			return Objects.equals(this.type, otherRelationship.type) &&
				Objects.equals(this.nodeBefore, otherRelationship.nodeBefore) &&
				Objects.equals(this.nodeAfter, otherRelationship.nodeAfter);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.type, this.nodeBefore, this.nodeAfter);
	}
	
	@Override
	public String toString() {
		return String.format("%s({%s}, {%s})", this.type, format(this.nodeBefore), format(this.nodeAfter));
	}
	
	public String getStandardDescription() {
		return String.format("%s\t{%s}\t{%s})", this.type, formatWithLineNum(this.nodeBefore), formatWithLineNum(this.nodeAfter));
	}
	
	private String formatWithLineNum(CstNode node) {
		return String.format("%s %s at %s:%d", node.getType(), node.getLocalName(), node.getLocation().getFile(), node.getLocation().getLine());
	}
	
	private String format(CstNode node) {
		return String.join(" ", CstRootHelper.getNodePath(node));
	}
	
	public boolean isRefactoring() {
		return !type.equals(RelationshipType.SAME);
	}
}
