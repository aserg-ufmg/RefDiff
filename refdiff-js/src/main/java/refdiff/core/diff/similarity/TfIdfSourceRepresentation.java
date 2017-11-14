package refdiff.core.diff.similarity;

import java.util.HashSet;
import java.util.Set;

public class TfIdfSourceRepresentation {
	
	private final Multiset<String> tokens;
	private final Vocabulary vocabulary;
	
	public TfIdfSourceRepresentation(Multiset<String> tokens, Vocabulary vocabulary) {
		this.tokens = tokens;
		this.vocabulary = vocabulary;
	}
	
	public TfIdfSourceRepresentation minus(TfIdfSourceRepresentation other) {
		return new TfIdfSourceRepresentation(tokens.minus((other).tokens), vocabulary);
	}
	
	public String toString() {
		return tokens.toString();
	}
	
	public TfIdfSourceRepresentation combine(TfIdfSourceRepresentation sr) {
		Multiset<String> multisetUnion = tokens;
		TfIdfSourceRepresentation tokenIdfSR = (TfIdfSourceRepresentation) sr;
		multisetUnion = multisetUnion.plus(tokenIdfSR.tokens);
		return new TfIdfSourceRepresentation(multisetUnion, vocabulary);
	}
	
	public double similarity(TfIdfSourceRepresentation other) {
		return jaccardSimilarity(((TfIdfSourceRepresentation) other).tokens, false);
	}
	
	public double partialSimilarity(TfIdfSourceRepresentation other) {
		return jaccardSimilarity(((TfIdfSourceRepresentation) other).tokens, true);
	}
	
	public double jaccardSimilarity(Multiset<String> tokens2, boolean partial) {
		if (tokens.isEmpty() || tokens2.isEmpty()) {
			return 0.0;
		}
		Set<String> keys = new HashSet<String>();
		keys.addAll(tokens.asSet());
		keys.addAll(tokens2.asSet());
		double idfu = 0.0;
		double idfd = 0.0;
		for (String key : keys) {
			int c1 = tokens.getMultiplicity(key);
			int c2 = tokens2.getMultiplicity(key);
			idfu += Math.min(c1, c2) * vocabulary.idf(key);
			idfd += Math.max(c1, c2) * vocabulary.idf(key);
		}
		if (partial) {
			double idfp = 0.0;
			for (String key : tokens.asSet()) {
				int c1 = tokens.getMultiplicity(key);
				idfp += c1 * vocabulary.idf(key);
			}
			return idfu / idfp;
		}
		return idfu / idfd;
	}
	
}
