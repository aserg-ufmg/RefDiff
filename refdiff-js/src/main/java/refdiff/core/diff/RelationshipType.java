package refdiff.core.diff;

public enum RelationshipType {
	
	SAME(true, true),
	MOVE(true, true),
	RENAME(true, true),
	EXTRACT(false, true),
	EXTRACT_SUPER(false, true),
	INLINE(false, true),
	PULL_UP(true, true),
	PUSH_DOWN(true, true);
	
	private final boolean unmarkRemoved;
	private final boolean unmarkAdded;
	
	private RelationshipType(boolean unmarkRemoved, boolean unmarkAdded) {
		this.unmarkRemoved = unmarkRemoved;
		this.unmarkAdded = unmarkAdded;
	}

	public boolean isUnmarkRemoved() {
		return unmarkRemoved;
	}

	public boolean isUnmarkAdded() {
		return unmarkAdded;
	}
	
}
