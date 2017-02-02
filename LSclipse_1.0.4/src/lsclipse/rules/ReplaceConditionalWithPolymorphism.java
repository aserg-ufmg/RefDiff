/* ReplaceConditionalWithPolymorphism.java
 * 
 * This class is used to check adherence to the replace 
 * conditional with polymorphism logical refactoring rule.
 * 
 * author:   Kyle Prete
 * created:  8/8/2010
 */
package lsclipse.rules;

import lsclipse.RefactoringQuery;
import lsclipse.utils.CodeCompare;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class ReplaceConditionalWithPolymorphism implements Rule {
	private static final String SUBM_FULL_NAME = "?newmFullName";
	private static final String SUBT_FULL_NAME = "?subtFullName";
	private static final String T_FULL_NAME = "?tFullName";
	private static final String M_SHORT_NAME = "?mShortName";
	private static final String M_FULL_NAME = "?mFullName";
	private static final String CONDITION = "?condition";
	private static final String NEWM_BODY = "?newmBody";
	private static final String IF_PART = "?ifPart";
	private static final String F_FULL_NAME = "?fFullName";
	private static final String TYPET_FULL_NAME = "?typeTFullName";
	private static final String TYPEM_FULL_NAME = "?typeMFullName";
	private String name_;

	public ReplaceConditionalWithPolymorphism() {
		name_ = "replace_conditional_with_polymorphism";
	}

	@Override
	public String getName() {
		return name_;
	}

	@Override
	public String getRefactoringString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RefactoringQuery getRefactoringQuery() {
		return new RefactoringQuery(getName(), getQueryString());
	}

	private String getQueryString() {
		return "deleted_conditional(" + CONDITION + "," + IF_PART + ",?,"
				+ M_FULL_NAME + "),before_method(" + M_FULL_NAME + ","
				+ M_SHORT_NAME + "," + T_FULL_NAME + "),(after_subtype("
				+ T_FULL_NAME + "," + SUBT_FULL_NAME + ");(after_field("
				+ F_FULL_NAME + ",?," + T_FULL_NAME + "),after_fieldoftype("
				+ F_FULL_NAME + "," + TYPET_FULL_NAME + "),after_subtype("
				+ TYPET_FULL_NAME + "," + SUBT_FULL_NAME + "),added_method("
				+ TYPEM_FULL_NAME + "," + M_SHORT_NAME + "," + TYPET_FULL_NAME
				+ "),added_calls(" + M_FULL_NAME + "," + TYPEM_FULL_NAME
				+ "))),added_method(" + SUBM_FULL_NAME + "," + M_SHORT_NAME
				+ "," + SUBT_FULL_NAME + "),added_methodbody(" + SUBM_FULL_NAME
				+ "," + NEWM_BODY + ")";

	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		String cond = rs.getString(CONDITION).toLowerCase();

		if (!(cond.toLowerCase().contains("type") || cond
				.contains("instanceof")))
			return null;

		String newmBody_str = rs.getString(NEWM_BODY);
		String ifPart_str = rs.getString(IF_PART);

		if (newmBody_str.length() > 1
				&& CodeCompare.compare(newmBody_str, ifPart_str)) {

			return getName() + "(\"" + rs.getString(M_FULL_NAME) + "\",\""
					+ rs.getString(SUBT_FULL_NAME) + "\")";
		}
		return null;
	}

}
