package refdiff.test.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import refdiff.core.diff.RastDiff;
import refdiff.core.diff.Relationship;
import refdiff.core.diff.RelationshipType;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastRoot;

public class RastDiffMatchers {
	
	public static RelationshipQuery relationship(RelationshipType type, NodeQuery nodeBefore, NodeQuery nodeAfter) {
		return new RelationshipQuery(type, nodeBefore, nodeAfter);
	}
	
	public static NodeQuery node(String... path) {
		return new NodeQuery(path);
	}
	
	public static Matcher<RastDiff> contains(RelationshipQuery... queries) {
		return new RastDiffMatcher(false, queries);
	}
	
	public static Matcher<RastDiff> containsOnly(RelationshipQuery... queries) {
		return new RastDiffMatcher(true, queries);
	}
	
	public static class NodeQuery {
		final String[] namePath;
		
		public NodeQuery(String... namePath) {
			this.namePath = namePath;
		}
		
		@Override
		public String toString() {
			return Arrays.toString(namePath);
		}
	}
	
	public static class RelationshipQuery {
		final RelationshipType type;
		final NodeQuery nodeQBefore;
		final NodeQuery nodeQAfter;
		
		public RelationshipQuery(RelationshipType type, NodeQuery nodeQBefore, NodeQuery nodeQAfter) {
			this.type = type;
			this.nodeQBefore = nodeQBefore;
			this.nodeQAfter = nodeQAfter;
		}
		
		@Override
		public String toString() {
			return String.format("%s(%s, %s)", type, nodeQBefore, nodeQAfter);
		}
		
		public Optional<Relationship> find(RastDiff diff) {
			RastRoot before = diff.getBefore();
			RastRoot after = diff.getAfter();
			Optional<RastNode> oNodeBefore = before.findByNamePath(nodeQBefore.namePath);
			Optional<RastNode> oNodeAfter = after.findByNamePath(nodeQAfter.namePath);
			return oNodeBefore.flatMap(nodeBefore -> oNodeAfter.flatMap(nodeAfter -> {
				Relationship r = new Relationship(type, nodeBefore, nodeAfter);
				if (diff.getRelationships().contains(r)) {
					return Optional.of(r);
				}
				return Optional.empty();
			}));
		}
	}
	
	private static class RastDiffMatcher extends TypeSafeDiagnosingMatcher<RastDiff> {
		
		RelationshipQuery[] queries;
		boolean computeFp;
		
		public RastDiffMatcher(boolean computeFp, RelationshipQuery... queries) {
			this.computeFp = computeFp;
			this.queries = queries;
		}
		
		@Override
		public void describeTo(Description description) {
			description.appendText(queries.length + " true positives");
		}
		
		@Override
		protected boolean matchesSafely(RastDiff diff, Description mismatchDescription) {
			List<RelationshipQuery> falseNegatives = new ArrayList<>();
			Set<Relationship> truePositives = new HashSet<>();
			Set<Relationship> falsePositives = new HashSet<>(diff.getRelationships());
			for (RelationshipQuery query : queries) {
				Optional<Relationship> optional = query.find(diff);
				if (optional.isPresent()) {
					truePositives.add(optional.get());
					falsePositives.remove(optional.get());
				} else {
					falseNegatives.add(query);
				}
			}
			int tp = truePositives.size();
			int fp = computeFp ? falsePositives.size() : 0;
			int fn = falseNegatives.size();
			if (fn != 0 || fp != 0) {
				if (computeFp) {
					mismatchDescription.appendText(String.format("%d true positives, %d false positives, %d false negatives", tp, fp, fn));
				} else {
					mismatchDescription.appendText(String.format("%d true positives, %d false negatives", tp, fn));
				}
				if (!falseNegatives.isEmpty()) {
					mismatchDescription.appendValueList("\nFalse negatives:\n", "\n", "", falseNegatives);
				}
				if (!falsePositives.isEmpty()) {
					mismatchDescription.appendValueList("\nFalse positives:\n", "\n", "", falsePositives);
				}
				return false;
			}
			return true;
		}
		
	}
	
}
