/*****************************************************************\
 * File:        BindingMode.java
 * Author:      TyRuBa
 * Meta author: Kris De Volder <kdvolder@cs.ubc.ca>
\*****************************************************************/
package tyRuBa.modes;

abstract public class BindingMode {
	
	public abstract boolean isBound();
	public abstract boolean isFree();

	public abstract boolean satisfyBinding(BindingMode mode);
	
}
