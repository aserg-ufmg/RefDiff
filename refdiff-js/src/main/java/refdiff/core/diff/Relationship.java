package refdiff.core.diff;

import java.util.Objects;

import refdiff.core.rast.RastNode;

public class Relationship {
	
	private final RelationshipType type;
	private final RastNode nodeBefore;
	private final RastNode nodeAfter;
	
	public Relationship(RelationshipType type, RastNode nodeBefore, RastNode nodeAfter) {
		this.type = type;
		this.nodeBefore = nodeBefore;
		this.nodeAfter = nodeAfter;
	}
	
	public RelationshipType getType() {
		return type;
	}
	
	public RastNode getNodeBefore() {
		return nodeBefore;
	}
	
	public RastNode getNodeAfter() {
		return nodeAfter;
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
		return String.format("%s(%s, %s)", this.type, this.nodeBefore, this.nodeAfter);
	}
}
