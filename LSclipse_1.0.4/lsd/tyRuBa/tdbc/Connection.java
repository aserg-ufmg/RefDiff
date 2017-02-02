/*
 * Created on Feb 2, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package tyRuBa.tdbc;

import tyRuBa.engine.QueryEngine;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;

/**
 * @author dsjanzen
 *
 */
public class Connection {

	private QueryEngine queryEngine;
	
	public Connection(QueryEngine queryEngine) {
		this.queryEngine = queryEngine;
	}
	
	public Query createQuery() {
		return new Query(queryEngine);
	}

	public Insert createInsert() {
		return new Insert(queryEngine);
	}
	
	public PreparedQuery prepareQuery(String qry) throws TyrubaException {
		try {
			return queryEngine.prepareForRunning(qry);
		} catch (ParseException e) {
			throw new TyrubaException(e);
		} catch (TypeModeError e) {
			throw new TyrubaException(e);
		}
	}

	public PreparedInsert prepareInsert(String fact) throws ParseException, TypeModeError {
		return queryEngine.prepareForInsertion(fact);		
	}

}
