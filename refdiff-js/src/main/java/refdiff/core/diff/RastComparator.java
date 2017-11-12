package refdiff.core.diff;

import java.util.Set;
import java.util.function.BiConsumer;

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
            matchByLogicalName(diff.getBefore(), diff.getAfter());
            return diff;
        }

        private void matchByLogicalName(HasChildrenNodes nodes1, HasChildrenNodes nodes2) {
            forAllWithSameLogicalName(nodes1, nodes2, (n1, n2) -> {
                diff.addRelationships(new Relationship(RelationshipType.SAME, n1, n2, 1.0));
            });
        }

        private void forAllWithSameLogicalName(HasChildrenNodes nodes1, HasChildrenNodes nodes2, BiConsumer<RastNode, RastNode> function) {
            for (RastNode n1 : nodes1.getNodes()) {
                for (RastNode n2 : nodes2.getNodes()) {
                    if (!n1.getLocalName().isEmpty() && n1.getLocalName().equals(n2.getLocalName())) {
                        function.accept(n1, n2);
                    }
                }
            }
        }
        
        
    }
    
}
