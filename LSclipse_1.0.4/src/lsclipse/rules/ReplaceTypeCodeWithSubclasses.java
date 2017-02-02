/* ReplaceTypeCodeWithSubclasses.java
 * 
 * This class is used to test a tyRuBa result set for adherence 
 * to the "replace_type_code_with_subclasses" logical refactoring rule.
 * 
 * author:   Napol Rachatasumrit
 * created:  8/7/2010
 */

package lsclipse.rules;

import lsclipse.RefactoringQuery;
import lsclipse.utils.CodeCompare;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class ReplaceTypeCodeWithSubclasses implements Rule {

	private String name_;

	public ReplaceTypeCodeWithSubclasses() {
		name_ = "replace_type_code_with_subclasses";
	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		String fShortName1 = rs.getString("?fShortName1");
		String fShortName2 = rs.getString("?fShortName2");
		String tCodeShortName1 = rs.getString("?tCodeShortName1");
		String tCodeShortName2 = rs.getString("?tCodeShortName2");

		fShortName1 = fShortName1.toLowerCase();
		fShortName2 = fShortName2.toLowerCase();
		tCodeShortName1 = tCodeShortName1.toLowerCase();
		tCodeShortName2 = tCodeShortName2.toLowerCase();

		if ((CodeCompare.compare(fShortName1, tCodeShortName1) && CodeCompare
				.compare(fShortName2, tCodeShortName2))
				|| (CodeCompare.compare(fShortName1, tCodeShortName2) && CodeCompare
						.compare(fShortName2, tCodeShortName1))) {
			String writeTo = getName() + "(\"" + rs.getString("?tFullName")
					+ "\")";
			return writeTo;
		}
		return null;
	}

	private String getQueryString() {
		return "before_field(" + "?fFullName1, ?fShortName1, ?tFullName), "
				+ "before_field(" + "?fFullName2, ?fShortName2, ?tFullName), "
				+ "NOT(equals(?fFullName1, ?fFullName2)), "
				+ "before_fieldmodifier(" + "?fFullName1, \"static\"), "
				+ "before_fieldmodifier(" + "?fFullName2, \"static\"), "
				+ "before_fieldmodifier(" + "?fFullName1, \"final\"), "
				+ "before_fieldmodifier(" + "?fFullName2, \"final\"), "
				+ "deleted_field(" + "?tCodefFullName, ?, ?tFullName), "
				+ "added_type(" + "?tCodeFullName1, ?tCodeShortName1, ?), "
				+ "added_type(" + "?tCodeFullName2, ?tCodeShortName2, ?), "
				+ "NOT(equals(?tCodeFullName1, ?tCodeFullName2)),"
				+ "added_subtype(" + "?tFullName, ?tCodeFullName1), "
				+ "added_subtype(" + "?tFullName, ?tCodeFullName2)";
	}

	@Override
	public String getName() {
		return name_;
	}

	@Override
	public RefactoringQuery getRefactoringQuery() {
		RefactoringQuery repl = new RefactoringQuery(getName(),
				getQueryString());
		return repl;
	}

	@Override
	public String getRefactoringString() {
		return getName() + "(?tFullName)";
	}

}
