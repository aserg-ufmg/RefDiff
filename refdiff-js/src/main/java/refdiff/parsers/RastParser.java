package refdiff.parsers;

import java.util.Set;

import refdiff.core.io.SourceReader;
import refdiff.core.rast.RastRoot;

public interface RastParser {

    RastRoot parse(Set<String> filesOfInterest, SourceReader reader) throws Exception;

}
