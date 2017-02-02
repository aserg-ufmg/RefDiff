/* ReplaceArrayWithObject.java
 * 
 * This class is used to test a tyRuBa result set for adherence 
 * to the "replace array with object" logical refactoring rule.
 * 
 * author:   Kyle Prete and Napol Rachatasumrit
 * created:  8/3/2010
 */
package lsclipse.rules;

import lsclipse.RefactoringQuery;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class ReplaceArrayWithObject implements Rule {
	private static final String NEWT_FULL_NAME = "?newtFullName";
	private static final String F_FULL_NAME = "?fFullName";
	private static final String OLDT_FULL_NAME = "?oldtFullName";
	private String name_;

	public ReplaceArrayWithObject() {
		name_ = "replace_array_with_object";
	}

	@Override
	public String getName() {
		return name_;
	}

	@Override
	public String getRefactoringString() {
		return getName() + "(" + F_FULL_NAME + "," + NEWT_FULL_NAME + ")";
	}

	@Override
	public RefactoringQuery getRefactoringQuery() {
		RefactoringQuery repl = new RefactoringQuery(getName(),
				getQueryString());
		return repl;
	}

	private String getQueryString() {
		return "deleted_fieldoftype(" + F_FULL_NAME + ", " + OLDT_FULL_NAME
				+ ")," + "added_fieldoftype(" + F_FULL_NAME + ", "
				+ NEWT_FULL_NAME + ")," + "added_type(" + NEWT_FULL_NAME
				+ ", ?, ?)";
	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		String oldType = rs.getString(OLDT_FULL_NAME);
		if (oldType.endsWith("[]")) {
			String writeTo = getName() + "(\"" + rs.getString(F_FULL_NAME)
					+ "\",\"" + rs.getString(NEWT_FULL_NAME) + "\")";

			return writeTo;
		}
		return null;
	}

}
