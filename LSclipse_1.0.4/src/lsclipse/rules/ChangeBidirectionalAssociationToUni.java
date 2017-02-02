/* ChangeBidirectionalAssociationToUni.java
 * 
 * This class is used to test a tyRuBa result set for adherence 
 * to the "change bidirectional association to unidirectional" 
 * logical refactoring rule.
 * 
 * author:   Kyle Prete
 * created:  8/4/2010
 */
package lsclipse.rules;

import lsclipse.RefactoringQuery;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class ChangeBidirectionalAssociationToUni implements Rule {
	private static final String T2P_FULL_NAME = "?t2pFullName";
	private static final String TP_FULL_NAME = "?tpFullName";
	private static final String T2_FULL_NAME = "?t2FullName";
	private static final String T_FULL_NAME = "?tFullName";
	private String name_;

	public ChangeBidirectionalAssociationToUni() {
		name_ = "change_bi_to_uni";
	}

	@Override
	public String getName() {
		return name_;
	}

	@Override
	public String getRefactoringString() {
		return getName() + "(" + T_FULL_NAME + "," + T2_FULL_NAME + ")";
	}

	@Override
	public RefactoringQuery getRefactoringQuery() {
		RefactoringQuery changeunitobi = new RefactoringQuery(getName(),
				getQueryString());
		return changeunitobi;
	}

	private String getQueryString() {
		return "before_field(?fFullName, ?, " + T2_FULL_NAME + "),"
				+ "after_field(?fFullName, ?, " + T2_FULL_NAME + "),"
				+ "before_fieldoftype(?fFullName, " + TP_FULL_NAME + "),"
				+ "after_fieldoftype(?fFullName, " + TP_FULL_NAME + "),"
				+ "deleted_field(?f2FullName, ?, " + T_FULL_NAME + "),"
				+ "deleted_fieldoftype(?f2FullName, " + T2P_FULL_NAME + "),"
				+ "NOT(equals(" + T_FULL_NAME + ", " + T2_FULL_NAME + "))";
	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		String tFullName = rs.getString(T_FULL_NAME);
		String t2FullName = rs.getString(T2_FULL_NAME);
		String tpFullName = rs.getString(TP_FULL_NAME);
		String t2pFullName = rs.getString(T2P_FULL_NAME);

		if ((tFullName.equals(tpFullName) && t2pFullName.contains(t2FullName))
				|| (t2FullName.equals(t2pFullName) && tpFullName
						.contains(tFullName))) {
			String result = getName() + "(\"" + tFullName + "\",\""
					+ t2FullName + "\")";
			return result;

		}
		return null;

	}

}
