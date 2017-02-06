package refdiff.core.rm2.model.refactoring;

import refdiff.core.rm2.model.SDEntity;

import refdiff.core.api.Refactoring;
import refdiff.core.api.RefactoringType;

public class SDRefactoring implements Refactoring {

	protected RefactoringType type;
	protected SDEntity mainEntity;
	protected SDEntity entityBefore;
	protected SDEntity entityAfter;

	public SDRefactoring(RefactoringType type, SDEntity mainEntity, SDEntity entityBefore, SDEntity entityAfter) {
		this.type = type;
		this.mainEntity = mainEntity;
		this.entityBefore = entityBefore;
		this.entityAfter = entityAfter;
	}

	@Override
	public RefactoringType getRefactoringType() {
		return type;
	}

	@Override
	public String getName() {
		return type.getDisplayName();
	}
	
	public SDEntity getEntityBefore() {
    return entityBefore;
  }

  public SDEntity getEntityAfter() {
    return entityAfter;
  }

  @Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getName());
		sb.append(' ');
		sb.append(this.mainEntity);
//		if (this.details != null && !this.details.isEmpty()) {
//			sb.append(' ');
//			sb.append(this.details);
//		}
		return sb.toString();
	}
}
