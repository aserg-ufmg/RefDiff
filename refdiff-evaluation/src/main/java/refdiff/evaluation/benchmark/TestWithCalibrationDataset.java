package refdiff.evaluation.benchmark;

import java.util.EnumSet;

import refdiff.core.RefDiff;
import refdiff.core.api.RefactoringType;
import refdiff.evaluation.utils.RefactoringSet;
import refdiff.evaluation.utils.ResultComparator;

public class TestWithCalibrationDataset {

	public static void main(String[] args) {
		CalibrationDataset dataset = new CalibrationDataset();
		ResultComparator rc = new ResultComparator();
		rc.expect(dataset.getCommits());
		
		RefDiff refdiff = new RefDiff();
		for (RefactoringSet commit : dataset.getCommits()) {
			rc.compareWith("refdiff", ResultComparator.collectRmResult(refdiff, commit.getProject(), commit.getRevision()));
		}
		EnumSet<RefactoringType> types = EnumSet.complementOf(EnumSet.of(RefactoringType.RENAME_CLASS, RefactoringType.RENAME_METHOD));
		rc.printSummary(System.out, types);
		rc.printDetails(System.out, types);
	}

}
