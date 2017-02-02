/* ExtractMethod.java
 * 
 * This class is used to test a tyRuBa result set for more strict adherence 
 * to the "extract method" logical refactoring rule.
 * 
 * author:   Kyle Prete
 * created:  7/22/2010
 */

package lsclipse.rules;

import lsclipse.RefactoringQuery;
import lsclipse.utils.CodeCompare;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class ExtractMethod implements Rule {
	private String name_;

	private String new_method_body_;
	private String method_body_;
	private String method_full_name_;
	private String new_method_full_name_;
	private String type_full_name_;

	public ExtractMethod() {
		super();
		name_ = "extract_method";

		new_method_body_ = "?newmBody";
		method_body_ = "?mBody";
		method_full_name_ = "?mFullName";
		new_method_full_name_ = "?newmFullName";
		type_full_name_ = "?tFullName";
	}

	public ExtractMethod(String method_full_name, String new_method_full_name,
			String new_method_body, String type_full_name) {
		super();
		name_ = "extract_method";

		new_method_body_ = new_method_body;
		method_body_ = "?mBody";
		method_full_name_ = method_full_name;
		new_method_full_name_ = new_method_full_name;
		type_full_name_ = type_full_name;
	}

	@Override
	public String getRefactoringString() {
		return getName() + "(" + method_full_name_ + ","
				+ new_method_full_name_ + "," + new_method_body_ + ","
				+ type_full_name_ + ")";
	}

	@Override
	public RefactoringQuery getRefactoringQuery() {
		RefactoringQuery extract_method = new RefactoringQuery(getName(),
				getQueryString());
		return extract_method;
	}

	private String getQueryString() {
		return "added_method(" + new_method_full_name_ + ",?,"
				+ type_full_name_ + "), " + "after_method(" + method_full_name_
				+ ",?," + type_full_name_ + ")," + "after_calls("
				+ method_full_name_ + ", " + new_method_full_name_
				+ "), added_methodbody(" + new_method_full_name_ + ","
				+ new_method_body_ + "), deleted_methodbody("
				+ method_full_name_ + "," + method_body_ + ")," + "NOT(equals("
				+ new_method_full_name_ + "," + method_full_name_ + "))";
	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		String newmBody_str = rs.getString(new_method_body_);
		String mBody_str = rs.getString(method_body_);

		if (newmBody_str.length() > 1
				&& CodeCompare.compare(newmBody_str, mBody_str)
				&& CodeCompare.contrast(newmBody_str, mBody_str)) {

			String writeTo = getName() + "(" + "\""
					+ rs.getString(method_full_name_) + "\"" + "," + "\""
					+ rs.getString(new_method_full_name_) + "\"" + "," + "\""
					+ newmBody_str + "\"" + "," + "\""
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
