package refdiff.parsers;

import java.util.Set;

import refdiff.core.io.SourceFile;
import refdiff.core.rast.RastRoot;

public interface RastParser {
	
	RastRoot parse(Set<SourceFile> sourceFiles) throws Exception;
	
}
