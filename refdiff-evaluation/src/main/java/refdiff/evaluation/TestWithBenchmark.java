package refdiff.evaluation;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import refdiff.core.RefDiff;
import refdiff.core.api.RefactoringType;
import refdiff.core.rm2.analysis.RefDiffConfigImpl;
import refdiff.core.rm2.analysis.codesimilarity.CodeSimilarityStrategy;
import refdiff.core.rm2.model.RelationshipType;
import refdiff.evaluation.benchmark.AbstractDataset;
import refdiff.evaluation.utils.ResultComparator;
import refdiff.evaluation.utils.ResultComparator.CompareResult;

public class TestWithBenchmark {

	private AbstractDataset oracle;

	public static void main(String[] args) {
		new TestWithBenchmark(new BenchmarkDataset()).calibrate();
	}

	public TestWithBenchmark(AbstractDataset oracle) {
		this.oracle = oracle;
	}

	public void calibrate() {
    	RefDiffConfigImpl config = new RefDiffConfigImpl();
        config.setThreshold(RelationshipType.MOVE_TYPE, 0.5);
        config.setThreshold(RelationshipType.RENAME_TYPE, 0.5);
        config.setThreshold(RelationshipType.EXTRACT_SUPERTYPE, 0.5);
        config.setThreshold(RelationshipType.MOVE_METHOD, 0.5);
        config.setThreshold(RelationshipType.RENAME_METHOD, 0.5);
        config.setThreshold(RelationshipType.PULL_UP_METHOD, 0.5);
        config.setThreshold(RelationshipType.PUSH_DOWN_METHOD, 0.5);
        config.setThreshold(RelationshipType.EXTRACT_METHOD, 0.5);
        config.setThreshold(RelationshipType.INLINE_METHOD, 0.5);
        config.setThreshold(RelationshipType.MOVE_FIELD, 0.5);
        config.setThreshold(RelationshipType.PULL_UP_FIELD, 0.5);
        config.setThreshold(RelationshipType.PUSH_DOWN_FIELD, 0.5);

        config = calibrate(config, RelationshipType.MOVE_TYPE, RefactoringType.MOVE_CLASS);
//        config = calibrate(config, RelationshipType.RENAME_TYPE, RefactoringType.RENAME_CLASS);
//        config = calibrate(config, RelationshipType.EXTRACT_SUPERTYPE, RefactoringType.EXTRACT_SUPERCLASS, RefactoringType.EXTRACT_INTERFACE);
//        
//        config = calibrate(config, RelationshipType.MOVE_METHOD, RefactoringType.MOVE_OPERATION);
//        config = calibrate(config, RelationshipType.RENAME_METHOD, RefactoringType.RENAME_METHOD);
//        config = calibrate(config, RelationshipType.PULL_UP_METHOD, RefactoringType.PULL_UP_OPERATION);
//        config = calibrate(config, RelationshipType.PUSH_DOWN_METHOD, RefactoringType.PUSH_DOWN_OPERATION);
//        config = calibrate(config, RelationshipType.EXTRACT_METHOD, RefactoringType.EXTRACT_OPERATION);
//        config = calibrate(config, RelationshipType.INLINE_METHOD, RefactoringType.INLINE_OPERATION);
//        
//        config = calibrate(config, RelationshipType.PULL_UP_FIELD, RefactoringType.PULL_UP_ATTRIBUTE);
//        config = calibrate(config, RelationshipType.PUSH_DOWN_FIELD, RefactoringType.PUSH_DOWN_ATTRIBUTE);
//        config = calibrate(config, RelationshipType.MOVE_FIELD, RefactoringType.MOVE_ATTRIBUTE);
        
        config.setId("refdiff-default");
//
        System.out.println(config.toString());
        //printTable3();
    }
    
    private RefDiffConfigImpl calibrate(RefDiffConfigImpl baseConfig, RelationshipType relType, RefactoringType refType, RefactoringType ... refTypes) {
        ResultComparator rc1 = new ResultComparator();
        rc1.expect(oracle.getExpected());
        EnumSet<RefactoringType> refTypeSet = EnumSet.of(refType, refTypes);
        
        List<RefDiffConfigImpl> configurations = generateRmConfigurations(baseConfig, relType);
        double maxF1 = 0.0;
        RefDiffConfigImpl maxConfig = configurations.get(0);
        
        for (RefDiffConfigImpl config : configurations) {
            rc1.compareWith(config.getId(), ResultComparator.collectRmResult(new RefDiff(config), oracle.getExpected()));
            CompareResult result = rc1.getCompareResult(config.getId(), refTypeSet);
            double f1 = result.getF1();
            if (f1 >= maxF1) {
                maxF1 = f1;
                maxConfig = config;
            }
        }
        rc1.printSummary(System.out, refTypeSet);
        rc1.printDetails(System.out, refTypeSet);
        return maxConfig;
    }

    public List<RefDiffConfigImpl> generateRmConfigurations(RefDiffConfigImpl baseConfig, RelationshipType relationshipType) {
        List<RefDiffConfigImpl> list = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            double t = 0.1 * i;
            RefDiffConfigImpl config = baseConfig.clone();
            config.setId("refdiff-" + relationshipType + "-" + i);
            config.setThreshold(relationshipType, t);
            config.setCodeSimilarityStrategy(CodeSimilarityStrategy.TFIDF);
            list.add(config);
        }
        return list;
    }

    private void printTable3() {
        ResultComparator rc1 = new ResultComparator();
        rc1.expect(oracle.getExpected());
        RefDiffConfigImpl config = new RefDiffConfigImpl();
        rc1.compareWith(config.getId(), ResultComparator.collectRmResult(new RefDiff(config), oracle.getExpected()));
        CompareResult r = rc1.getCompareResult(config.getId(), EnumSet.allOf(RefactoringType.class));
        
        System.out.println("\\begin{tabular}{@{}lrrrrrcc@{}}");
        System.out.println("\\toprule");
        System.out.println("Ref. Type & \\# & Threshold & TP & FP & FN & Precision & Recall\\\\");
        System.out.println("\\midrule");
        
        table3Row(r, "Rename Type", 0.4, RefactoringType.RENAME_CLASS);
        table3Row(r, "Move Type", 0.9, RefactoringType.MOVE_CLASS);
        table3Row(r, "Extract Superclass", 0.8, RefactoringType.EXTRACT_SUPERCLASS, RefactoringType.EXTRACT_INTERFACE);
        table3Row(r, "Rename Method", 0.3, RefactoringType.RENAME_METHOD);
        table3Row(r, "Pull Up Method", 0.4, RefactoringType.PULL_UP_OPERATION);
        table3Row(r, "Push Down Method", 0.6, RefactoringType.PUSH_DOWN_OPERATION);
        table3Row(r, "Move Method", 0.4, RefactoringType.MOVE_OPERATION);
        table3Row(r, "Extract Method", 0.1, RefactoringType.EXTRACT_OPERATION);
        table3Row(r, "Inline Method", 0.3, RefactoringType.INLINE_OPERATION);
        table3Row(r, "Pull Up Field", 0.5, RefactoringType.PULL_UP_ATTRIBUTE);
        table3Row(r, "Push Down Field", 0.3, RefactoringType.PUSH_DOWN_ATTRIBUTE);
        table3Row(r, "Move Field", 0.5, RefactoringType.MOVE_ATTRIBUTE);
        System.out.println("\\addlinespace");
        System.out.println(String.format(Locale.US, "Total & %d & & %d & %d & %d & \\xbar{%.3f} & \\xbar{%.3f}\\\\", r.getTPCount() + r.getFNCount(), r.getTPCount(), r.getFPCount(), r.getFNCount(), r.getPrecision(), r.getRecall()));
        System.out.println("\\bottomrule");
        System.out.println("\\end{tabular}");
    }

    private void table3Row(CompareResult r, String name, double threshold, RefactoringType ... refactoringTypes) {
        System.out.print(String.format(Locale.US, "%s", name));
        CompareResult fr = r.filterBy(refactoringTypes);
        int expected = fr.getTPCount() + fr.getFNCount();
        System.out.println(String.format(Locale.US, " & %d & %.1f & %d & %d & %d & \\xbar{%.3f} & \\xbar{%.3f}\\\\", expected, threshold, fr.getTPCount(), fr.getFPCount(), fr.getFNCount(), fr.getPrecision(), fr.getRecall()));
    }
    
}
