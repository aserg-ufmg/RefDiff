/* ReplaceTypeCodeWithState.java
 * 
 * This class is used to test a tyRuBa result set for adherence 
 * to the "replace_type_code_with_state" logical refactoring rule.
 * 
 * author:   Napol Rachatasumrit
 * created:  8/8/2010
 */

package lsclipse.rules;

import lsclipse.RefactoringQuery;
import lsclipse.utils.CodeCompare;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class ReplaceTypeCodeWithState implements Rule {
	
	private String name_;
	
	public ReplaceTypeCodeWithState() {
		name_ = "replace_type_code_with_state";
	}
	
	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		
		String fShortName1 = rs.getString("?fShortName1");
		String fShortName2 = rs.getString("?fShortName2");
		String tCodeShortName1 = rs.getString("?tCodeShortName1");
		String tCodeShortName2 = rs.getString("?tCodeShortName2");
		String tCodeShortName = rs.getString("?tCodeShortName");
		String tShortName = rs.getString("?tShortName");
		
		fShortName1 = fShortName1.toLowerCase();
		fShortName2 = fShortName2.toLowerCase();
		tCodeShortName1 = tCodeShortName1.toLowerCase();
		tCodeShortName2 = tCodeShortName2.toLowerCase();
		tCodeShortName = tCodeShortName.toLowerCase();
		tShortName = tShortName.toLowerCase();

		if ((CodeCompare.compare(fShortName1, tCodeShortName1)
				&& CodeCompare.compare(fShortName2, tCodeShortName2))
				|| (CodeCompare.compare(fShortName1, tCodeShortName2)
 				&& CodeCompare.compare(fShortName2, tCodeShortName1))
				&& CodeCompare.compare(tCodeShortName, tShortName)) {
			String writeTo = getName() + "(\"" + rs.getString("?tFullName")
					+ "\",\"" + rs.getString("?tCodeFullName") + "\")";
			return writeTo;
			
			// fix to check that it's different.
		}
		return null;
	}
	
	private String getQueryString() {
		return "deleted_field(" + "?old_fFullName1, ?fShortName1, ?tFullName), "
				+ "deleted_field("
				+ "?old_fFullName2, ?fShortName2, ?tFullName), "
				+ "NOT(equals(?fShortName1, ?fShortName2)), "
				+ "before_fieldmodifier(" + "?old_fFullName1, \"static\"), "
				+ "before_fieldmodifier(" + "?old_fFullName2, \"static\"), "
				+ "before_fieldmodifier(" + "?old_fFullName1, \"final\"), "
				+ "before_fieldmodifier(" + "?old_fFullName2, \"final\"), "
				+ "modified_type(" + "?tFullName, ?tShortName, ?), "
				
				+ "added_field(" + "?new_fFullName1, ?fShortName1, ?tCodeFullName), "
				+ "added_field("
				+ "?new_fFullName2, ?fShortName2, ?tCodeFullName), "
				+ "after_fieldmodifier(" + "?new_fFullName1, \"static\"), "
				+ "after_fieldmodifier(" + "?new_fFullName2, \"static\"), "
				+ "after_fieldmodifier(" + "?new_fFullName1, \"final\"), "
				+ "after_fieldmodifier(" + "?new_fFullName2, \"final\"), "
				
				+ "deleted_fieldoftype(" + "?tCodefieldFullName, ?), "
				+ "added_fieldoftype(" + "?tCodefieldFullName, ?tCodeFullName), "
				
				+ "added_type(" + "?tCodeFullName, ?tCodeShortName, ?), "
				+ "added_type(" + "?tCodeFullName1, ?tCodeShortName1, ?), "
				+ "added_type(" + "?tCodeFullName2, ?tCodeShortName2, ?), "
				+ "added_subtype(" + "?tCodeFullName, ?tCodeFullName1), "
				+ "added_subtype(" + "?tCodeFullName, ?tCodeFullName2),"
				+ "NOT(equals(?tCodeShortName1, ?tCodeShortName2))";
		
				// could check after_accesses facts to make it less general
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
		return getName() + "(?tFullName, ?tCodeFullName)";
	}

}
