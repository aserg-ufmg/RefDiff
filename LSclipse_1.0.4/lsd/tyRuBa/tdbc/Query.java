/*
 * Created on Feb 2, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package tyRuBa.tdbc;

import tyRuBa.engine.QueryEngine;

/**
 * @author dsjanzen
 */
public class Query {
	
	private QueryEngine queryEngine;
	
	Query(QueryEngine queryEngine) {
		this.queryEngine = queryEngine;
	}
	
	public ResultSet executeQuery(String queryString) throws TyrubaException {
		return new ResultSet(queryEngine, queryString);
	}

}
