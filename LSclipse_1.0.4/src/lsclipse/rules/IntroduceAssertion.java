/* IntroduceAssertion.java
 * 
 * This class is used to check adherence to the
 * introduce assertion logical refactoring rule.
 * 
 * author:   Kyle Prete
 * created:  8/7/2010
 */
package lsclipse.rules;

import lsclipse.RefactoringQuery;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class IntroduceAssertion implements Rule {
	private static final String NEWM_BODY = "?newmBody";
	private static final String OLDM_BODY = "?oldmBody";
	private static final String M_FULL_NAME = "?mFullName";
	private String name_;

	public IntroduceAssertion() {
		name_ = "introduce_assertion";
	}

	@Override
	public String getName() {
		return name_;
	}

	@Override
	public String getRefactoringString() {
		return getName() + "(" + M_FULL_NAME + ")";
	}

	@Override
	public RefactoringQuery getRefactoringQuery() {
		return new RefactoringQuery(getName(), getQueryString());
	}

	private String getQueryString() {
		return "deleted_methodbody(" + M_FULL_NAME + "," + OLDM_BODY
				+ "),added_methodbody(" + M_FULL_NAME + "," + NEWM_BODY + ")";
	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		String oldmBody = rs.getString(OLDM_BODY);
		String newmBody = rs.getString(NEWM_BODY);
		int oldNumAsserts = oldmBody.split("assert").length - 1;
		int newNumAsserts = newmBody.split("assert").length - 1;

		if (newNumAsserts > oldNumAsserts) {
			return getName() + "(\"" + rs.getString(M_FULL_NAME) + "\")";
		}

		return null;
	}

}
