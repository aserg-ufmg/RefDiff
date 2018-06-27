package refdiff.core.io;

import java.util.Collections;
import java.util.List;

public class FilePathFilter {
	private final List<String> allowedFileExtensions;
	private final List<String> excludedFileExtensions;
	
	public FilePathFilter(List<String> allowedFileExtensions) {
		this(allowedFileExtensions, Collections.emptyList());
	}
	
	public FilePathFilter(List<String> allowedFileExtensions, List<String> excludedFileExtensions) {
		this.allowedFileExtensions = allowedFileExtensions;
		this.excludedFileExtensions = excludedFileExtensions;
	}
	
	public boolean isAllowed(String filePath) {
		for (String fileExtension : excludedFileExtensions) {
			if (filePath.endsWith(fileExtension)) {
				return false;
			}
		}
		for (String fileExtension : allowedFileExtensions) {
			if (filePath.endsWith(fileExtension)) {
				return true;
			}
		}
		return false;
	}
}
