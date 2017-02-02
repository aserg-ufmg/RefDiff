/*****************************************************************\
 * File:        Free.java
 * Author:      TyRuBa
 * Meta author: Kris De Volder <kdvolder@cs.ubc.ca>
\*****************************************************************/
package tyRuBa.modes;

public class Free extends BindingMode {
	
	static public Free the = new Free();

	private Free() {}

	public int hashCode() {
		return this.getClass().hashCode();
	}

	public boolean equals(Object other) {
		return other instanceof Free;
	}

	public String toString() {
		return "F";
	}

	/** check that this binding satisfied the binding mode */
	public boolean satisfyBinding(BindingMode mode) {
		return this.equals(mode);
	}

	public boolean isBound() {
		return false;
	}
	public boolean isFree() {
		return true;
	}
}
