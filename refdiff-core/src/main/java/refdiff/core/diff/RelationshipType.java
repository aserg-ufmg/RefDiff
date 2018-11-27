package refdiff.core.diff;

public enum RelationshipType {
	
	SAME(true, true),
	CONVERT_TYPE(true, true),
	CHANGE_SIGNATURE(true, true),
	MOVE(true, true),
	INTERNAL_MOVE(true, true),
	MOVE_RENAME(true, true),
	INTERNAL_MOVE_RENAME(true, true),
	RENAME(true, true),
	EXTRACT(false, true),
	EXTRACT_MOVE(false, true),
	EXTRACT_SUPER(false, true),
	INLINE(true, false),
	PULL_UP(true, true),
	PUSH_DOWN(true, true),
	PULL_UP_SIGNATURE(false, true),
	PUSH_DOWN_IMPL(false, true);
	
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
