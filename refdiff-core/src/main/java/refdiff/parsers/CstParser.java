package refdiff.parsers;

import refdiff.core.io.FilePathFilter;
import refdiff.core.io.SourceFileSet;
import refdiff.core.cst.CstRoot;

public interface CstParser {
	
	CstRoot parse(SourceFileSet sources) throws Exception;
	
	FilePathFilter getAllowedFilesFilter();
	
}
