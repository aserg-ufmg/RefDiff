package refdiff.evaluation.rm;

import refdiff.core.api.Refactoring;
import refdiff.core.api.RefactoringType;

public class RefactoringAdapter implements Refactoring {

    private final org.refactoringminer.api.Refactoring refactoring;

    public RefactoringAdapter(org.refactoringminer.api.Refactoring r) {
        this.refactoring = r;
    }

    @Override
    public RefactoringType getRefactoringType() {
        return RefactoringType.valueOf(refactoring.getRefactoringType().toString());
    }

    @Override
    public String getName() {
        return refactoring.getName();
    }
    
    @Override
    public String toString() {
        return refactoring.toString();
    }

    public static RefactoringAdapter from(org.refactoringminer.api.Refactoring r) {
        return new RefactoringAdapter(r);
    }

    public org.refactoringminer.api.Refactoring getRefactoring() {
        return refactoring;
    }
    
}
