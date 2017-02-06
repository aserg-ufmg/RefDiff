package refdiff.core.rm2.analysis;

import refdiff.core.rm2.model.SDModel;
import refdiff.core.rm2.model.SDType;

public class TypeMatcher extends EntityMatcher<SDType> {

    public TypeMatcher() {
        using(SimilarityIndex.SOURCE_CODE);
        //using(SimilarityIndex.MEMBERS);
    }
    
    @Override
    public int getPriority(SDModel m, SDType entityBefore, SDType entityAfter) {
        return Math.max(entityBefore.nestingLevel(), entityAfter.nestingLevel());
    }
 
}
