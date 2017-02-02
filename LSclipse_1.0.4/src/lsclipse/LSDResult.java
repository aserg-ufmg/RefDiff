package lsclipse;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import lsd.rule.LSDFact;
import lsd.rule.LSDVariable;

public class LSDResult {
	public int num_matches;
	public int num_counter;
	public String desc;
	public java.util.List<LSDFact> examples;
	public java.util.List<Map<LSDVariable, String>> exceptions;
	private ArrayList<String> examplesString = null;
	private ArrayList<String> exceptionsString = null;
	public ArrayList<String> getExampleStr() {
		if (examplesString==null) {	//contruct strings from examples
			examplesString = new ArrayList<String>();
			for (LSDFact fact : examples) {
				examplesString.add(fact.toString());
			}
		}
		return examplesString;
	}
	public ArrayList<String> getExceptionsString() {
		if (exceptionsString==null) {	//contruct strings from examples
			exceptionsString = new ArrayList<String>();
			for (Map<LSDVariable, String> exception : exceptions) {
				StringBuilder s = new StringBuilder();
				s.append("[ ");
				for (Entry<LSDVariable, String> entry : exception.entrySet()) {
					s.append(entry.getKey());
					s.append("=\"");
					s.append(entry.getValue());
					s.append("\" ");
				}
				s.append("]");
				exceptionsString.add(s.toString());
			}
		}
		return exceptionsString;
	}
}
