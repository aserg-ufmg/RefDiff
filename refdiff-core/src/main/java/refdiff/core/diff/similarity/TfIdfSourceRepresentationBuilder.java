package refdiff.core.diff.similarity;

import java.util.List;

import refdiff.core.rast.RastNode;
import refdiff.core.util.IdentifierSplitter;

public class TfIdfSourceRepresentationBuilder implements SourceRepresentationBuilder<TfIdfSourceRepresentation> {
	
	Vocabulary vocabulary = new Vocabulary();
	
	@Override
	public TfIdfSourceRepresentation buildForNode(RastNode node, boolean isBefore, List<String> tokenizedSourceCode) {
		Multiset<String> multiset = new Multiset<String>();
		
		// Add tokens from node name and from its parents
		collectTokensOfSimpleName(multiset, node);
		
		multiset.addAll(tokenizedSourceCode);
		vocabulary.count(isBefore, multiset.asSet());
		return new TfIdfSourceRepresentation(multiset, vocabulary);
	}
	
	private static void collectTokensOfSimpleName(Multiset<String> multiset, RastNode node) {
		String nodeName;
		if (node.getNamespace() != null) {
			nodeName = node.getNamespace() + node.getSimpleName();
		} else {
			nodeName = node.getSimpleName();
		}
		List<String> tokens = IdentifierSplitter.split(nodeName);
		multiset.add(nodeName);
		if (tokens.size() > 1) {
			multiset.addAll(tokens);
		}
		if (node.getParent().isPresent()) {
			collectTokensOfSimpleName(multiset, node.getParent().get());
		}
	}
	
	@Override
	public TfIdfSourceRepresentation buildForFragment(List<String> tokenizedSourceCode) {
		Multiset<String> multiset = new Multiset<String>();
		multiset.addAll(tokenizedSourceCode);
		return new TfIdfSourceRepresentation(multiset, vocabulary);
	}
	
	@Override
	public TfIdfSourceRepresentation combine(TfIdfSourceRepresentation arg1, TfIdfSourceRepresentation arg2) {
		return arg1.combine(arg2);
	}
	
	@Override
	public TfIdfSourceRepresentation minus(TfIdfSourceRepresentation arg1, TfIdfSourceRepresentation arg2) {
		return arg1.minus(arg2);
	}
	
	@Override
	public double similarity(TfIdfSourceRepresentation arg1, TfIdfSourceRepresentation arg2) {
		return arg1.similarity(arg2);
	}
	
	@Override
	public double partialSimilarity(TfIdfSourceRepresentation arg1, TfIdfSourceRepresentation arg2) {
		return arg1.partialSimilarity(arg2);
	}
	
	@Override
	public TfIdfSourceRepresentation minus(TfIdfSourceRepresentation arg1, List<String> tokensToRemove) {
		return arg1.minus(tokensToRemove);
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "\n" + vocabulary.toString();
	}

}
