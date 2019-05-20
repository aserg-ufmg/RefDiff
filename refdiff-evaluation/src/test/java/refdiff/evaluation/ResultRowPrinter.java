package refdiff.evaluation;

@FunctionalInterface
public interface ResultRowPrinter {
	
	void printDetails(RefactoringSet rs, RefactoringRelationship r, String label, String cause, EvaluationDetails evaluationDetails);
	
}
