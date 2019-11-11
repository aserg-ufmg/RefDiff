package refdiff.evaluation.icse;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import refdiff.core.diff.CstComparator;
import refdiff.core.diff.Relationship;
import refdiff.evaluation.EvaluationDetails;
import refdiff.evaluation.EvaluationUtils;
import refdiff.evaluation.KeyPair;
import refdiff.evaluation.RefactoringRelationship;
import refdiff.evaluation.RefactoringSet;
import refdiff.evaluation.RefactoringType;
import refdiff.evaluation.ResultComparator;
import refdiff.parsers.java.JavaPlugin;

public class RunIcseEval {
	
	public static EnumSet<RefactoringType> refactoringTypes = EnumSet.complementOf(EnumSet.of(RefactoringType.PULL_UP_ATTRIBUTE, RefactoringType.PUSH_DOWN_ATTRIBUTE, RefactoringType.MOVE_ATTRIBUTE));
	private EvaluationUtils evalUtils;
	
	public RunIcseEval(String tempFolder) {
		evalUtils = new EvaluationUtils(new CstComparator(new JavaPlugin()), tempFolder);
	}

	public static void main(String[] args) throws Exception {
		new RunIcseEval(args.length > 0 ? args[0] : "D:/refdiff/").run();
	}
	
	public void run() throws Exception {
		IcseDataset data = new IcseDataset();
		List<RefactoringSet> expected = data.getExpected();
		
		ResultComparator rc = EvaluationCsvReader.buildResultComparator(data, EvaluationCsvReader.readEvalAll());
		
		int count = 0;
		int errorCount = 0;
		for (int i = 0; i < expected.size(); i++) {
			RefactoringSet rs = expected.get(i);
			String project = rs.getProject();
			String commit = rs.getRevision();
			try {
				System.out.printf("%d/%d - ", i + 1, expected.size());
				evalUtils.prepareSourceCodeLightCheckout(project, commit);
				
				Map<KeyPair, String> explanations = new HashMap<>();
				rc.compareWith("RefDiff2", evalUtils.runRefDiff(project, commit, explanations, rs));
				rc.addFnExplanations(project, commit, explanations);
			} catch (RuntimeException e) {
				errorCount++;
				System.err.println(String.format("Skipped %s %s", project, commit));
				System.err.println(e.getMessage());
				continue;
			}
			count++;
		}
		
		rc.compareWith("RefDiff1", data.getRefDiffRefactorings());
		rc.compareWith("RMiner", data.getrMinerRefactorings());
		
		System.out.println("\n\n\n");
		rc.printDetails2(System.out, refactoringTypes);
		System.out.println();
		rc.printSummary(System.out, refactoringTypes);
		
		System.out.println(String.format("%d commits processed, %d commits skipped.", count, errorCount));
	}
	
	public static void printDetails(RefactoringSet rs, RefactoringRelationship r, String label, String cause, EvaluationDetails evaluationDetails) {
		String refDiffRefType = "";
		String n1Location = "";
		String n2Location = "";
		Relationship cstRelationship = r.getCstRelationship();
		if (cstRelationship != null) {
			refDiffRefType = cstRelationship.getType().toString();
			n1Location = cstRelationship.getNodeBefore().getLocation().format();
			n2Location = cstRelationship.getNodeAfter().getLocation().format();
		}
		System.out.printf("\t%s\t%s\t%s\t%s\t%s\t%s", refDiffRefType, n1Location, n2Location, label, findOrigin(label, evaluationDetails, cause), evaluationDetails != null ? evaluationDetails.format() : "");
	}
	
	private static String findOrigin(String label, EvaluationDetails evaluationDetails, String cause) {
		if (evaluationDetails != null && evaluationDetails.evaluators != null) {
			return evaluationDetails.evaluators;
		} else if ("FP".equals(label)) {
			return cause;
		}
		return "Oracle";
	}
}
