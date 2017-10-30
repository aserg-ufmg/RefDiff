package refdiff.core.diff;

import java.util.Set;

import refdiff.core.io.SourceFile;
import refdiff.core.rast.RastRoot;
import refdiff.parsers.RastParser;
import refdiff.parsers.SourceTokenizer;

public class RastComparator {

    private final RastParser parser;
    private final SourceTokenizer tokenizer;
    
    public RastComparator(RastParser parser, SourceTokenizer tokenizer) {
        this.parser = parser;
        this.tokenizer = tokenizer;
    }

    public RastDiff compare(Set<SourceFile> filesBefore, Set<SourceFile> filesAfter) throws Exception {
        RastRoot rastBefore = parser.parse(filesBefore);
        RastRoot rastAfter = parser.parse(filesAfter);
        
        RastDiff diff = new RastDiff(rastBefore, rastAfter);
        return diff;
    }
}
