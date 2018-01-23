package refdiff.core.diff.similarity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Vocabulary {
	
	private int dc = 0;
	private Map<String, Integer> df = new HashMap<String, Integer>();
	
	public double idf(String key) {
		return Math.log(1.0 + (((double) dc) / df.get(key)));
	}
	
	public void count(Collection<String> occurrences) {
		dc++;
		for (String term : occurrences) {
			df.merge(term, 1, (oldValue, newValue) -> oldValue + newValue);
		}
	}
	
}
