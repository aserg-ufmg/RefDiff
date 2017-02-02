/* RenameMethod.java
 * 
 * This class is used to test a tyRuBa result set for more liberal adherence 
 * to the "rename method" logical refactoring rule.
 * 
 * author:   Kyle Prete
 * created:  8/20/2010
 */

package lsclipse.rules;

import lsclipse.RefactoringQuery;
import lsclipse.utils.CodeCompare;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class RenameMethod implements Rule {
	private String name_;

	private static final String T_FULL_NAME = "?tFullName";
	private static final String M1_SHORT_NAME = "?m1ShortName";
	private static final String M2_SHORT_NAME = "?m2ShortName";
	public static final String M2_BODY = "?m2Body";
	public static final String M1_BODY = "?m1Body";
	public static final String M1_FULL_NAME = "?m1FullName";
	public static final String M2_FULL_NAME = "?m2FullName";

	public RenameMethod() {
		super();
		name_ = "rename_method";
	}

	@Override
	public String getRefactoringString() {
		return getName() + "(" + M1_FULL_NAME + "," + M2_FULL_NAME + ","
				+ T_FULL_NAME + ")";
	}

	@Override
	public RefactoringQuery getRefactoringQuery() {
		return new RefactoringQuery(getName(), getQueryString());
	}

	private static String getQueryString() {
		return "deleted_method(" + M1_FULL_NAME + ", " + M1_SHORT_NAME + ", "
				+ T_FULL_NAME + "), added_method(" + M2_FULL_NAME + ", "
				+ M2_SHORT_NAME + ", " + T_FULL_NAME + "), NOT(equals("
				+ M1_SHORT_NAME + ", " + M2_SHORT_NAME
				+ ")), added_methodbody(" + M2_FULL_NAME + "," + M2_BODY
				+ "), deleted_methodbody(" + M1_FULL_NAME + "," + M1_BODY + ")";
	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		String m1Body_str = rs.getString(M1_BODY);
		String m2Body_str = rs.getString(M2_BODY);

		if (m2Body_str.length() > 1
				&& CodeCompare.compare(m1Body_str, m2Body_str)) {

			String writeTo = getName() + "(\"" + rs.getString(M1_FULL_NAME)
					+ "\",\"" + rs.getString(M2_FULL_NAME) + "\",\""
					+ rs.getString(T_FULL_NAME) + "\")";

			return writeTo;
		}
		return null;

	}

	@Override
	public String getName() {
		return name_;
	}
}
