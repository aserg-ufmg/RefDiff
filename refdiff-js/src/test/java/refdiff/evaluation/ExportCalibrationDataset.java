package refdiff.evaluation;

import java.util.List;

public class ExportCalibrationDataset {
	
	public static void main(String[] args) {
		CalibrationDataset cd = new CalibrationDataset();
		List<RefactoringSet> expected = cd.getExpected();
		
		ResultComparator rc = new ResultComparator();
		
		for (RefactoringSet rs : expected) {
			String project = rs.getProject();
			String commit = rs.getRevision();
			//rc.compareWith("refdiff", TODO);
		}
		
	}
	
}
