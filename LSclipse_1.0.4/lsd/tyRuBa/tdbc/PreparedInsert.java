/*
 * Created on Feb 24, 2004
 */
package tyRuBa.tdbc;

import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeModeError;
import tyRuBa.tdbc.PreparedStatement;

import tyRuBa.engine.QueryEngine;
import tyRuBa.engine.RBPredicateExpression;

/**
 * @author kdvolder
 */
public class PreparedInsert extends PreparedStatement {
	
	RBPredicateExpression fact;
	  // A fact which may contain templateVars
	
	public PreparedInsert(QueryEngine engine,RBPredicateExpression fact,TypeEnv tEnv) {
		super(engine,tEnv);
		this.fact = fact;
	}

	public void executeInsert() throws TyrubaException {
		checkReadyToRun();
		try {
			getEngine().insert((RBPredicateExpression)fact.substitute(putMap));
		} catch (TypeModeError e) {
			throw new TyrubaException(e);
		}
	}
	
	public String toString() {
		return "PrepIns("+fact+", "+putMap+")";
	}

}
