package refdiff.parsers;

import java.util.Set;

import refdiff.rast.RastRoot;

public interface RastParser {

    RastRoot parse(Set<String> filesOfInterest, FileContentReader reader) throws Exception;

}
