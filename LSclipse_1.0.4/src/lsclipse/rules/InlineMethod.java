/* InlineMethod.java
 * 
 * This class is used to test a tyRuBa result set for more strict adherence 
 * to the "inline method" logical refactoring rule.
 * 
 * author:   Kyle Prete
 * created:  7/23/2010
 */

package lsclipse.rules;

import lsclipse.RefactoringQuery;
import lsclipse.utils.CodeCompare;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class InlineMethod implements Rule {
	private String name_;

	private String old_method_body_;
	private String method_body_;
	private String method_full_name_;
	private String old_method_full_name_;
	private String type_full_name_;
	private String old_method_short_name_;

	public InlineMethod() {
		super();
		name_ = "inline_method";

		old_method_body_ = "?oldmBody";
		method_body_ = "?mBody";
		method_full_name_ = "?mFullName";
		old_method_full_name_ = "?oldmFullName";
		type_full_name_ = "?tFullName";
		old_method_short_name_ = "?oldmShortName";
	}

	@Override
	public String getRefactoringString() {
		return getName() + "(" + method_full_name_ + ","
				+ old_method_full_name_ + "," + old_method_body_ + ","
				+ type_full_name_ + ")";
	}

	@Override
	public RefactoringQuery getRefactoringQuery() {
		RefactoringQuery inline_method = new RefactoringQuery(getName(),
				getQueryString());
		return inline_method;
	}

	private String getQueryString() {
		return "deleted_method(" + old_method_full_name_ + ", "
				+ old_method_short_name_ + ", " + type_full_name_ + "), "
				+ "before_method(" + method_full_name_ + ", ?, "
				+ type_full_name_ + "), " + "before_calls(" + method_full_name_
				+ ", " + old_method_full_name_ + "), added_methodbody("
				+ method_full_name_ + "," + method_body_
				+ "), deleted_methodbody(" + old_method_full_name_ + ","
				+ old_method_body_ + ")," + "NOT(equals(" + method_full_name_
				+ "," + old_method_full_name_ + "))";
	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		String oldmBody_str = rs.getString(old_method_body_);
		String mBody_str = rs.getString(method_body_);

		if (oldmBody_str.length() > 1
				&& CodeCompare.compare(oldmBody_str, mBody_str)
				&& CodeCompare.contrast(oldmBody_str, mBody_str)) {

			String writeTo = getName() + "(" + "\""
					+ rs.getString(method_full_name_) + "\"" + "," + "\""
					+ rs.getString(old_method_full_name_) + "\"" + "," + "\""
					+ oldmBody_str + "\"" + "," + "\""
					+ rs.getString(type_full_name_) + "\"" + ")";

			return writeTo;
		}
		return null;

	}

	@Override
	public String getName() {
		return name_;
	}
}
