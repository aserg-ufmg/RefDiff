package refdiff.core.diff;

import static refdiff.core.diff.RastRootHelper.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import refdiff.core.diff.similarity.SourceRepresentationBuilder;
import refdiff.core.io.SourceFile;
import refdiff.core.rast.HasChildrenNodes;
import refdiff.core.rast.Location;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastNodeRelationshipType;
import refdiff.core.rast.Stereotype;
import refdiff.core.util.Statistics;
import refdiff.parsers.RastParser;
import refdiff.parsers.SourceTokenizer;

public class RastComparator<T> {
	
	private final RastParser parser;
	private final SourceTokenizer tokenizer;
	private final SourceRepresentationBuilder<T> srb;
	RastComparatorThresholds thresholds;
	
	public RastComparator(RastParser parser, SourceTokenizer tokenizer, SourceRepresentationBuilder<T> srb, RastComparatorThresholds thresholds) {
		this.parser = parser;
		this.tokenizer = tokenizer;
		this.srb = srb;
		this.thresholds = thresholds;
	}
	
	public RastDiff compare(List<SourceFile> filesBefore, List<SourceFile> filesAfter) throws Exception {
		DiffBuilder diffBuilder = new DiffBuilder(filesBefore, filesAfter);
		RastDiff diff = diffBuilder.computeDiff();
		diffBuilder.reportSimilarity();
		return diff;
	}
	
	private class DiffBuilder {
		private RastDiff diff;
		private RastRootHelper<T> before;
		private RastRootHelper<T> after;
		private Set<RastNode> removed;
		private Set<RastNode> added;
		private ArrayList<Double> similaritySame = new ArrayList<>();
		
		private final Map<RastNode, T> srMap = new HashMap<>();
		private final Map<RastNode, RastNode> mapBeforeToAfter = new HashMap<>();
		private final Map<RastNode, RastNode> mapAfterToBefore = new HashMap<>();
		private final Map<RastNode, Integer> depthMap = new HashMap<>();
		private final Map<String, SourceFile> fileMapBefore = new HashMap<>();
		private final Map<String, SourceFile> fileMapAfter = new HashMap<>();
		
		DiffBuilder(List<SourceFile> filesBefore, List<SourceFile> filesAfter) throws Exception {
			this.diff = new RastDiff(parser.parse(filesBefore), parser.parse(filesAfter));
			this.before = new RastRootHelper<T>(this.diff.getBefore());
			this.after = new RastRootHelper<T>(this.diff.getAfter());
			this.removed = new HashSet<>();
			for (SourceFile fileBefore : filesBefore) {
				fileMapBefore.put(fileBefore.getPath(), fileBefore);
			}
			for (SourceFile fileAfter : filesAfter) {
				fileMapAfter.put(fileAfter.getPath(), fileAfter);
			}
			this.diff.getBefore().forEachNode((node, depth) -> {
				this.removed.add(node);
				computeSourceRepresentation(fileMapBefore, node);
				depthMap.put(node, depth);
			});
			this.added = new HashSet<>();
			this.diff.getAfter().forEachNode((node, depth) -> {
				this.added.add(node);
				computeSourceRepresentation(fileMapAfter, node);
				depthMap.put(node, depth);
			});
		}
		
		private void apply(Set<Relationship> relationships) {
			for (Relationship r : relationships) {
				apply(r);
			}
		}

		private void apply(Relationship r) {
			diff.addRelationships(r);
			if (r.getType().isUnmarkRemoved()) {
				removed.remove(r.getNodeBefore());
			}
			if (r.getType().isUnmarkAdded()) {
				added.remove(r.getNodeAfter());
			}
		}
		
		private void computeSourceRepresentation(Map<String, SourceFile> fileMap, RastNode node) {
			String sourceCode = retrieveSourceCode(fileMap, node);
			T sourceCodeRepresentation = srb.buildForNode(node, tokenizer.tokenize(sourceCode));
			srMap.put(node, sourceCodeRepresentation);
		}
		
		private String retrieveSourceCodeBefore(RastNode node) {
			return retrieveSourceCode(fileMapBefore, node);
		}
		
		private String retrieveSourceCodeAfter(RastNode node) {
			return retrieveSourceCode(fileMapAfter, node);
		}
		
		private String retrieveSourceCode(Map<String, SourceFile> fileMap, RastNode node) {
			try {
				Location location = node.getLocation();
				SourceFile sourceFile = fileMap.get(location.getFile());
				return sourceFile.getContent().substring(location.getBodyBegin(), location.getBodyEnd());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		RastDiff computeDiff() {
			matchExactChildren(diff.getBefore(), diff.getAfter());
			matchPullUpAndPushDownMembers();
			matchExtractSuper();
			matchMovesOrRenames();
			matchExtract();
			matchInline();
			return diff;
		}
		
		private void matchExtractSuper() {
			Set<Relationship> relationships = new HashSet<>();
			for (RastNode potentialSupertype : added) {
				relationships.addAll(findPullUpMembers(potentialSupertype));
			}
			apply(relationships);
		}

		private void matchMovesOrRenames() {
			List<PotentialMatch> candidates = new ArrayList<>();
			for (RastNode n1 : removed) {
				for (RastNode n2 : added) {
					if (sameType(n1, n2) && !anonymous(n1) && !anonymous(n2)) {
						double score = srb.similarity(sourceRep(n1), sourceRep(n2));
						if (score > thresholds.moveOrRename) {
							PotentialMatch candidate = new PotentialMatch(n1, n2, Math.max(depth(n1), depth(n2)), score);
							candidates.add(candidate);
						}
					}
				}
			}
			Collections.sort(candidates);
			for (PotentialMatch candidate : candidates) {
				RastNode n1 = candidate.getNodeBefore();
				RastNode n2 = candidate.getNodeAfter();
				if (sameLocation(n1, n2)) {
					if (sameName(n1, n2)) {
						// change signature
					} else {
						addMatch(new Relationship(RelationshipType.RENAME, n1, n2));
					}
				} else {
					if (sameSignature(n1, n2)) {
						addMatch(new Relationship(RelationshipType.MOVE, n1, n2));
					} else {
						// move and rename / move and rename and change signature
					}
				}
			}
		}
		
		private void matchExtract() {
			Set<Relationship> relationships = new HashSet<>();
			for (RastNode n2 : added) {
				for (RastNode n1After : after.findReverseRelationships(RastNodeRelationshipType.USE, n2)) {
					Optional<RastNode> optMatchingNode = matchingNodeBefore(n1After);
					if (optMatchingNode.isPresent()) {
						RastNode n1 = optMatchingNode.get();
						if (sameType(n1, n2)) {
							T sourceN1After = sourceRep(n1After);
							T sourceN1Before = sourceRep(n1);
							T removedSource = srb.minus(sourceN1Before, sourceN1After);
							double score = srb.partialSimilarity(sourceRep(n2), removedSource);
							if (score > thresholds.extract) {
								relationships.add(new Relationship(RelationshipType.EXTRACT, n1, n2));
							}
						}
					}
				}
			}
			apply(relationships);
		}
		
		private void matchInline() {
			Set<Relationship> relationships = new HashSet<>();
			for (RastNode n1 : removed) {
				for (RastNode n1Caller : before.findReverseRelationships(RastNodeRelationshipType.USE, n1)) {
					Optional<RastNode> optMatchingNode = matchingNodeAfter(n1Caller);
					if (optMatchingNode.isPresent()) {
						RastNode n2 = optMatchingNode.get();
						if (sameType(n1, n2)) {
							T sourceN1 = sourceRep(n1);
							T sourceN1Caller = sourceRep(n1Caller);
							T sourceN1CallerAfter = sourceRep(n2);
							T addedCode = srb.minus(sourceN1CallerAfter, sourceN1Caller);
							double score = srb.partialSimilarity(sourceN1, addedCode);
							if (score > thresholds.inline) {
								relationships.add(new Relationship(RelationshipType.INLINE, n1, n2));
							}
						}
					}
				}
			}
			apply(relationships);
		}
		
		private void matchExactChildren(HasChildrenNodes node1, HasChildrenNodes node2) {
			List<RastNode> removedChildren = children(node1, this::removed);
			List<RastNode> addedChildren = children(node2, this::added);
			for (RastNode n1 : removedChildren) {
				for (RastNode n2 : addedChildren) {
					if (sameNamespace(n1, n2) && sameSignature(n1, n2)) {
						if (sameType(n1, n2)) {
							addMatch(new Relationship(RelationshipType.SAME, n1, n2));
							recordSimilarity(n1, n2);
						} else {
							addMatch(new Relationship(RelationshipType.CONVERT_TYPE, n1, n2));
						}
					}
				}
			}
		}

		private void matchPullUpAndPushDownMembers() {
			for (Map.Entry<RastNode, RastNode> entry : mapBeforeToAfter.entrySet()) {
				RastNode nBefore = entry.getKey();
				RastNode nAfter = entry.getValue();
				apply(findPullUpMembers(nAfter));
				apply(findPushDownMembers(nBefore, nAfter));
			}
		}

		private Set<Relationship> findPushDownMembers(RastNode nBefore, RastNode nAfter) {
			Set<Relationship> relationships = new HashSet<>();
			for (RastNode removedMember : children(nBefore, m -> removed(m) && m.hasStereotype(Stereotype.TYPE_MEMBER))) {
				for (RastNode subtype : after.findReverseRelationships(RastNodeRelationshipType.SUBTYPE, nAfter)) {
					Optional<RastNode> optNode = findByFullName(subtype, fullName(removedMember));
					if (optNode.isPresent() && optNode.get().hasStereotype(Stereotype.TYPE_MEMBER) && added(optNode.get())) {
						relationships.add(new Relationship(RelationshipType.PUSH_DOWN, removedMember, optNode.get()));
					}
				}
			}
			return relationships;
		}

		private Set<Relationship> findPullUpMembers(RastNode supertypeAfter) {
			Collection<RastNode> subtypesAfter = after.findReverseRelationships(RastNodeRelationshipType.SUBTYPE, supertypeAfter);
			if (subtypesAfter.isEmpty()) {
				return Collections.emptySet();
			}
			Set<Relationship> relationships = new HashSet<>();
			Set<RastNode> subtypesWithPulledUpMembers = new HashSet<>();
			for (RastNode addedMember : children(supertypeAfter, m -> added(m) && m.hasStereotype(Stereotype.TYPE_MEMBER))) {
				for (RastNode subtypeAfter : subtypesAfter) {
					Optional<RastNode> optSubtypeBefore = matchingNodeBefore(subtypeAfter);
					if (optSubtypeBefore.isPresent()) {
						RastNode subtypeBefore = optSubtypeBefore.get();
						Optional<RastNode> optNode = findByFullName(subtypeBefore, fullName(addedMember));
						if (optNode.isPresent() && optNode.get().hasStereotype(Stereotype.TYPE_MEMBER) && removed(optNode.get())) {
							relationships.add(new Relationship(RelationshipType.PULL_UP, optNode.get(), addedMember));
							subtypesWithPulledUpMembers.add(subtypeBefore);
						}
					}
				}
			}
			if (!subtypesWithPulledUpMembers.isEmpty() && added(supertypeAfter)) {
				for (RastNode subtype : subtypesWithPulledUpMembers) {
					relationships.add(new Relationship(RelationshipType.EXTRACT_SUPER, subtype, supertypeAfter));
				}
			}
			return relationships;
		}
		
		private void addMatch(Relationship relationship) {
			RastNode nBefore = relationship.getNodeBefore();
			RastNode nAfter = relationship.getNodeAfter();
			if (removed(nBefore) && added(nAfter)) {
				apply(relationship);
				mapBeforeToAfter.put(nBefore, nAfter);
				mapAfterToBefore.put(nAfter, nBefore);
				matchExactChildren(nBefore, nAfter);
			}
		}
		

		private T sourceRep(RastNode n) {
			return srMap.get(n);
		}
		
		private int depth(RastNode n) {
			return depthMap.get(n);
		}
		
		private Optional<RastNode> matchingNodeBefore(RastNode n2) {
			return Optional.ofNullable(mapAfterToBefore.get(n2));
		}
		
		private Optional<RastNode> matchingNodeAfter(RastNode n1) {
			return Optional.ofNullable(mapBeforeToAfter.get(n1));
		}
		
		private boolean sameLocation(RastNode n1, RastNode n2) {
			if (n1.getParent().isPresent() && n2.getParent().isPresent()) {
				return matchingNodeAfter(n1.getParent().get()).equals(n2.getParent());
			} else if (!n1.getParent().isPresent() && !n1.getParent().isPresent()) {
				return sameNamespace(n1, n2);
			} else {
				return false;
			}
		}
		
		private boolean removed(RastNode n) {
			return this.removed.contains(n);
		}
		
		private boolean added(RastNode n) {
			return this.added.contains(n);
		}
		
		private List<RastNode> children(HasChildrenNodes nodeWithChildren, Predicate<RastNode> predicate) {
			return nodeWithChildren.getNodes().stream().filter(predicate).collect(Collectors.toList());
		}

		public void recordSimilarity(RastNode n1, RastNode n2) {
			if (!n1.hasStereotype(Stereotype.ABSTRACT) && !n2.hasStereotype(Stereotype.ABSTRACT)) {
				double similarity = srb.similarity(sourceRep(n1), sourceRep(n2));
				/*
				if (Double.isNaN(similarity)) {
					String sourceN1 = retrieveSourceCodeBefore(n1);
					String sourceN2 = retrieveSourceCodeAfter(n2);
					System.out.println("NaN");
					srb.similarity(sourceRep(n1), sourceRep(n2));
				}
				*/
				similaritySame.add(similarity);
			}
		}
		
		public void reportSimilarity() {
			if (similaritySame.isEmpty()) {
				System.out.println("Empty");
			} else {
				Collections.sort(similaritySame);
				double min = Statistics.min(similaritySame);
				double q1 = Statistics.q1(similaritySame);
				double q2 = Statistics.median(similaritySame);
				double q3 = Statistics.q3(similaritySame);
				double max = Statistics.max(similaritySame);
				System.out.println(String.format("Size: %d, [%.3f, %.3f, %.3f, %.3f, %.3f]", similaritySame.size(), min, q1, q2, q3, max));
			}
		}
		
	}

}
