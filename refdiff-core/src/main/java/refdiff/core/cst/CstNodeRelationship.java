package refdiff.core.cst;

import java.util.Objects;

public class CstNodeRelationship {
	
	private final CstNodeRelationshipType type;
	private final int n1;
	private final int n2;
	
	public CstNodeRelationship(CstNodeRelationshipType type, int n1, int n2) {
		this.type = type;
		this.n1 = n1;
		this.n2 = n2;
	}
	
	public CstNodeRelationshipType getType() {
		return type;
	}
	
	public int getN1() {
		return n1;
	}

	public int getN2() {
		return n2;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CstNodeRelationship) {
			CstNodeRelationship otherRelationship = (CstNodeRelationship) obj;
			return Objects.equals(this.type, otherRelationship.type) &&
				this.n1 == otherRelationship.n1 &&
				this.n2 == otherRelationship.n2;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.type, this.n1, this.n2);
	}
	
	@Override
	public String toString() {
		return String.format("%d %s %d", n1, type, n2);
	}
}
