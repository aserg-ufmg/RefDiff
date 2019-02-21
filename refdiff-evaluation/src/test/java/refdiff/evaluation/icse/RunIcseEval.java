package refdiff.evaluation.icse;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import refdiff.evaluation.EvaluationUtils;
import refdiff.evaluation.KeyPair;
import refdiff.evaluation.RefactoringSet;
import refdiff.evaluation.RefactoringType;
import refdiff.evaluation.ResultComparator;

public class RunIcseEval {
	
	public static EnumSet<RefactoringType> refactoringTypes = EnumSet.complementOf(EnumSet.of(RefactoringType.PULL_UP_ATTRIBUTE, RefactoringType.PUSH_DOWN_ATTRIBUTE, RefactoringType.MOVE_ATTRIBUTE));
	private EvaluationUtils evalUtils;
	
	public RunIcseEval(String tempFolder) {
		evalUtils = new EvaluationUtils(tempFolder);
	}

	public static void main(String[] args) throws Exception {
		new RunIcseEval(args.length > 0 ? args[0] : "D:/refdiff/").run();
	}
	
	public void run() throws Exception {
		IcseDataset data = new IcseDataset();
		List<RefactoringSet> expected = data.getExpected();
		
		ResultComparator rc = new ResultComparator();
		rc.dontExpect(data.getNotExpected());
		
		int count = 0;
		int errorCount = 0;
		for (int i = 0; i < expected.size(); i++) {
			RefactoringSet rs = expected.get(i);
			String project = rs.getProject();
			String commit = rs.getRevision();
			try {
				System.out.printf("%d/%d - ", i + 1, expected.size());
				evalUtils.prepareSourceCode2(project, commit);
			} catch (RuntimeException e) {
				errorCount++;
				System.err.println(String.format("Skipped %s %s", project, commit));
				System.err.println(e.getMessage());
				continue;
			}
			rc.expect(rs);
			Map<KeyPair, String> explanations = new HashMap<>();
			rc.compareWith("RefDiff", evalUtils.runRefDiff(project, commit, explanations), explanations);
			count++;
		}
		
		rc.printDetails(System.out, refactoringTypes, "RefDiff");
		System.out.println();
		rc.printSummary(System.out, refactoringTypes);
		
		System.out.println(String.format("%d commits processed, %d commits skipped.", count, errorCount));
	}
	
}
