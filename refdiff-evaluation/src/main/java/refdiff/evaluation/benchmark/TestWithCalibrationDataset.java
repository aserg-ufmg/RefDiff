package refdiff.evaluation.benchmark;

import java.util.EnumSet;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import refdiff.core.RefDiff;
import refdiff.core.api.RefactoringType;
import refdiff.core.rm2.analysis.RefDiffConfigImpl;
import refdiff.core.rm2.model.RelationshipType;
import refdiff.evaluation.utils.RefactoringSet;
import refdiff.evaluation.utils.ResultComparator;

public class TestWithCalibrationDataset {

	public static void main(String[] args) {
	    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
	    
        RefDiffConfigImpl config = new RefDiffConfigImpl();
        config.setId("refdiff-init");
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
        
		CalibrationDataset dataset = new CalibrationDataset();
		ResultComparator rc = new ResultComparator();
		rc.expect(dataset.getExpected());
		rc.dontExpect(dataset.getNotExpected());
		
		RefDiff refdiff = new RefDiff(config);
		for (RefactoringSet commit : dataset.getExpected()) {
			rc.compareWith("refdiff", ResultComparator.collectRmResult(refdiff, commit.getProject(), commit.getRevision()));
		}
		EnumSet<RefactoringType> types = EnumSet.allOf(RefactoringType.class);
		rc.printSummary(System.out, types);
		rc.printDetails(System.out, types);
	}

}
