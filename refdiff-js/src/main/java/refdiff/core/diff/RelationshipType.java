package refdiff.core.diff;

public enum RelationshipType {
	
	SAME(false, false),
	MOVE(false, false),
	RENAME(false, false),
	EXTRACT(true, false),
	INLINE(false, true),
	PULL_UP(true, false),
	PUSH_DOWN(false, true);
	
	private final boolean multisource;
	private final boolean multitarget;
	
	private RelationshipType(boolean multisource, boolean multitarget) {
		this.multisource = multisource;
		this.multitarget = multitarget;
	}
	
	public boolean isMultisource() {
		return multisource;
	}
	
	public boolean isMultitarget() {
		return multitarget;
	}
	
}
