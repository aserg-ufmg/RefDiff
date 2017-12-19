package refdiff.core.diff;

public class RastComparatorThresholds {
	public final double moveOrRename;
	public final double extract;
	public final double inline;
	
	public RastComparatorThresholds(double moveOrRename, double extract, double inline) {
		this.moveOrRename = moveOrRename;
		this.extract = extract;
		this.inline = inline;
	}
	
	public static final RastComparatorThresholds DEFAULT = new RastComparatorThresholds(0.5, 0.2, 0.3);
	
}
