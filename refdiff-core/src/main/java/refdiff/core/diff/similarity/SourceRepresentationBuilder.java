package refdiff.core.diff.similarity;

import java.util.List;

import refdiff.core.rast.RastNode;

public interface SourceRepresentationBuilder<T> {
	
	T buildForNode(RastNode node, boolean isBefore, List<String> tokenizedSourceCode);
	
	T buildForName(RastNode node, boolean isBefore);
	
	T buildForFragment(List<String> tokenizedSourceCode);
	
	T combine(T arg1, T arg2);
	
	T minus(T arg1, T arg2);
	
	T minus(T arg1, List<String> tokensToRemove);
	
	double similarity(T arg1, T arg2);
	
	double partialSimilarity(T arg1, T arg2);

}
