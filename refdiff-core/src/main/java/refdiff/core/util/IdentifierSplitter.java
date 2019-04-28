package refdiff.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IdentifierSplitter {
	
	private static final Pattern regex = Pattern.compile("[_/\\.]|(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
	
	public static List<String> split(String identifierOrPath) {
		return regex.splitAsStream(identifierOrPath)
			.filter(s -> !s.isEmpty())
			.collect(Collectors.toList());
	}
	
	// public static List<String> split(String identifierOrPath) {
	// return regex.splitAsStream(identifierOrPath)
	// .filter(s -> !s.isEmpty())
	// .collect(Collectors.toList());
	// }
	
	public static int split(String identifierOrPath, Consumer<String> onSplitIdentifier) {
		final int length = identifierOrPath.length();
		int pos = 0;
		int count = 0;
		for (int i = 0; i < length; i++) {
			char c = identifierOrPath.charAt(i);
			boolean isSeparator = c == '_' || c == '/' || c == '.';
			boolean isUpperAlpha = c >= 'A' && c <= 'Z';
			char nextC = i + 1 < length ? identifierOrPath.charAt(i + 1) : '\0';
			char prevC = i - 1 >= 0 ? identifierOrPath.charAt(i - 1) : '\0';
			boolean nextIsLowerAlpha = nextC >= 'a' && nextC <= 'z';
			boolean prevIsLowerAlpha = prevC >= 'a' && prevC <= 'z';
			if (isSeparator || (isUpperAlpha && (nextIsLowerAlpha || prevIsLowerAlpha))) {
				if (pos < i) {
					onSplitIdentifier.accept(identifierOrPath.substring(pos, i));
					count++;
					if (isSeparator) {
						pos = i + 1;
					} else {
						pos = i;
					}
				} else {
					if (isSeparator) {
						pos = i + 1;
					}
				}
			}
		}
		if (pos < length - 1) {
			onSplitIdentifier.accept(identifierOrPath.substring(pos, length));
			count++;
		}
		return count;
	}
	
	public static List<String> split2(String identifierOrPath) {
		List<String> result = new ArrayList<>();
		split(identifierOrPath, result::add);
		return result;
	}
	
	@FunctionalInterface
	public interface OnSplitIdentifier {
		void onSplitIdentifier(int start, int end);
	}
}
