/*
 * Created on Jul 15, 2004
 */
package tyRuBa.util;

import tyRuBa.engine.RBExpression;

public abstract class QueryLogger {
	
    public QueryLogger() {}
    
    public abstract void close(); 
    
    public abstract void logQuery(RBExpression query);
}
