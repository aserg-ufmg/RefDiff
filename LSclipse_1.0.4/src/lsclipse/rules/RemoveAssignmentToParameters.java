/* RemoveAssignmentToParameters.java
 * 
 * This class is used to check adherence to the remove 
 * assignment to parameters logical refactoring rule.
 * 
 * author:   Kyle Prete
 * created:  8/8/2010
 */
package lsclipse.rules;

import lsclipse.RefactoringQuery;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class RemoveAssignmentToParameters implements Rule {
	private static final String NEWM_BODY = "?newmBody";
	private static final String OLDM_BODY = "?oldmBody";
	private static final String PARAM_LIST = "?paramList";
	private static final String M_FULL_NAME = "?mFullName";
	private String name_;

	public RemoveAssignmentToParameters() {
		name_ = "remove_assignment_to_parameters";
	}

	@Override
	public String getName() {
		return name_;
	}

	@Override
	public String getRefactoringString() {
		return getName() + "(" + M_FULL_NAME + ",?paramName)";
	}

	@Override
	public RefactoringQuery getRefactoringQuery() {
		return new RefactoringQuery(getName(), getQueryString());
	}

	private String getQueryString() {
		return "before_parameter(" + M_FULL_NAME + ", " + PARAM_LIST + ", ?),"
				+ "after_parameter(" + M_FULL_NAME + ", " + PARAM_LIST
				+ ", ?)," + "deleted_methodbody(" + M_FULL_NAME + ", "
				+ OLDM_BODY + ")," + "added_methodbody(" + M_FULL_NAME + ", "
				+ NEWM_BODY + ")";
	}

	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		String[] params = rs.getString(PARAM_LIST).split(",");
		String oldmBody = rs.getString(OLDM_BODY);
		String newmBody = rs.getString(NEWM_BODY);

		// If param list contains only the empty String
		if (params[0].length() == 0)
			return null;

		for (String param : params) {
			String name = param.substring(param.indexOf(':') + 1,
					param.length());
			int oldAssignments = countNumAssignments(name, oldmBody);
			int newAssignments = countNumAssignments(name, newmBody);
			if (newAssignments < oldAssignments) {
				return getName() + "(\"" + rs.getString(M_FULL_NAME) + "\",\""
						+ param + "\")";
			}

		}

		return null;
	}

	private int countNumAssignments(String name, String body) {
		int result = 0;
		int nameAssignmentIndex = -1;
		while (nameAssignmentIndex + 1 < body.length()
				&& (nameAssignmentIndex = findAssignmentIndex(name, body,
						nameAssignmentIndex + 1)) > 0) {
			++result;
		}
		return result;
	}

	// TODO(kprete): Clean up this method.
	private int findAssignmentIndex(String name, String body, int startindex) {
		int index = body.indexOf(name + "=", startindex);
		if (index < 0 || index == body.indexOf(name + "==", startindex)) {
			index = body.indexOf(name + "+=", startindex);
		}
		if (index < 0) {
			index = body.indexOf(name + "-=", startindex);
		}
		if (index < 0) {
			index = body.indexOf(name + "*=", startindex);
		}
		if (index < 0) {
			index = body.indexOf(name + "/=", startindex);
		}
		if (index < 0) {
			index = body.indexOf(name + "%=", startindex);
		}
		if (index < 0) {
			index = body.indexOf(name + "&=", startindex);
		}
		if (index < 0) {
			index = body.indexOf(name + "|=", startindex);
		}
		if (index < 0) {
			index = body.indexOf(name + "^=", startindex);
		}
		if (index < 0) {
			index = body.indexOf(name + "<<=", startindex);
		}
		if (index < 0) {
			index = body.indexOf(name + ">>=", startindex);
		}
		if (index < 0) {
			index = body.indexOf(name + ">>>=", startindex);
		}
		return index;
	}

}
