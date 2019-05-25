package refdiff.parsers;

import refdiff.core.io.FilePathFilter;
import refdiff.core.io.SourceFileSet;
import refdiff.core.rast.RastRoot;

public interface CstParser {
	
	RastRoot parse(SourceFileSet sources) throws Exception;
	
	FilePathFilter getAllowedFilesFilter();
	
}
