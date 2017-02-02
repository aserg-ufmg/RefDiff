/* ReplaceNestedCondWithGuardClauses.java
 * 
 * This class is used to test a tyRuBa result set for more lenient adherence 
 * to the "replace nested conditional with guard clauses" logical refactoring rule.
 * 
 * author:   Napol Rachatasumrit
 * created:  8/4/2010
 */

package lsclipse.rules;

import lsclipse.RefactoringQuery;
import lsclipse.utils.CodeCompare;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class ReplaceNestedCondWithGuardClauses implements Rule {

	private String name_;

	public ReplaceNestedCondWithGuardClauses() {
		name_ = "replace_nested_cond_guard_clauses";
	}

	private String getQueryString() {
		return "deleted_conditional("
				+ "?old_cond1, ?old_ifPart1, ?old_elsePart1, ?mFullName), "
				+ "added_conditional("
				+ "?new_cond1, ?new_ifPart1, \"\", ?mFullName), "
				+ "added_conditional("
				+ "?new_cond2, ?new_ifPart2, \"\", ?mFullName)";
	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		String old_cond1 = rs.getString("?old_cond1");
		String new_cond1 = rs.getString("?new_cond1");
		String new_cond2 = rs.getString("?new_cond2");
		String old_ifPart1 = rs.getString("?old_ifPart1");
		String new_ifPart1 = rs.getString("?new_ifPart1");
		String new_ifPart2 = rs.getString("?new_ifPart2");
		String old_elsePart1 = rs.getString("?old_elsePart1");

		if (CodeCompare.compare(old_cond1, new_cond1)
				&& CodeCompare.compare(old_ifPart1, new_ifPart1)
				&& CodeCompare.compare(new_cond2, old_elsePart1)
				&& CodeCompare.compare(new_ifPart2, old_elsePart1)) {

			String writeTo = getName() + "(\"" + rs.getString("?mFullName")
					+ "\")";

			return writeTo;
		}
		return null;
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
		return getName() + "(?mFullName)";

	}

}
