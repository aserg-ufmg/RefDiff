/* ConsolidateDuplicateConditionalFragment.java
 * 
 * This class is used to test a tyRuBa result set for adherence 
 * to the "consolidate_duplicate_cond_fragments" logical refactoring rule.
 * 
 * author:   Napol Rachatasumrit
 * created:  08/06/2010
 */

package lsclipse.rules;

import lsclipse.LCS;
import lsclipse.RefactoringQuery;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class ConsolidateDuplicateConditionalFragment implements Rule {

	private String name_;

	public ConsolidateDuplicateConditionalFragment() {
		name_ = "consolidate_duplicate_cond_fragments";
	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		String old_elsePart = rs.getString("?old_elsePart");
		String new_elsePart = rs.getString("?new_elsePart");
		String old_ifPart = rs.getString("?old_ifPart");
		String new_ifPart = rs.getString("?new_ifPart");
		String body = rs.getString("?mbody");
		
		if(old_elsePart.equals("")) return null;
		if (similar_fragments(old_elsePart, new_elsePart, body)
				&& similar_fragments(old_ifPart, new_ifPart, body)) {
			String writeTo = getName() + "(\"" + rs.getString("?mFullName")
					+ "\")";
			return writeTo;
		}
		return null;
	}

	private String getQueryString() {
		return "deleted_conditional("
				+ "?cond, ?old_ifPart, ?old_elsePart, ?mFullName), "
				+ "added_conditional("
				+ "?cond, ?new_ifPart, ?new_elsePart, ?mFullName), "
				+ "after_methodbody(" + "?mFullName, ?mbody)";
	}

	public boolean similar_fragments(String old, String news, String body) {
		String lcs = LCS.getLCS(old, news);
		
		int index = old.indexOf(lcs);
		
		if(index == -1) return false;

		String prefix = old.substring(0, index);
		String suffix = old.substring(index + lcs.length());

		if (body.contains(prefix) && body.contains(suffix))
			return true;
		else
			return false;
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
