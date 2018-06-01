package refdiff.parsers;

import java.util.List;

import refdiff.core.io.SourceFile;
import refdiff.core.rast.RastRoot;

public interface RastParser {
	
	RastRoot parse(List<SourceFile> sourceFiles) throws Exception;

	List<String> getAllowedFileExtensions();
	
}
