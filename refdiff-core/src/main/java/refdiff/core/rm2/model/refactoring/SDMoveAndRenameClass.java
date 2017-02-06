package refdiff.core.rm2.model.refactoring;

import refdiff.core.rm2.model.SDType;

import refdiff.core.api.RefactoringType;

public class SDMoveAndRenameClass extends SDMoveClass {

    public SDMoveAndRenameClass(SDType typeBefore, SDType typeAfter) {
        super(RefactoringType.MOVE_RENAME_CLASS, typeBefore, typeAfter);
    }

}
