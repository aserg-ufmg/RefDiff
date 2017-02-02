/* SeparateQueryFromModifier.java
 * 
 * This class is used to check adherence to the separate
 * query from modifier logical refactoring rule.
 * 
 * author:   Kyle Prete
 * created:  8/4/2010
 */
package lsclipse.rules;

import lsclipse.RefactoringQuery;
import lsclipse.utils.CodeCompare;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class SeparateQueryFromModifier implements Rule {
	private static final String F2_FULL_NAME = "?f2FullName";
	private static final String F1_FULL_NAME = "?f1FullName";
	private static final String T_FULL_NAME = "?tFullName";
	private static final String M2_SHORT_NAME = "?m2ShortName";
	private static final String M1_SHORT_NAME = "?m1ShortName";
	private static final String OLDM_SHORT_NAME = "?oldmShortName";
	private static final String M2_FULL_NAME = "?m2FullName";
	private static final String M1_FULL_NAME = "?m1FullName";
	private static final String OLDM_FULL_NAME = "?oldmFullName";
	private String name_;

	public SeparateQueryFromModifier() {
		name_ = "separate_query_from_modifier";
	}

	@Override
	public String getName() {
		return name_;
	}

	@Override
	public String getRefactoringString() {
		return getName() + "(" + OLDM_FULL_NAME + "," + M1_FULL_NAME + ","
				+ M2_FULL_NAME + ")";
	}

	@Override
	public RefactoringQuery getRefactoringQuery() {
		RefactoringQuery sep_qm = new RefactoringQuery(getName(),
				getQueryString());
		return sep_qm;
	}

	private String getQueryString() {
		return "before_field(" + F1_FULL_NAME + ",?," + T_FULL_NAME + "),"
				+ "after_field(" + F1_FULL_NAME + ",?," + T_FULL_NAME + "),"
				+ "before_field(" + F2_FULL_NAME + ",?," + T_FULL_NAME + "),"
				+ "after_field(" + F2_FULL_NAME + ",?," + T_FULL_NAME + "),"
				+ "deleted_method(" + OLDM_FULL_NAME + "," + OLDM_SHORT_NAME
				+ "," + T_FULL_NAME + ")," + "deleted_accesses(" + F1_FULL_NAME
				+ "," + OLDM_FULL_NAME + ")," + "deleted_accesses("
				+ F2_FULL_NAME + "," + OLDM_FULL_NAME + ")," + "added_method("
				+ M1_FULL_NAME + "," + M1_SHORT_NAME + "," + T_FULL_NAME + "),"
				+ "added_accesses(" + F1_FULL_NAME + "," + M1_FULL_NAME + "),"
				+ "added_method(" + M2_FULL_NAME + "," + M2_SHORT_NAME + ","
				+ T_FULL_NAME + ")," + "added_accesses(" + F2_FULL_NAME + ","
				+ M2_FULL_NAME + ")," + "NOT(equals(" + M1_FULL_NAME + ","
				+ M2_FULL_NAME + "))";

	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		String oldmShortName = rs.getString(OLDM_SHORT_NAME);
		String m1ShortName = rs.getString(M1_SHORT_NAME);
		String m2ShortName = rs.getString(M2_SHORT_NAME);

		if (!(CodeCompare.compare(oldmShortName, m1ShortName) && CodeCompare
				.compare(oldmShortName, m2ShortName)))
			return null;

		if (m1ShortName.compareTo(m2ShortName) < 0)
			return getName() + "(\"" + rs.getString(OLDM_FULL_NAME) + "\",\""
					+ rs.getString(M1_FULL_NAME) + "\",\""
					+ rs.getString(M2_FULL_NAME) + "\")";
		else
			return getName() + "(\"" + rs.getString(OLDM_FULL_NAME) + "\",\""
					+ rs.getString(M2_FULL_NAME) + "\",\""
					+ rs.getString(M1_FULL_NAME) + "\")";

	}

}
