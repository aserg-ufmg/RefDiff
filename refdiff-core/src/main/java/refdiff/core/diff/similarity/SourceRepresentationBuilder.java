package refdiff.core.diff.similarity;

import java.util.List;

import refdiff.core.cst.CstNode;

public interface SourceRepresentationBuilder<T> {
	
	T buildForNode(CstNode node, boolean isBefore, List<String> tokenizedSourceCode);
	
	T buildForName(CstNode node, boolean isBefore);
	
	T buildForFragment(List<String> tokenizedSourceCode);
	
	T combine(T arg1, T arg2);
	
	T minus(T arg1, T arg2);
	
	T minus(T arg1, List<String> tokensToRemove);
	
	double similarity(T arg1, T arg2);
	
	double partialSimilarity(T arg1, T arg2);

	double rawSimilarity(T arg1, T arg2);

	int size(T arg);

}
