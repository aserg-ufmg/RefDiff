package refdiff.core.diff;

/**
 * Represents the types of relationships between CST nodes in a CST diff.
 * All types of relationships, except for SAME, represent refactorings applied.
 */
public enum RelationshipType {
	
	/**
	 * Represents nodes that preserved their identities between revisions. Note that their content might 
	 * have changed (or not).
	 */
	SAME(true, true, true, true),
	/**
	 * Represents nodes that preserved their identities between revisions, but have different types. 
	 * E.g.: convert a class to an interface.
	 */
	CONVERT_TYPE(true, true, true, true),
	/**
	 * Represents matched nodes whose signature changed. 
	 * E.g.: add/remove parameter of a function.
	 */
	CHANGE_SIGNATURE(true, true, true, false),
	/**
	 * Represents a member of a type that was pulled up to a supertype.
	 */
	PULL_UP(true, true, true, true),
	/**
	 * Represents a member of a type that was pushed down to a subtype.
	 */
	PUSH_DOWN(true, true, true, true),
	/**
	 * Represents a member of a type whose signature was pulled up to a subtype, keeping its implementation in the original type.
	 */
	PULL_UP_SIGNATURE(false, true, false, true),
	/**
	 * Represents a member of a type whose implementation was pushed down to a subtype, keeping its signature in the original type.
	 */
	PUSH_DOWN_IMPL(false, true, false, true),
	/**
	 * Represents matched nodes whose names changed.
	 */
	RENAME(true, true, true, false),
	/**
	 * Represents matched nodes whose parent nodes changed, but not their root parent nodes.
	 * E.g.: A method that moved from one inner class to another one, but both are within the same top-level class.
	 */
	INTERNAL_MOVE(true, true, true, false),
	/**
	 * Represents matched nodes whose root parent nodes changed.
	 */
	MOVE(true, true, true, false),
	/**
	 * Represents matched nodes with a combination of INTERNAL_MOVE and RENAME.
	 */
	INTERNAL_MOVE_RENAME(true, true, true, false),
	/**
	 * Represents matched nodes with a combination of MOVE and RENAME.
	 */
	MOVE_RENAME(true, true, true, false),
	/**
	 * Represents that the node after is a supertype extracted from the node before.
	 */
	EXTRACT_SUPER(false, true, false, false),
	/**
	 * Represents that the node after is extracted from the node before.
	 */
	EXTRACT(false, true, false, false),
	/**
	 * Represents that the node after is extracted from the node before and moved to another parent node.
	 */
	EXTRACT_MOVE(false, true, false, false),
	/**
	 * Represents that the node before is inlined to the node after.
	 */
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
