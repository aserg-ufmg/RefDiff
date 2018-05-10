package refdiff.core.diff.similarity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Vocabulary {
	
	private FreqCounter dc = new FreqCounter();
	private Map<String, FreqCounter> df = new HashMap<String, FreqCounter>();
	
	public void count(boolean isBefore, Collection<String> occurrences) {
		dc.increment(isBefore);
		for (String term : occurrences) {
			count(isBefore, term);
		}
	}

	public double getIdf(String key) {
		double documentCount = getDc();
		double documentFreq = getDf(key);
		return Math.max(0.01, Math.log(documentCount / documentFreq));
	}

	public int getDc() {
		return dc.getMax();
	}
	
	public int getDf(String key) {
		return df.get(key).getMax();
	}
	
	private void count(boolean isBefore, String term) {
		df.computeIfAbsent(term, key -> new FreqCounter()).increment(isBefore);
	}
	
	private static class FreqCounter {
		int freqBefore = 0;
		int freqAfter = 0;

		public FreqCounter increment(boolean isBefore) {
			if (isBefore) {
				freqBefore++;
			} else {
				freqAfter++;
			}
			return this;
		}
		
		public int getMax() {
			return Math.max(freqBefore, freqAfter);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Documents: %d\n", getDc()));
		for (String term : df.keySet()) {
			sb.append(String.format("%s\t%d\t%f\n", term, getDf(term), getIdf(term)));
		}
		return sb.toString();
	}
}
