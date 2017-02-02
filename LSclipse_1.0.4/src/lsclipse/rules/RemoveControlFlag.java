/* RemoveControlFlag.java
 * 
 * This class is used to test a tyRuBa result set for adherence 
 * to the remove control flag logical refactoring rule.
 * 
 * author:   Kyle Prete
 * created:  8/9/2010
 */
package lsclipse.rules;

import lsclipse.RefactoringQuery;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class RemoveControlFlag implements Rule {
	private static final String CONDITION = "?condition";
	private static final String EXPRESSION = "?expression";
	private static final String IDENTIFIER = "?identifier";
	private static final String M_FULL_NAME = "?mFullName";
	private String name_;

	public RemoveControlFlag() {
		name_ = "remove_control_flag";
	}

	@Override
	public String getName() {
		return name_;
	}

	@Override
	public String getRefactoringString() {
		return getName() + "(" + IDENTIFIER + "," + M_FULL_NAME + ")";
	}

	@Override
	public RefactoringQuery getRefactoringQuery() {
		return new RefactoringQuery(getName(), getQueryString());
	}

	private String getQueryString() {
		return "deleted_localvar(" + M_FULL_NAME + ",\"boolean\"," + IDENTIFIER
				+ "," + EXPRESSION + "),deleted_conditional(" + CONDITION
				+ ",?,?," + M_FULL_NAME + "),NOT(added_conditional("
				+ CONDITION + ",?,?," + M_FULL_NAME + "))";
	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		String condition = rs.getString(CONDITION);
		String identifier = rs.getString(IDENTIFIER);

		if (!condition.contains(identifier))
			return null;

		return getName() + "(\"" + identifier + "\",\""
				+ rs.getString(M_FULL_NAME) + "\")";
	}

}
