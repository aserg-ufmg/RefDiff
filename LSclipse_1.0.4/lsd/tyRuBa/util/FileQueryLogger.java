/*
 * Created on Jul 15, 2004
 */
package tyRuBa.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import tyRuBa.engine.RBExpression;

public class FileQueryLogger extends QueryLogger {
	
    PrintWriter writer;
    
    public void close() {
    		writer.close();
    }
    
    public FileQueryLogger(File logFile, boolean append) throws IOException {
        writer = new PrintWriter(new FileOutputStream(logFile, append));
        writer.println("//SCENARIO");
    }
    
    public void logQuery(RBExpression query) {
        writer.println(query.toString());
    }
}
