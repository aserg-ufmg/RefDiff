/* IntroduceNullObject.java
 * 
 * This class is used to check adherence to the
 * introduce null object logical refactoring rule.
 * 
 * author:   Kyle Prete
 * created:  8/7/2010
 */
package lsclipse.rules;

import lsclipse.RefactoringQuery;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class IntroduceNullObject implements Rule {
	private static final String NULLT_FULL_NAME = "?nulltFullName";
	private static final String SERVERT_FULL_NAME = "?servertFullName";
	private static final String SERVERM_FULL_NAME = "?servermFullName";
	private static final String M_FULL_NAME = "?mFullName";
	private static final String NULL_COND = "?nullCond";
	private String name_;

	public IntroduceNullObject() {
		name_ = "introduce_null_object";
	}

	@Override
	public String getName() {
		return name_;
	}

	@Override
	public String getRefactoringString() {
		return getName() + "(" + NULLT_FULL_NAME + "," + SERVERT_FULL_NAME
				+ ")";
	}

	@Override
	public RefactoringQuery getRefactoringQuery() {
		return new RefactoringQuery(getName(), getQueryString());
	}

	private String getQueryString() {
		return "deleted_conditional(" + NULL_COND + ", ?, ?, " + M_FULL_NAME
				+ ")," + "NOT(added_conditional(" + NULL_COND + ", ?, ?, "
				+ M_FULL_NAME + "))," + "before_calls(" + M_FULL_NAME + ", "
				+ SERVERM_FULL_NAME + ")," + "after_calls(" + M_FULL_NAME
				+ ", " + SERVERM_FULL_NAME + ")," + "after_method("
				+ SERVERM_FULL_NAME + ", ?, " + SERVERT_FULL_NAME + "),"
				+ "added_type(" + NULLT_FULL_NAME + ", ?, ?),"
				+ "added_subtype(" + SERVERT_FULL_NAME + ", " + NULLT_FULL_NAME
				+ ")";
	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		if (!rs.getString(NULL_COND).contains("==null"))
			return null;
		return getName() + "(\"" + rs.getString(NULLT_FULL_NAME) + "\",\""
				+ rs.getString(SERVERT_FULL_NAME) + "\")";
	}

}
