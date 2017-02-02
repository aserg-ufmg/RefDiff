/* IntroduceExplainingVariable.java
 * 
 * This class is used to test a tyRuBa result set for adherence 
 * to the introduce explaining variable logical refactoring rule.
 * 
 * author:   Kyle Prete
 * created:  8/9/2010
 */
package lsclipse.rules;

import java.util.regex.Pattern;

import lsclipse.RefactoringQuery;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class IntroduceExplainingVariable implements Rule {
	private static final String NEWM_BODY = "?newmBody";
	private static final String M_BODY = "?mBody";
	private static final String EXPRESSION = "?expression";
	private static final String IDENTIFIER = "?identifier";
	private static final String M_FULL_NAME = "?mFullName";
	private String name_;

	public IntroduceExplainingVariable() {
		name_ = "introduce_explaining_variable";
	}

	@Override
	public String getName() {
		return name_;
	}

	@Override
	public String getRefactoringString() {
		return getName() + "(" + IDENTIFIER + "," + EXPRESSION + ","
				+ M_FULL_NAME + ")";
	}

	@Override
	public RefactoringQuery getRefactoringQuery() {
		return new RefactoringQuery(getName(), getQueryString());
	}

	private String getQueryString() {
		return "added_localvar(" + M_FULL_NAME + ",?," + IDENTIFIER + ","
				+ EXPRESSION + "),NOT(deleted_localvar(" + M_FULL_NAME + ",?,"
				+ IDENTIFIER + ",?)),NOT(deleted_localvar(" + M_FULL_NAME
				+ ",?,?," + EXPRESSION + ")),deleted_methodbody(" + M_FULL_NAME
				+ "," + M_BODY + "),added_methodbody(" + M_FULL_NAME + ","
				+ NEWM_BODY + ")";
	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		String expression = rs.getString(EXPRESSION);
		String mBody = rs.getString(M_BODY);
		String newmBody = rs.getString(NEWM_BODY);

		// Use of Pattern.LITERAL flag makes sure we treat this as a literal
		// instead of a regex.
		// Use of -1 in split() makes sure trailing empty strings are kept
		// Subtract 1 to reflect count
		int oldCount = Pattern.compile(expression, Pattern.LITERAL).split(
				mBody, -1).length - 1;
		int newCount = Pattern.compile(expression, Pattern.LITERAL).split(
				newmBody, -1).length - 1;

		if (newCount > oldCount)
			return null;
		
		// Make sure expression was in old version at least once.
		if (oldCount < 1)
			return null;

		return getName() + "(\"" + rs.getString(IDENTIFIER) + "\",\""
				+ expression + "\",\"" + rs.getString(M_FULL_NAME) + "\")";
	}

}
