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

import refdiff.core.diff.CstDiff;
import refdiff.core.diff.CstRootHelper;
import refdiff.core.diff.Relationship;
import refdiff.core.diff.RelationshipType;
import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstRoot;

public class CstDiffMatchers {
	
	public static RelationshipQuery relationship(RelationshipType type, NodeQuery nodeBefore, NodeQuery nodeAfter) {
		return new RelationshipQuery(type, nodeBefore, nodeAfter);
	}
	
	public static NodeQuery node(String... path) {
		return new NodeQuery(path);
	}
	
	public static CstDiffMatcher contains(RelationshipQuery... queries) {
		return new CstDiffMatcher(false, queries);
	}
	
	public static CstDiffMatcher containsOnly(RelationshipQuery... queries) {
		return new CstDiffMatcher(true, queries);
	}
	
	public static Matcher<CstDiff> doesntContain(RelationshipQuery... queries) {
		return new CstDiffMatcherDoesntContain(queries);
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
		
		public Optional<Relationship> find(CstDiff diff) {
			CstRoot before = diff.getBefore();
			CstRoot after = diff.getAfter();
			Optional<CstNode> oNodeBefore = CstRootHelper.findByNamePath(before, nodeQBefore.namePath);
			Optional<CstNode> oNodeAfter = CstRootHelper.findByNamePath(after, nodeQAfter.namePath);
			return oNodeBefore.flatMap(nodeBefore -> oNodeAfter.flatMap(nodeAfter -> {
				Relationship r = new Relationship(type, nodeBefore, nodeAfter);
				if (diff.getRelationships().contains(r)) {
					return Optional.of(r);
				}
				return Optional.empty();
			}));
		}
	}
	
	private static class CstDiffMatcher extends TypeSafeDiagnosingMatcher<CstDiff> {
		
		RelationshipQuery[] queries;
		boolean computeFp;
		
		public CstDiffMatcher(boolean computeFp, RelationshipQuery... queries) {
			this.computeFp = computeFp;
			this.queries = queries;
		}
		
		@Override
		public void describeTo(Description description) {
			description.appendText(queries.length + " true positives");
		}
		
		@Override
		protected boolean matchesSafely(CstDiff diff, Description mismatchDescription) {
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
	
	private static class CstDiffMatcherDoesntContain extends TypeSafeDiagnosingMatcher<CstDiff> {
		
		RelationshipQuery[] queries;
		
		public CstDiffMatcherDoesntContain(RelationshipQuery... queries) {
			this.queries = queries;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText(queries.length + " true positives");
		}

		@Override
		protected boolean matchesSafely(CstDiff diff, Description mismatchDescription) {
			int truePositives = 0;
			int falsePositives = 0;
			
			for (RelationshipQuery query : queries) {
				Optional<Relationship> optional = query.find(diff);
				if (optional.isPresent()) {
					falsePositives++;
				} else {
					truePositives++;
				}
			}
			
			if (falsePositives != 0) {
				mismatchDescription.appendText(String.format(
						"%d true positives, %d false positives", truePositives, falsePositives));
				return false;
			}
			return true;
		}
	}
	
}
