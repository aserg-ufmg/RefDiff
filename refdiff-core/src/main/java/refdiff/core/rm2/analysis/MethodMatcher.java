package refdiff.core.rm2.analysis;

import refdiff.core.rm2.model.SDMethod;

public class MethodMatcher extends EntityMatcher<SDMethod> {

    public MethodMatcher() {
        using(SimilarityIndex.SOURCE_CODE);
    }
 
}
