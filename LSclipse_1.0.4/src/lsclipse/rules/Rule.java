package lsclipse.rules;

import lsclipse.RefactoringQuery;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public interface Rule {
	public abstract String getName();

	public abstract String getRefactoringString();

	public abstract RefactoringQuery getRefactoringQuery();

	// Make sure the important facts are separated by "\",\"" without a space.
	public abstract String checkAdherence(ResultSet rs) throws TyrubaException;

}