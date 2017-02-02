/* ConsolidateConditionalExpression.java
 * 
 * This class is used to test a tyRuBa result set for more lenient adherence 
 * to the "consolidate conditional expression" logical refactoring rule.
 * 
 * author:   Napol Rachatasumrit
 * created:  8/3/2010
 */

package lsclipse.rules;

import lsclipse.RefactoringQuery;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class ConsolidateConditionalExpression implements Rule {

	private String name_;

	public ConsolidateConditionalExpression() {
		name_ = "consolidate_cond_expression";
	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		String old_cond1 = rs.getString("?old_cond1");
		String old_cond2 = rs.getString("?old_cond2");
		String new_cond = rs.getString("?new_cond");
		String extMthdBody = "";
		boolean foundExtract = true;
		try {
			extMthdBody = rs.getString("?extMthdBody");
		} catch (NullPointerException e) {
			// Did not find extract method.
			foundExtract = false;
		}
		
		if (foundExtract){
			if(!extMthdBody.contains(old_cond1) 
					|| !extMthdBody.contains(old_cond2)) return null;
		}else{ 
			if (!new_cond.contains(old_cond1)
					|| !new_cond.contains(old_cond2)) return null;  
		}
			String writeTo = getName() + "(\"" + rs.getString("?mFullName")
					+ "\")";
			
			return writeTo;
	}

	private String getQueryString() {
		return "(deleted_conditional("
				+ "?old_cond1, ?ifPart, ?elsePart, ?mFullName), "
				+ "deleted_conditional("
				+ "?old_cond2, ?ifPart, ?elsePart, ?mFullName), "
				+ "added_conditional("
				+ "?new_cond, ?ifPart, ?elsePart, ?mFullName), "
				+ "extract_method("
				+ "?mFullName, ?, ?extMthdBody, ?)); "
				+ "(deleted_conditional("
				+ "?old_cond1, ?ifPart, ?elsePart, ?mFullName), "
				+ "deleted_conditional("
				+ "?old_cond2, ?ifPart, ?elsePart, ?mFullName), "
				+ "added_conditional("
				+ "?new_cond, ?ifPart, ?elsePart, ?mFullName))";
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
