package lsclipse.utils;

public class StringCleaner {
	public static String cleanupString(String s) {
		s = s.trim();
		s = s.replace("\n", "");
		s = s.replace("\"", "");
		s = s.replace(";", "");
		s = s.replace(" ", "");
		s = s.replace("{", "");
		s = s.replace("}", "");
		s = s.replace("\\", ""); // TyRuBa chokes on \'s
		s = s.replace("return", "");
		return s;
	}
}
