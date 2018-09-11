package refdiff.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AbstractDataset {
	
	protected final List<CommitEntry> commits;
	
	public AbstractDataset() {
		commits = new ArrayList<>();
	}
	
	public void add(RefactoringSet rs) {
		CommitEntry entry = new CommitEntry(rs, new RefactoringSet(rs.getProject(), rs.getRevision()));
		commits.add(entry);
	}
	
	public void add(RefactoringSet rs, RefactoringSet rsNotExpected) {
		CommitEntry entry = new CommitEntry(rs, rsNotExpected);
		commits.add(entry);
	}
	
	public List<RefactoringSet> getExpected() {
		return commits.stream().map(e -> e.expected).collect(Collectors.toList());
	}
	
	public List<RefactoringSet> getNotExpected() {
		return commits.stream().map(e -> e.notExpected).collect(Collectors.toList());
	}
	
	public RefactoringSet remove(String repo, String sha1) {
		for (int i = 0; i < commits.size(); i++) {
			CommitEntry c = commits.get(i);
			if (c.expected.getProject().equals(repo) && c.expected.getRevision().equals(sha1)) {
				return commits.remove(i).expected;
			}
		}
		throw new RuntimeException(String.format("Not found: %s %s", repo, sha1));
	}
	
	public CommitEntry commit(String repo, String sha1) {
		for (int i = 0; i < commits.size(); i++) {
			CommitEntry c = commits.get(i);
			if (c.expected.getProject().equals(repo) && c.expected.getRevision().equals(sha1)) {
				return commits.get(i);
			}
		}
		throw new RuntimeException(String.format("Not found: %s %s", repo, sha1));
	}
	
	public static class CommitEntry {
		private final RefactoringSet expected;
		private final RefactoringSet notExpected;
		
		public CommitEntry(String repo, String sha1) {
			this.expected = new RefactoringSet(repo, sha1);
			this.notExpected = new RefactoringSet(repo, sha1);
		}
		
		public CommitEntry(RefactoringSet expected, RefactoringSet notExpected) {
			this.expected = expected;
			this.notExpected = notExpected;
		}
		
		public CommitEntry addTP(String refType, String entityBefore, String entityAfter) {
			this.expected.add(new RefactoringRelationship(RefactoringType.fromName(refType), entityBefore, entityAfter));
			return this;
		}
		
		public CommitEntry addFP(String refType, String entityBefore, String entityAfter) {
			RefactoringRelationship rr = new RefactoringRelationship(RefactoringType.fromName(refType), entityBefore, entityAfter);
			this.notExpected.add(rr);
			this.expected.remove(rr);
			return this;
		}
		
		public RefactoringSet getExpected() {
			return expected;
		}
		
		public RefactoringSet getNotExpected() {
			return notExpected;
		}
	}
}
