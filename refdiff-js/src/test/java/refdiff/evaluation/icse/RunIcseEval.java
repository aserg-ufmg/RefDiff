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
		new RunIcseEval(args.length > 0 ? args[0] : "D:/tmp/").run();
	}
	
	public void run() throws Exception {
		IcseDataset data = new IcseDataset();
		List<RefactoringSet> expected = data.getExpected();
		
		ResultComparator rc = new ResultComparator();
		rc.dontExpect(data.getNotExpected());
		
		int i = 0;
		for (RefactoringSet rs : expected) {
			String project = rs.getProject();
			String commit = rs.getRevision();
			rc.expect(rs);
			evalUtils.prepareSourceCode(project, commit);
			//rc.compareWith("RefDiff", evalUtils.runRefDiff(project, commit));
			//if (++i > 0) break;
		}
		
		rc.printSummary(System.out, refactoringTypes);
		//rc.printDetails(System.out, refactoringTypes);
	}
	
}
