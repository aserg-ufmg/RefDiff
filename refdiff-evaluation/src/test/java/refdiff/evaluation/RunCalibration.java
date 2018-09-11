package refdiff.evaluation;

import java.util.EnumSet;
import java.util.List;

public class RunCalibration {
	
	private EnumSet<RefactoringType> refactoringTypes = EnumSet.complementOf(EnumSet.of(RefactoringType.PULL_UP_ATTRIBUTE, RefactoringType.PUSH_DOWN_ATTRIBUTE, RefactoringType.MOVE_ATTRIBUTE));
	private EvaluationUtils evalUtils = new EvaluationUtils("D:/tmp/");
	
	public static void main(String[] args) throws Exception {
		new RunCalibration().run();
	}
	
	public void run() throws Exception {
		CalibrationDataset cd = new CalibrationDataset();
		List<RefactoringSet> expected = cd.getExpected();
		
		ResultComparator rc = new ResultComparator();
		
		// int i = 0;
		for (RefactoringSet rs : expected) {
			String project = rs.getProject();
			String commit = rs.getRevision();
			rc.expect(rs);
			rc.compareWith("RefDiff", evalUtils.runRefDiff(project, commit));
			// if (i++ > 4) break;
		}
		
		rc.printSummary(System.out, refactoringTypes);
		rc.printDetails(System.out, refactoringTypes, "RefDiff");
	}
	
}
