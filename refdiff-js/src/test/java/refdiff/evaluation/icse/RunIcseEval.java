package refdiff.evaluation.icse;

import java.util.EnumSet;
import java.util.List;

import refdiff.evaluation.EvaluationUtils;
import refdiff.evaluation.RefactoringSet;
import refdiff.evaluation.RefactoringType;
import refdiff.evaluation.ResultComparator;

public class RunIcseEval {
	
	private EnumSet<RefactoringType> refactoringTypes = EnumSet.complementOf(EnumSet.of(RefactoringType.PULL_UP_ATTRIBUTE, RefactoringType.PUSH_DOWN_ATTRIBUTE, RefactoringType.MOVE_ATTRIBUTE));
	private EvaluationUtils evalUtils;
	
	public RunIcseEval(String tempFolder) {
		evalUtils = new EvaluationUtils(tempFolder);
	}

	public static void main(String[] args) throws Exception {
		new RunIcseEval(args.length > 0 ? args[0] : "C:/tmp/").run();
	}
	
	public void run() throws Exception {
		IcseDataset data = new IcseDataset();
		List<RefactoringSet> expected = data.getExpected();
		
		ResultComparator rc = new ResultComparator();
		rc.dontExpect(data.getNotExpected());
		
		for (RefactoringSet rs : expected) {
			String project = rs.getProject();
			String commit = rs.getRevision();
			try {
				evalUtils.prepareSourceCode(project, commit);
			} catch (RuntimeException e) {
				System.out.println(String.format("Skipped %s %s", project, commit));
				System.err.println(e.getMessage());
				continue;
			}
			rc.expect(rs);
			rc.compareWith("RefDiff", evalUtils.runRefDiff(project, commit));
		}
		
		rc.printDetails(System.out, refactoringTypes);
		System.out.println();
		rc.printSummary(System.out, refactoringTypes);
	}
	
}
