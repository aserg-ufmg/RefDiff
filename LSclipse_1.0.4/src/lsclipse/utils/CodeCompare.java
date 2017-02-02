package lsclipse.utils;

import lsclipse.LCS;

public class CodeCompare {
	public static final double SIMILARITY_THRESHOLD = 0.85;
	public static final double DIFFERNCE_THRESHOLD = 0.75;

	// Make sure the longest common string is at least SIMILARITY_THRESHOLD of
	// the shorter code fragment.
	public static boolean compare(String left, String right) {
		String shorter = getShorterString(left, right);
		String lcs = LCS.getLCS(left, right);
		double similarity = (double) lcs.length() / (double) shorter.length();
		if (similarity >= SIMILARITY_THRESHOLD)
			return true;

		return false;
	}

	// Make sure the longest common string is at most SIMILARITY_THRESHOLD of
	// the longer code fragment.
	public static boolean contrast(String left, String right) {
		String longer = getLongerString(left, right);
		String lcs = LCS.getLCS(left, right);
		double similarity = (double) lcs.length() / (double) longer.length();
		if (similarity <= DIFFERNCE_THRESHOLD)
			return true;

		return false;
	}

	private static String getShorterString(String left, String right) {
		if (left.length() < right.length())
			return left;
		return right;
	}

	private static String getLongerString(String left, String right) {
		if (left.length() > right.length())
			return left;
		return right;
	}

}
