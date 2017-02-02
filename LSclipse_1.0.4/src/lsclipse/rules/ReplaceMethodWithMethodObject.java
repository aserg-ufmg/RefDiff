/* ReplaceMethodWithMethodObject.java
 * 
 * This class is used to test a tyRuBa result set for more liberal adherence 
 * to the "replace method with method object" logical refactoring rule.
 * 
 * author:   Kyle Prete
 * created:  8/20/2010
 */

package lsclipse.rules;

import lsclipse.RefactoringQuery;
import lsclipse.utils.CodeCompare;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class ReplaceMethodWithMethodObject implements Rule {
	private static final String CALLINGT_FULL_NAME = "?callingtFullName";
	private static final String T_FULL_NAME = "?tFullName";
	public static final String NEWM_BODY = "?newmBody";
	public static final String M_BODY = "?mBody";
	public static final String M_FULL_NAME = "?mFullName";
	public static final String NEWM_FULL_NAME = "?newmFullName";

	private String name_;

	public ReplaceMethodWithMethodObject() {
		super();
		name_ = "replace_method_with_method_object";
	}

	@Override
	public String getRefactoringString() {
		return getName() + "(" + M_FULL_NAME + "," + T_FULL_NAME + ")";
	}

	@Override
	public RefactoringQuery getRefactoringQuery() {
		return new RefactoringQuery(getName(), getQueryString());
	}

	private static String getQueryString() {
		// Class named after old function?
		return "added_type(" + T_FULL_NAME + ", " + "?tShortName"
				+ ", ?), added_method(" + NEWM_FULL_NAME + ", ?, "
				+ T_FULL_NAME + ")," + "added_calls(" + M_FULL_NAME + ", "
				+ NEWM_FULL_NAME + ")," + "after_method(" + M_FULL_NAME
				+ ", ?, " + CALLINGT_FULL_NAME + "), added_methodbody("
				+ NEWM_FULL_NAME + "," + NEWM_BODY + "), deleted_methodbody("
				+ M_FULL_NAME + "," + M_BODY + ")";
		// Do not need to check before_method, as deleted_methodbody
		// would not be spawned.

	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		String newmBody_str = rs.getString(NEWM_BODY);
		String mBody_str = rs.getString(M_BODY);

		if (newmBody_str.length() > 1
				&& CodeCompare.compare(newmBody_str, mBody_str)) {

			String writeTo = getName() + "(\"" + rs.getString(M_FULL_NAME)
					+ "\",\"" + rs.getString(T_FULL_NAME) + "\")";

			return writeTo;
		}
		return null;

	}

	@Override
	public String getName() {
		return name_;
	}
}
