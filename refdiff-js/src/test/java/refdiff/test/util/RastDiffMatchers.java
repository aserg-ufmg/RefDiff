package refdiff.test.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import refdiff.core.diff.RastDiff;
import refdiff.core.diff.Relationship;
import refdiff.core.diff.RelationshipType;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastRoot;

public class RastDiffMatchers {

    public static RelationshipQuery relationship(RelationshipType type, String nameBefore, String nameAfter) {
        return new RelationshipQuery(type, nameBefore, nameAfter);
    }

    public static Matcher<RastDiff> contains(RelationshipQuery... queries) {
        return new RastDiffMatcher(queries);
    }

    public static class RelationshipQuery {
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
            Optional<RastNode> nodeBefore = before.findByName(nameBefore);
            Optional<RastNode> nodeAfter = after.findByName(nameAfter);
            if (nodeBefore.isPresent() && nodeAfter.isPresent()) {
                return diff.getRelationships().contains(new Relationship(type, nodeBefore.get(), nodeAfter.get(), 0.0));
            }
            return false;
        }
    }

    private static class RastDiffMatcher extends TypeSafeDiagnosingMatcher<RastDiff> {

        RelationshipQuery[] queries;

        public RastDiffMatcher(RelationshipQuery... queries) {
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
