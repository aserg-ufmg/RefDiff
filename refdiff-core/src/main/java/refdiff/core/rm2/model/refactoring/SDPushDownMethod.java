package refdiff.core.rm2.model.refactoring;

import refdiff.core.rm2.model.SDMethod;

import refdiff.core.api.RefactoringType;

public class SDPushDownMethod extends SDMoveMethod {

    public SDPushDownMethod(SDMethod methodBefore, SDMethod methodAfter) {
        super(RefactoringType.PUSH_DOWN_OPERATION, methodBefore, methodAfter);
    }

}
