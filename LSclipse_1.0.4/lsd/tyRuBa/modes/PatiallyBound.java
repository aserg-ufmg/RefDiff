package tyRuBa.modes;

public class PatiallyBound extends BindingMode {
	
	static public PatiallyBound the = new PatiallyBound();

	private PatiallyBound() {}

	public int hashCode() {
		return this.getClass().hashCode();
	}

	public boolean equals(Object other) {
		return other instanceof PatiallyBound;
	}

	public String toString() {
		return "BF";
	}

	/** check that this binding satisfied the binding mode */
	public boolean satisfyBinding(BindingMode mode) {
		return mode instanceof Free;
	}

	public boolean isBound() {
		return false;
	}
	public boolean isFree() {
		return false;
	}
}
