package refdiff.core.rm2.analysis;

import refdiff.core.rm2.analysis.codesimilarity.CodeSimilarityStrategy;
import refdiff.core.rm2.model.RelationshipType;

public interface RefactoringDetectorConfig {

    String getId();

    double getThreshold(RelationshipType relationshipType);

    CodeSimilarityStrategy getCodeSimilarityStrategy();

}