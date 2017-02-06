package refdiff.core.rm2.model.refactoring;

import refdiff.core.rm2.model.SDMethod;

import refdiff.core.api.RefactoringType;

public class SDPullUpMethod extends SDMoveMethod {

    public SDPullUpMethod(SDMethod methodBefore, SDMethod methodAfter) {
        super(RefactoringType.PULL_UP_OPERATION, methodBefore, methodAfter);
    }
}
