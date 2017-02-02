/* MoveMethod.java
 * 
 * This class is used to test a tyRuBa result set for more liberal adherence 
 * to the "move method" logical refactoring rule.
 * 
 * author:   Kyle Prete
 * created:  8/20/2010
 */

package lsclipse.rules;

import lsclipse.RefactoringQuery;
import lsclipse.utils.CodeCompare;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class MoveMethod implements Rule {
	private String name_;

	private static final String PACKAGE = "?package";
	private static final String T2_FULL_NAME = "?t2FullName";
	private static final String T1_FULL_NAME = "?t1FullName";
	private static final String M_SHORT_NAME = "?mShortName";
	public static final String M2_BODY = "?m2Body";
	public static final String M1_BODY = "?m1Body";
	public static final String M1_FULL_NAME = "?m1FullName";
	public static final String M2_FULL_NAME = "?m2FullName";

	public MoveMethod() {
		super();
		name_ = "move_method";
	}

	@Override
	public String getRefactoringString() {
		return getName() + "(" + M_SHORT_NAME + "," + T1_FULL_NAME + ","
				+ T2_FULL_NAME + ")";
	}

	@Override
	public RefactoringQuery getRefactoringQuery() {
		return new RefactoringQuery(getName(), getQueryString());
	}

	private static String getQueryString() {
		return "deleted_method(" + M1_FULL_NAME + ", " + M_SHORT_NAME + ", "
				+ T1_FULL_NAME + "), added_method(" + M2_FULL_NAME + ", "
				+ M_SHORT_NAME + ", " + T2_FULL_NAME + "), before_type("
				+ T1_FULL_NAME + ", ?, " + PACKAGE + "), after_type("
				+ T2_FULL_NAME + ", ?, " + PACKAGE + "), NOT(equals("
				+ T1_FULL_NAME + ", " + T2_FULL_NAME + ")), added_methodbody("
				+ M2_FULL_NAME + "," + M2_BODY + "), deleted_methodbody("
				+ M1_FULL_NAME + "," + M1_BODY + ")," + "NOT(equals("
				+ M2_FULL_NAME + "," + M1_FULL_NAME + "))";
	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		String mShortName = rs.getString(M_SHORT_NAME);
		if (mShortName.equals("<init>()"))
			return null;

		String newmBody_str = rs.getString(M2_BODY);
		String mBody_str = rs.getString(M1_BODY);

		if (newmBody_str.length() > 1
				&& CodeCompare.compare(newmBody_str, mBody_str)) {

			String writeTo = getName() + "(\"" + rs.getString(M_SHORT_NAME)
					+ "\",\"" + rs.getString(T1_FULL_NAME) + "\",\""
					+ rs.getString(T2_FULL_NAME) + "\")";

			return writeTo;
		}
		return null;

	}

	@Override
	public String getName() {
		return name_;
	}
}
