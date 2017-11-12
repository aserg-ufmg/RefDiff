package refdiff.core.diff;

import java.util.Set;

import refdiff.core.io.SourceFile;
import refdiff.core.rast.HasChildrenNodes;
import refdiff.core.rast.RastNode;
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
        return new Comparison(filesBefore, filesAfter).computeDiff();
    }
    
    private class Comparison {
        RastDiff diff;
        
        Comparison(Set<SourceFile> filesBefore, Set<SourceFile> filesAfter) throws Exception {
            this.diff = new RastDiff(parser.parse(filesBefore), parser.parse(filesAfter));
        }
        
        RastDiff computeDiff() {
            matchByName(diff.getBefore(), diff.getAfter());
            return diff;
        }

        private void matchByName(HasChildrenNodes nodes1, HasChildrenNodes nodes2) {
            for (RastNode n1 : nodes1.getNodes()) {
                for (RastNode n2 : nodes2.getNodes()) {
                    if (sameName(n1, n2)) {
                        diff.addRelationships(new Relationship(RelationshipType.SAME, n1, n2, 1.0));
                        matchByName(n1, n2);
                    }
                }
            }
        }

        private boolean sameName(RastNode n1, RastNode n2) {
            return !n1.getLocalName().isEmpty() && n1.getLocalName().equals(n2.getLocalName());
        }
    }
    
}
