package tyRuBa.util;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

/*
 * This is a helper class for printing a representation of
 * ElementSources and ElementCollectors for the purpose of debugging.
 */
public class PrintingState {
	
	/** 
	 * Since Collectors can be recursive we must avoid running in circles. Thus
	 * the PrintingState keeps track of where the printing process has visited.
	 */
	Set visited = new HashSet();
	
	int indentationLevel = 0;
	int column = 0;
	
	PrintStream out;

	public PrintingState(PrintStream s) {
		out = s;
	}

	public void print(String o) {
		String s = o.toString();
		column += s.length();
		out.print(s);
	}
	
	void println(String o) {
		print(o);
		newline();
	}

	void newline() {
		out.println();
		for (column = 0; column < indentationLevel; column++) {
			out.print(" ");
		}
	}

	void indent() {
		indentationLevel += 2;
	}

	void outdent() {
		indentationLevel -= 2;
	}

	protected void printObj(Object object) {
		if (object instanceof ElementSource) {
			((ElementSource)object).print(this);
		}
		else
			print(object.toString());
	}

}
