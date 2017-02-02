/*
 * Created on Feb 2, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package tyRuBa.tdbc;

import tyRuBa.engine.Frame;
import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.QueryEngine;
import tyRuBa.engine.RBCompoundTerm;
import tyRuBa.engine.RBExpression;
import tyRuBa.engine.RBRepAsJavaObjectCompoundTerm;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBVariable;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.util.ElementSource;

/**
 * @author dsjanzen
 *
 */
public class ResultSet {

	private ElementSource eltSource;
	private Frame frame = null;
	
	public ResultSet(ElementSource eltSource) {
		this.eltSource = eltSource;		
	}
	
	ResultSet(QueryEngine queryEngine, String query) throws TyrubaException {
		try {
			eltSource = queryEngine.frameQuery(query);
		} catch (ParseException e) {
			throw new TyrubaException(e);
		} catch (TypeModeError e) {
			throw new TyrubaException(e);
		}	
	}
	
	public ResultSet(QueryEngine queryEngine, RBExpression query) throws TyrubaException {
		try {
			eltSource = queryEngine.frameQuery(query);
		} catch (ParseException e) {
			throw new TyrubaException(e);
		} catch (TypeModeError e) {
			throw new TyrubaException(e);
		}	
	}

	public boolean next() throws TyrubaException {
		boolean more = eltSource.hasMoreElements();
		if (more) {
			frame = (Frame) eltSource.nextElement();
		}
		else {
			frame = null;
			
		}
		return more;
	}
	
	public Object getObject(String variableName) throws TyrubaException {
		if (frame == null) {
			throw new TyrubaException("There are no more elements in the current result set.");
		}
		RBVariable var = FrontEnd.makeVar(variableName);
		RBTerm term = frame.get(var);
		if (term instanceof RBCompoundTerm) {
            if (term instanceof RBRepAsJavaObjectCompoundTerm) {
                return ((RBRepAsJavaObjectCompoundTerm)term).getValue();
            } else if (((RBCompoundTerm)term).getNumArgs() == 1) {
                if (((RBCompoundTerm)term).getArg() != null) {
                    return ((RBCompoundTerm)term).getArg().up();
                } else {
                    return term.up();
                }
            } else {
                return term;
            }
        }
		else {
			return term;
		}
	}

	public String getString(String variableName) throws TyrubaException {
		Object o = getObject(variableName);
		if (!(o instanceof String)) {
			throw wrongType(variableName, o, "String");
		}
		return (String) o; 
	}

	/**
	 */
	public int getInt(String variableName) throws TyrubaException {
		Object o = getObject(variableName);
		if (o instanceof Integer) {
			return ((Integer) o).intValue();
		}
		throw wrongType(variableName, o, "int");
	}

	private TyrubaException wrongType(String varName, Object found, String expectedType) {
		return new TyrubaException("Variable "+varName+" is bound to an object of type "+found.getClass().getName()+" not "+expectedType+".");
	}
	
}


