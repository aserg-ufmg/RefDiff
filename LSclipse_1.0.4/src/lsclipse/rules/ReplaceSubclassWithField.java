/* ReplaceSubclassWithField.java
 * 
 * This class is used to test a tyRuBa result set for adherence 
 * to the replace subclass with field logical refactoring rule.
 * 
 * author:   Kyle Prete
 * created:  8/9/2010
 */
package lsclipse.rules;

import lsclipse.RefactoringQuery;
import lsclipse.utils.CodeCompare;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class ReplaceSubclassWithField implements Rule {
	private static final String F_SHORT_NAME = "?fShortName";
	private static final String SUBT_FULL_NAME = "?subtFullName";
	private static final String SUPERT_FULL_NAME = "?supertFullName";
	private static final String M_FACT_FULL_NAME = "?mFactFullName";
	private static final String SUBT_SHORT_NAME = "?subtShortName";
	private String name_;

	public ReplaceSubclassWithField() {
		name_ = "replace_subclass_with_field";
	}

	@Override
	public String getName() {
		return name_;
	}

	@Override
	public String getRefactoringString() {
		return getName() + "(" + SUPERT_FULL_NAME + "," + SUBT_SHORT_NAME + ","
				+ F_SHORT_NAME + ")";
	}

	@Override
	public RefactoringQuery getRefactoringQuery() {
		return new RefactoringQuery(getName(), getQueryString());
	}

	private String getQueryString() {
		return "replace_constructor_with_factory_method(?, " + M_FACT_FULL_NAME
				+ "),after_method(" + M_FACT_FULL_NAME + ", ?, "
				+ SUPERT_FULL_NAME + "),deleted_subtype(" + SUPERT_FULL_NAME
				+ ", " + SUBT_FULL_NAME + "),added_field(?, " + F_SHORT_NAME
				+ ", " + SUPERT_FULL_NAME + "),before_type(" + SUBT_FULL_NAME
				+ "," + SUBT_SHORT_NAME + ",?)";
	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		String fName = rs.getString(F_SHORT_NAME);
		String subtName = rs.getString(SUBT_SHORT_NAME);

		if (!CodeCompare.compare(fName, subtName))
			return null;

		return getName() + "(\"" + rs.getString(SUPERT_FULL_NAME) + "\",\""
				+ subtName + "\",\"" + fName + "\")";
	}

}
