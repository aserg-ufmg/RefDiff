package refdiff.core.diff;

public enum RelationshipType {
	
	SAME(true, true, true),
	CONVERT_TYPE(true, true, true),
	CHANGE_SIGNATURE(true, true, true),
	MOVE(true, true, true),
	INTERNAL_MOVE(true, true, true),
	MOVE_RENAME(true, true, true),
	INTERNAL_MOVE_RENAME(true, true, true),
	RENAME(true, true, true),
	EXTRACT(false, true, false),
	EXTRACT_MOVE(false, true, false),
	EXTRACT_SUPER(false, true, false),
	INLINE(true, false, false),
	PULL_UP(true, false, false),
	PUSH_DOWN(false, true, false),
	PULL_UP_SIGNATURE(true, false, false),
	PUSH_DOWN_IMPL(false, true, false);
	
	private final boolean unmarkRemoved;
	private final boolean unmarkAdded;
	private final boolean matching;
	
	private RelationshipType(boolean unmarkRemoved, boolean unmarkAdded, boolean matching) {
		this.unmarkRemoved = unmarkRemoved;
		this.unmarkAdded = unmarkAdded;
		this.matching = matching;
	}

	public boolean isUnmarkRemoved() {
		return unmarkRemoved;
	}

	public boolean isUnmarkAdded() {
		return unmarkAdded;
	}

	public boolean isMatching() {
		return matching;
	}
	
}
