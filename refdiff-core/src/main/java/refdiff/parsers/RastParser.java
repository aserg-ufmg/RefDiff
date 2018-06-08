package refdiff.parsers;

import java.util.List;

import refdiff.core.io.SourceFileSet;
import refdiff.core.rast.RastRoot;

public interface RastParser {
	
	RastRoot parse(SourceFileSet sources) throws Exception;

	List<String> getAllowedFileExtensions();
	
}
