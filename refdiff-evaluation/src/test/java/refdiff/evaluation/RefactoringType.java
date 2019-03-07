package refdiff.evaluation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum RefactoringType {

	MOVE_CLASS("Move Class", "Move Class (.+) moved to (.+)"),
	MOVE_OPERATION("Move Method", "Move Method (.+) from class (.+) to (.+) from class (.+)"),
	MOVE_RENAME_CLASS("Move And Rename Class", ".+"),
	RENAME_CLASS("Rename Class", "Rename Class (.+) renamed to (.+)"),
	RENAME_METHOD("Rename Method", "Rename Method (.+) renamed to (.+) in class (.+)"),
	EXTRACT_INTERFACE("Extract Interface", "Extract Interface (.+) from classes \\[(.+)\\]", 2),
	EXTRACT_SUPERCLASS("Extract Superclass", "Extract Superclass (.+) from classes \\[(.+)\\]", 2),

	CHANGE_METHOD_SIGNATURE("Change Method Signature", "Change Method Signature (.+) to (.+) in class (.+)"),
	PULL_UP_OPERATION("Pull Up Method", "Pull Up Method (.+) from class (.+) to (.+) from class (.+)", 1, 2),
	PUSH_DOWN_OPERATION("Push Down Method", "Push Down Method (.+) from class (.+) to (.+) from class (.+)", 3, 4),
	EXTRACT_OPERATION("Extract Method", "Extract Method (.+) extracted from (.+) in class (.+)", 2),
	INLINE_OPERATION("Inline Method", "Inline Method (.+) inlined to (.+) in class (.+)", 2),

	PULL_UP_ATTRIBUTE("Pull Up Attribute", "Pull Up Attribute (.+) from class (.+) to class (.+)", 2),
	PUSH_DOWN_ATTRIBUTE("Push Down Attribute", "Push Down Attribute (.+) from class (.+) to class (.+)", 3),
	MOVE_ATTRIBUTE("Move Attribute", "Move Attribute (.+) from class (.+) to class (.+)"),

	MOVE_CLASS_FOLDER("Move Class Folder", ".+"),
	//EXTRACT_SUPERTYPE("Extract Supertype", "Extract Supertype (.+) from classes \\[(.+)\\]", 2),
	MERGE_OPERATION("Merge Method", ".+"),
	EXTRACT_AND_MOVE_OPERATION("Extract And Move Method", ".+"),
	CONVERT_ANONYMOUS_CLASS_TO_TYPE("Convert Anonymous Class to Type", ".+"),
	INTRODUCE_POLYMORPHISM("Introduce Polymorphism", ".+"),
	RENAME_PACKAGE("Rename Package", "Rename Package (.+) to (.+)");

	private String displayName;
	private Pattern regex;
	private int[] aggregateGroups;

	private RefactoringType(String displayName, String regex, int ... aggregateGroups) {
		this.displayName = displayName;
		this.regex = Pattern.compile(regex);
		this.aggregateGroups = aggregateGroups;
	}
	
	public Pattern getRegex() {
		return regex;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}
	
	public String getAbbreviation() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.displayName.length(); i++) {
			char c = this.displayName.charAt(i);
			if (Character.isLetter(c) && Character.isUpperCase(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public String aggregate(String refactoringDescription) {
		Matcher m = regex.matcher(refactoringDescription);
		if (m.matches()) {
			StringBuilder sb = new StringBuilder();
			int current = 0;
			int replace = 0;
			for (int g = 1; g <= m.groupCount(); g++) {
				sb.append(refactoringDescription, current, m.start(g));
				if (aggregateGroups.length > replace && g == aggregateGroups[replace]) {
					sb.append('*');
					replace++;
				} else {
					sb.append(refactoringDescription, m.start(g), m.end(g));
				}
				current = m.end(g);
			}
			sb.append(refactoringDescription, current, refactoringDescription.length());
			return sb.toString();
		} else {
			throw new RuntimeException("Pattern not matched: " + refactoringDescription);
		}
	}
	
	public static RefactoringType extractFromDescription(String refactoringDescription) {
		for (RefactoringType refType : RefactoringType.values()) {
			if (refactoringDescription.startsWith(refType.getDisplayName())) {
				return refType;
			}
		}
		throw new RuntimeException("Unknown refactoring type: " + refactoringDescription);
	}
	
	public String getGroup(String refactoringDescription, int group) {
		Matcher m = regex.matcher(refactoringDescription);
		if (m.matches()) {
			return m.group(group);
		} else {
			throw new RuntimeException("Pattern not matched: " + refactoringDescription);
		}
	}
	
	public static RefactoringType fromName(String name) {
		String lcName = name.toLowerCase();
		for (RefactoringType rt : RefactoringType.values()) {
			if (lcName.equals(rt.getDisplayName().toLowerCase())) {
				return rt;
			}
		}
		throw new IllegalArgumentException("refactoring type not known " + name);
	}
}
