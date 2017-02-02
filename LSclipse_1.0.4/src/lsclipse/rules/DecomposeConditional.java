/* DecomposeConditional.java
 * 
 * This class is used to test a tyRuBa result set for more liberal adherence 
 * to the "decompose conditional" logical refactoring rule.
 * 
 * author:   Kyle Prete
 * created:  7/20/2010
 */
package lsclipse.rules;

import lsclipse.RefactoringQuery;
import lsclipse.utils.CodeCompare;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;

public class DecomposeConditional implements Rule {
	private ExtractMethod conditionExtractMethod = null;
	private ExtractMethod ifBlockExtractMethod = null;
	private ExtractMethod elseBlockExtractMethod = null;

	private String name_;

	public static final String ELSE_BLOCK_FULL_NAME = "?m3FullName";
	public static final String IF_BLOCK_FULL_NAME = "?m2FullName";
	public static final String CONDITION_M_FULL_NAME = "?m1FullName";
	public static final String ELSE_BLOCK_2 = "?elseBlockB";
	public static final String IF_BLOCK_2 = "?ifBlockB";
	public static final String CONDITION_BLOCK_2 = "?conditionB";
	public static final String M_FULL_NAME = "?mFullName";
	public static final String ELSE_BLOCK = "?elseBlock";
	public static final String IF_BLOCK = "?ifBlock";
	public static final String CONDITION_BLOCK = "?condition";

	public DecomposeConditional() {
		super();
		name_ = "decompose_conditional";

		conditionExtractMethod = new ExtractMethod(M_FULL_NAME,
				CONDITION_M_FULL_NAME, CONDITION_BLOCK_2, "?t1FullName");
		ifBlockExtractMethod = new ExtractMethod(M_FULL_NAME,
				IF_BLOCK_FULL_NAME, IF_BLOCK_2, "?t2FullName");
		elseBlockExtractMethod = new ExtractMethod(M_FULL_NAME,
				ELSE_BLOCK_FULL_NAME, ELSE_BLOCK_2, "?t3FullName");

	}

	private String getQueryString() {
		return "deleted_conditional(" + CONDITION_BLOCK + ", " + IF_BLOCK
				+ ", " + ELSE_BLOCK + ", " + M_FULL_NAME + "), "
				+ conditionExtractMethod.getRefactoringString() + ", "
				+ ifBlockExtractMethod.getRefactoringString() + ", "
				+ elseBlockExtractMethod.getRefactoringString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lsclipse.rules.Rule#getRefactoringQuery()
	 */
	@Override
	public RefactoringQuery getRefactoringQuery() {
		RefactoringQuery decompose_conditional = new RefactoringQuery(
				getName(), getQueryString());
		return decompose_conditional;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lsclipse.rules.Rule#checkRule(tyRuBa.tdbc.ResultSet)
	 */
	@Override
	public String checkAdherence(ResultSet rs) throws TyrubaException {
		// Make sure extracted methods are different
		String m1FullName = rs.getString(CONDITION_M_FULL_NAME);
		String m2FullName = rs.getString(IF_BLOCK_FULL_NAME);
		String m3FullName = rs.getString(ELSE_BLOCK_FULL_NAME);
		// Check if blocks of conditional are similar enough
		String ifBlock = rs.getString(IF_BLOCK);
		String ifBlockB = rs.getString(IF_BLOCK_2);
		String elseBlock = rs.getString(ELSE_BLOCK);
		String elseBlockB = rs.getString(ELSE_BLOCK_2);
		String condition = rs.getString(CONDITION_BLOCK);
		String conditionB = rs.getString(CONDITION_BLOCK_2);
		if (!m1FullName.equals(m2FullName) && !m1FullName.equals(m3FullName)
				&& !m2FullName.equals(m3FullName)
				&& CodeCompare.compare(ifBlock, ifBlockB)
				&& CodeCompare.compare(elseBlock, elseBlockB)
				&& CodeCompare.compare(condition, conditionB)) {
			return getName() + "(\"" + condition + "\",\"" + ifBlock
					+ "\",\"" + elseBlock + "\",\""
					+ rs.getString(M_FULL_NAME) + "\")";
		}
		return null;
	}

	@Override
	public String getName() {
		return name_;
	}

	@Override
	public String getRefactoringString() {
		return getName() + "(" + CONDITION_BLOCK + "," + IF_BLOCK + ","
				+ ELSE_BLOCK + "," + M_FULL_NAME + ")";
	}

}
