/* EncapsulateCollection.java
 * 
 * This class is used to check adherence to the encapsulate
 * collection logical refactoring rule.
 * 
 * author:   Kyle Prete
 * created:  8/4/2010
 */
package lsclipse.rules;

import lsclipse.RefactoringQuery;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class EncapsulateCollection implements Rule {
	private String name_;

	public EncapsulateCollection() {
		name_ = "encapsulate_collection";
	}

	@Override
	public String getName() {
		return name_;
	}

	@Override
	public String getRefactoringString() {
		return getName() + "(" + "?fFullName" + ")";
	}

	@Override
	public RefactoringQuery getRefactoringQuery() {
		RefactoringQuery encap_coll = new RefactoringQuery(getName(),
				getQueryString());
		return encap_coll;
	}

	private String getQueryString() {
		return "before_field(" + "?fFullName" + ", ?, " + "?tFullName" + "),"
				+ "after_field(" + "?fFullName" + ", ?, " + "?tFullName" + "),"
				+ "before_accesses(" + "?fFullName" + ", " + "?getmFullName"
				+ ")," + "after_accesses(" + "?fFullName" + ", "
				+ "?getmFullName" + ")," + "added_calls(" + "?getmFullName"
				+ ", " + "?unmodmFullName" + ")," + "after_method("
				+ "?getmFullName" + ", " + "?getmShortName" + ", "
				+ "?tFullName" + ")," + "added_method(" + "?m1FullName" + ", "
				+ "?m1ShortName" + ", " + "?tFullName" + "),"
				+ "added_accesses(" + "?fFullName" + ", " + "?m1FullName"
				+ ")," + "added_method(" + "?m2FullName" + ", "
				+ "?m2ShortName" + ", " + "?tFullName" + "),"
				+ "added_accesses(" + "?fFullName" + ", " + "?m2FullName"
				+ ")," + "NOT(equals(" + "?m1FullName" + ", " + "?m2FullName"
				+ "))," + "deleted_method(" + "?setmFullName" + ", "
				+ "?setmShortName" + ", " + "?tFullName" + "),"
				+ "deleted_accesses(" + "?fFullName" + ", " + "?setmFullName"
				+ ")";
	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		// Confirm getter
		String getmShortName = rs.getString("?getmShortName");
		if (!getmShortName.startsWith("get"))
			return null;

		// Confirm setter
		String setmShortName = rs.getString("?setmShortName");
		if (!setmShortName.startsWith("set"))
			return null;

		// Confirm add/remove methods
		String m1ShortName = rs.getString("?m1ShortName");
		String m2ShortName = rs.getString("?m2ShortName");
		if (!((m1ShortName.startsWith("add") && m2ShortName
				.startsWith("remove")) || (m2ShortName.startsWith("add") && m1ShortName
				.startsWith("remove"))))
			return null;

		// Confirm call to an unmodifiable factory.
		String unmodmFullName = rs.getString("?unmodmFullName");
		if (!unmodmFullName.startsWith("java.util%.Collections#unmodifiable"))
			return null;

		return getName() + "(\"" + rs.getString("?fFullName") + "\")";

	}

}
