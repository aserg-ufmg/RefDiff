package refdiff.core.rm2.model.refactoring;

import refdiff.core.api.RefactoringType;
import refdiff.core.rm2.model.SDType;

public class SDExtractSupertype extends SDRefactoring {

    private final SDType extractedType;
    private final SDType typeOriginAfter;
    
    public SDExtractSupertype(SDType extractedType, SDType typeOriginAfter) {
        super(extractedType.isInterface() ? RefactoringType.EXTRACT_INTERFACE : RefactoringType.EXTRACT_SUPERCLASS, extractedType, typeOriginAfter, extractedType);
        this.extractedType = extractedType;
        this.typeOriginAfter = typeOriginAfter;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getName());
        sb.append(' ');
        sb.append(this.extractedType.fullName());
        sb.append(" from class ");
        sb.append(typeOriginAfter.fullName());
        return sb.toString();
    }
}
