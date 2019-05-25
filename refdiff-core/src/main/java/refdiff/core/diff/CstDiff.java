package refdiff.core.diff;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import refdiff.core.cst.CstRoot;

public class CstDiff {
	
	private final CstRoot before;
	private final CstRoot after;
	private final Set<Relationship> relationships = new HashSet<>();
	
	public CstDiff(CstRoot before, CstRoot after) {
		this.before = before;
		this.after = after;
	}
	
	public CstRoot getBefore() {
		return before;
	}
	
	public CstRoot getAfter() {
		return after;
	}
	
	public Set<Relationship> getRelationships() {
		return Collections.unmodifiableSet(relationships);
	}
	
	public void addRelationships(Relationship relationship) {
		relationships.add(relationship);
	}
	
	public Set<Relationship> getRefactoringRelationships() {
		return relationships.stream()
			.filter(Relationship::isRefactoring)
			.collect(Collectors.toSet());
	}
}
