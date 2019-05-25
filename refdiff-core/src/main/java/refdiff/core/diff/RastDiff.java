package refdiff.core.diff;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import refdiff.core.rast.RastRoot;

public class RastDiff {
	
	private final RastRoot before;
	private final RastRoot after;
	private final Set<Relationship> relationships = new HashSet<>();
	
	public RastDiff(RastRoot before, RastRoot after) {
		this.before = before;
		this.after = after;
	}
	
	public RastRoot getBefore() {
		return before;
	}
	
	public RastRoot getAfter() {
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
