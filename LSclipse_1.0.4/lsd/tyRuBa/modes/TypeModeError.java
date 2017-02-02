/*
 * Created on May 26, 2003
 */
package tyRuBa.modes;

import tyRuBa.engine.RBComponent;
import tyRuBa.engine.RBExpression;
import tyRuBa.engine.RBTerm;

public class TypeModeError extends Exception {

	public TypeModeError(String arg0) {
		super(arg0);
	}
	
	public TypeModeError(TypeModeError e, String str) {
		this(e.getMessage() + "\nin " + str);
	}

	public TypeModeError(TypeModeError e, RBComponent r) {
		this(e, r.toString());
	}
	
	public TypeModeError(TypeModeError e, RBTerm t) {
		this(e, t.toString());
	}
	
	public TypeModeError(TypeModeError e, RBExpression exp) {
		this(e, exp.toString());
	}
	
	public TypeModeError(TypeModeError e, Type t) {
		this(e, "Type: " + t.toString());
	}
	
	public TypeModeError(TypeModeError e, TupleType types) {
		this(e, types.toString());
	}
}
