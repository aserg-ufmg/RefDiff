package refdiff.core.diff.similarity;

import java.util.HashSet;
import java.util.List;
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
	
	public TfIdfSourceRepresentation minus(List<String> tokensToRemove) {
		return new TfIdfSourceRepresentation(tokens.minusElements(tokensToRemove), vocabulary);
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
		return jaccardSimilarity(other, false);
	}
	
	public double partialSimilarity(TfIdfSourceRepresentation other) {
		return jaccardSimilarity(other, true);
	}
	
	public double jaccardSimilarity(TfIdfSourceRepresentation other, boolean partial) {
		double[] tuple = jaccardSimilarityDecomposed(other, partial);
		return tuple[0] / tuple[1];
	}
	
	public double[] jaccardSimilarityDecomposed(TfIdfSourceRepresentation other, boolean partial) {
		Multiset<String> tokens2 = other.tokens;
		if (tokens.isEmpty() || tokens2.isEmpty()) {
			return new double[]{0.0, 1.0};
		}
		Set<String> keys = new HashSet<String>();
		keys.addAll(tokens.asSet());
		keys.addAll(tokens2.asSet());
		double idfu = 0.0;
		double idfd = 0.0;
		for (String key : keys) {
			double c1 = tf(tokens.getMultiplicity(key));
			double c2 = tf(tokens2.getMultiplicity(key));
			double idf = vocabulary.getIdf(key);
			idfu += Math.min(c1 * idf, c2 * idf);
			idfd += Math.max(c1 * idf, c2 * idf);
		}
		if (partial) {
			double idfp = 0.0;
			for (String key : tokens.asSet()) {
				double idf = vocabulary.getIdf(key);
				double c1 = tf(tokens.getMultiplicity(key));
				idfp += c1 * idf;
			}
			return new double[]{idfu, idfp};
		}
		return new double[]{idfu, idfd};
	}
	
	private double tf(int multiplicity) {
		//return Math.log(1.0 + multiplicity);
		return multiplicity;
	}

	public int getSize() {
		return tokens.size();
	}

}
