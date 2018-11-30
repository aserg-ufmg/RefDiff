package refdiff.core.diff;

public enum RelationshipType {
	
	SAME(true, true, true, true),
	CONVERT_TYPE(true, true, true, true),
	CHANGE_SIGNATURE(true, true, true, false),
	PULL_UP(true, true, true, true),
	PUSH_DOWN(true, true, true, true),
	PULL_UP_SIGNATURE(false, true, false, true),
	PUSH_DOWN_IMPL(false, true, false, true),
	RENAME(true, true, true, false),
	INTERNAL_MOVE(true, true, true, false),
	MOVE(true, true, true, false),
	INTERNAL_MOVE_RENAME(true, true, true, false),
	MOVE_RENAME(true, true, true, false),
	EXTRACT_SUPER(false, true, false, false),
	EXTRACT(false, true, false, false),
	EXTRACT_MOVE(false, true, false, false),
	INLINE(true, false, false, false);
	
	private final boolean unmarkRemoved;
	private final boolean unmarkAdded;
	private final boolean matching;
	private final boolean byId;
	
	private RelationshipType(boolean unmarkRemoved, boolean unmarkAdded, boolean matching, boolean byId) {
		this.unmarkRemoved = unmarkRemoved;
		this.unmarkAdded = unmarkAdded;
		this.matching = matching;
		this.byId = byId;
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

	public boolean isById() {
		return byId;
	}
	
}
