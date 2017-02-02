/*****************************************************************\
 * File:        Bound.java
 * Author:      TyRuBa
 * Meta author: Kris De Volder <kdvolder@cs.ubc.ca>
\*****************************************************************/
package tyRuBa.modes;

public class Bound extends BindingMode {
	
	static public Bound the = new Bound();

	private Bound() {}

	public int hashCode() {
		return this.getClass().hashCode();
	}

	public boolean equals(Object other) {
		return other instanceof Bound;
	}

	public String toString() {
		return "B";
	}

	public boolean satisfyBinding(BindingMode mode) {
		return true;
	}

	public boolean isBound() {
		return true;
	}
	public boolean isFree() {
		return false;
	}
	
}
