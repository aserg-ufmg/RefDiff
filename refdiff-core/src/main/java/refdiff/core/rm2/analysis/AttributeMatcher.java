package refdiff.core.rm2.analysis;

import refdiff.core.rm2.model.SDAttribute;

public class AttributeMatcher extends EntityMatcher<SDAttribute> {

    public AttributeMatcher() {
        using(SimilarityIndex.CLIENT_CODE);
    }

}
