package refdiff.parsers.js;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Assert;
import org.junit.Test;

import refdiff.core.diff.RastComparator;
import refdiff.core.diff.RastDiff;
import refdiff.core.diff.RelationshipType;
import refdiff.core.io.FileSystemSourceFile;
import refdiff.core.io.SourceFile;
import refdiff.core.rast.RastRoot;

public class TestRastComparator {

    @Test
    public void shouldParseSimpleFile() throws Exception {
        EsprimaParser parser = new EsprimaParser();
        
        String basePathV0 = "src/test/resources/diff1/v0/";
        Set<SourceFile> sourceFilesBefore = Collections.singleton(new FileSystemSourceFile(basePathV0, "hello.js"));
        
        String basePathV1 = "src/test/resources/diff1/v1/";
        Set<SourceFile> sourceFilesAfter = Collections.singleton(new FileSystemSourceFile(basePathV1, "hello.js"));
        
        RastComparator comparator = new RastComparator(parser, parser);

        RastDiff diff = comparator.compare(sourceFilesBefore, sourceFilesAfter);
        
        Assert.assertThat(diff, contains(
            relationship(RelationshipType.SAME, "hello.js#hello", "hello.js#hello")
        ));
        
    }
    
    private RelationshipQuery relationship(RelationshipType type, String nameBefore, String nameAfter) {
        return new RelationshipQuery(type, nameBefore, nameAfter);
    }
    
    private Matcher<RastDiff> contains(RelationshipQuery ... queries) {
        return new RastDiffMatcher(queries);
    }
    
    private static class RelationshipQuery {
        final RelationshipType type;
        final String nameBefore;
        final String nameAfter;
        public RelationshipQuery(RelationshipType type, String nameBefore, String nameAfter) {
            this.type = type;
            this.nameBefore = nameBefore;
            this.nameAfter = nameAfter;
        }
        @Override
        public String toString() {
            return String.format("%s(%s, %s)", type, nameBefore, nameAfter);
        }
        
        public boolean find(RastDiff diff) {
            RastRoot before = diff.getAfter();
            RastRoot after = diff.getAfter();
            return false;
        }
    }
    
    private static class RastDiffMatcher extends TypeSafeDiagnosingMatcher<RastDiff> {

        RelationshipQuery[] queries;
        
        public RastDiffMatcher(RelationshipQuery ... queries) {
            this.queries = queries;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(queries.length + " true positives");
        }

        @Override
        protected boolean matchesSafely(RastDiff diff, Description mismatchDescription) {
            int tp = 0;
            int fn = 0;
            List<RelationshipQuery> notFound = new ArrayList<>();
            for (RelationshipQuery query : queries) {
                if (query.find(diff)) {
                    tp++;
                } else {
                    fn++;
                    notFound.add(query);
                }
            }
            if (fn != 0) {
                mismatchDescription.appendText(String.format("%d true positives, %d false negatives", tp, fn));
                mismatchDescription.appendValueList("\n", "\n", "", notFound);
                return false;
            }
            return true;
        }
        
    }
}
