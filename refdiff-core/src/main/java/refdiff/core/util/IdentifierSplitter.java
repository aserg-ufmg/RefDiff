package refdiff.core.util;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IdentifierSplitter {
	
	private static final Pattern regex = Pattern.compile("[_/\\.]|(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
	
	public static List<String> split(String identifierOrPath) {
		return regex.splitAsStream(identifierOrPath)
			.filter(s -> !s.isEmpty())
			.collect(Collectors.toList());
	}

}
