package refdiff.core.util;

public class PairBeforeAfter<T> {
	
	private final T before;
	private final T after;
	
	public PairBeforeAfter(T before, T after) {
		this.before = before;
		this.after = after;
	}
	
	public T getBefore() {
		return before;
	}
	
	public T getAfter() {
		return after;
	}
	
}
