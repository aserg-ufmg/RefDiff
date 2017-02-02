/*
 * Created on Feb 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package tyRuBa.tdbc;

import java.io.ByteArrayInputStream;

import tyRuBa.engine.QueryEngine;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.parser.TyRuBaParser;

/**
 * @author kdvolder
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Insert {

	private QueryEngine queryEngine;
	
	Insert(QueryEngine queryEngine) {
		this.queryEngine = queryEngine;
	}
	
	public void executeInsert(String insertString) throws TyrubaException {
		TyRuBaParser parser = new TyRuBaParser(new ByteArrayInputStream(insertString.getBytes()),System.err);
		try {
			parser.Rule(queryEngine);
		} catch (ParseException e) {
			throw new TyrubaException(e);
		} catch (TypeModeError e) {
			throw new TyrubaException(e);
		}
	}

}
