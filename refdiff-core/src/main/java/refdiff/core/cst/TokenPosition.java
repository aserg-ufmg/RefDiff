package refdiff.core.cst;

public class TokenPosition {
	private final int start;
	private final int end;
	
	public TokenPosition(int start, int end) {
		this.start = start;
		this.end = end;
	}
	
	public int getStart() {
		return start;
	}
	
	public int getEnd() {
		return end;
	}
	
}
