package refdiff.evaluation.benchmark;

import java.util.EnumSet;
import java.util.Locale;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import refdiff.core.RefDiff;
import refdiff.core.api.RefactoringType;
import refdiff.core.rm2.analysis.RefDiffConfigImpl;
import refdiff.core.rm2.model.RelationshipType;
import refdiff.evaluation.utils.RefactoringSet;
import refdiff.evaluation.utils.ResultComparator;
import refdiff.evaluation.utils.ResultComparator.CompareResult;

public class TestWithCalibrationDataset {

	public static void main(String[] args) {
	    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        
        RefDiffConfigImpl config = new RefDiffConfigImpl();
        config.setId("xfinal");
        config.setThreshold(RelationshipType.MOVE_TYPE, 0.1);
        config.setThreshold(RelationshipType.RENAME_TYPE, 0.4);
        config.setThreshold(RelationshipType.EXTRACT_SUPERTYPE, 0.3);
        config.setThreshold(RelationshipType.MOVE_METHOD, 0.4);
        config.setThreshold(RelationshipType.RENAME_METHOD, 0.3);
        config.setThreshold(RelationshipType.PULL_UP_METHOD, 0.4);
        config.setThreshold(RelationshipType.PUSH_DOWN_METHOD, 0.6);
        config.setThreshold(RelationshipType.EXTRACT_METHOD, 0.3);
        config.setThreshold(RelationshipType.INLINE_METHOD, 0.3);
        config.setThreshold(RelationshipType.MOVE_FIELD, 0.1);
        config.setThreshold(RelationshipType.PULL_UP_FIELD, 0.2);
        config.setThreshold(RelationshipType.PUSH_DOWN_FIELD, 0.2);
        
		CalibrationDataset dataset = new CalibrationDataset();
		ResultComparator rc = new ResultComparator();
		rc.expect(dataset.getExpected());
		rc.dontExpect(dataset.getNotExpected());
		
		RefDiff refdiff = new RefDiff(config);
		for (RefactoringSet commit : dataset.getExpected()) {
			rc.compareWith("refdiff", ResultComparator.collectRmResult(refdiff, commit.getProject(), commit.getRevision()));
		}
		EnumSet<RefactoringType> types = EnumSet.complementOf(EnumSet.of(RefactoringType.MOVE_RENAME_CLASS));
		rc.printSummary(System.out, types);
		rc.printDetails(System.out, types);
		
		CompareResult r = rc.getCompareResult("refdiff", types);
		printTable3(r);
	}

	private static void printTable3(CompareResult r) {
        System.out.println("\\begin{tabular}{@{}lrrrrrcc@{}}");
        System.out.println("\\toprule");
        System.out.println("Ref. Type & \\# & Threshold & TP & FP & FN & Precision & Recall\\\\");
        System.out.println("\\midrule");
        
        table3Row(r, "Rename Type", 0.4, RefactoringType.RENAME_CLASS);
        table3Row(r, "Move Type", 0.1, RefactoringType.MOVE_CLASS);
        table3Row(r, "Extract Supertype", 0.3, RefactoringType.EXTRACT_SUPERCLASS, RefactoringType.EXTRACT_INTERFACE);
        table3Row(r, "Rename Method", 0.3, RefactoringType.RENAME_METHOD);
        table3Row(r, "Pull Up Method", 0.4, RefactoringType.PULL_UP_OPERATION);
        table3Row(r, "Push Down Method", 0.6, RefactoringType.PUSH_DOWN_OPERATION);
        table3Row(r, "Move Method", 0.4, RefactoringType.MOVE_OPERATION);
        table3Row(r, "Extract Method", 0.3, RefactoringType.EXTRACT_OPERATION);
        table3Row(r, "Inline Method", 0.3, RefactoringType.INLINE_OPERATION);
        table3Row(r, "Pull Up Field", 0.2, RefactoringType.PULL_UP_ATTRIBUTE);
        table3Row(r, "Push Down Field", 0.2, RefactoringType.PUSH_DOWN_ATTRIBUTE);
        table3Row(r, "Move Field", 0.1, RefactoringType.MOVE_ATTRIBUTE);
        System.out.println("\\addlinespace");
        System.out.println(String.format(Locale.US, "Total & %d & & %d & %d & %d & \\xbar{%.3f} & \\xbar{%.3f}\\\\", r.getTPCount() + r.getFNCount(), r.getTPCount(), r.getFPCount(), r.getFNCount(), r.getPrecision(), r.getRecall()));
        System.out.println("\\bottomrule");
        System.out.println("\\end{tabular}");
    }

    private static void table3Row(CompareResult r, String name, double threshold, RefactoringType ... refactoringTypes) {
        System.out.print(String.format(Locale.US, "%s", name));
        CompareResult fr = r.filterBy(refactoringTypes);
        int expected = fr.getTPCount() + fr.getFNCount();
        System.out.println(String.format(Locale.US, " & %d & %.1f & %d & %d & %d & \\xbar{%.3f} & \\xbar{%.3f}\\\\", expected, threshold, fr.getTPCount(), fr.getFPCount(), fr.getFNCount(), fr.getPrecision(), fr.getRecall()));
    }
}
