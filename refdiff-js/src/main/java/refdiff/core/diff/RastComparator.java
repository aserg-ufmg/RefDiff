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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import refdiff.core.diff.similarity.SourceRepresentationBuilder;
import refdiff.core.diff.similarity.TfIdfSourceRepresentationBuilder;
import refdiff.core.io.SourceFile;
import refdiff.core.rast.HasChildrenNodes;
import refdiff.core.rast.Location;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastNodeRelationshipType;
import refdiff.core.rast.Stereotype;
import refdiff.core.util.Statistics;
import refdiff.parsers.RastParser;
import refdiff.parsers.SourceTokenizer;

public class RastComparator {
	
	private final RastParser parser;
	private final SourceTokenizer tokenizer;
	
	public RastComparator(RastParser parser, SourceTokenizer tokenizer) {
		this.parser = parser;
		this.tokenizer = tokenizer;
	}
	
	public RastDiff compare(List<SourceFile> filesBefore, List<SourceFile> filesAfter) throws Exception {
		DiffBuilder<?> diffBuilder = new DiffBuilder<>(new TfIdfSourceRepresentationBuilder(), filesBefore, filesAfter);
		return diffBuilder.computeDiff();
	}
	
	private class DiffBuilder<T> {
		private final SourceRepresentationBuilder<T> srb;
		private RastDiff diff;
		private RastRootHelper<T> before;
		private RastRootHelper<T> after;
		private Set<RastNode> removed;
		private Set<RastNode> added;
		private ArrayList<Double> similaritySame = new ArrayList<>();
		private ThresholdsProvider threshold = new ThresholdsProvider();
		
		private final Map<RastNode, T> srMap = new HashMap<>();
		private final Map<RastNode, T> srBodyMap = new HashMap<>();
		private final Map<RastNode, RastNode> mapBeforeToAfter = new HashMap<>();
		private final Map<RastNode, RastNode> mapAfterToBefore = new HashMap<>();
		private final Map<RastNode, Integer> depthMap = new HashMap<>();
		private final Map<String, SourceFile> fileMapBefore = new HashMap<>();
		private final Map<String, SourceFile> fileMapAfter = new HashMap<>();
		
		DiffBuilder(SourceRepresentationBuilder<T> srb, List<SourceFile> filesBefore, List<SourceFile> filesAfter) throws Exception {
			this.srb = srb;
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
				computeSourceRepresentation(fileMapBefore, node, true);
				depthMap.put(node, depth);
			});
			this.added = new HashSet<>();
			this.diff.getAfter().forEachNode((node, depth) -> {
				this.added.add(node);
				computeSourceRepresentation(fileMapAfter, node, false);
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
		
		private void computeSourceRepresentation(Map<String, SourceFile> fileMap, RastNode node, boolean isBefore) {
			srMap.put(node, srb.buildForNode(node, isBefore, tokenizer.tokenize(retrieveSourceCode(fileMap, node, false))));
			srBodyMap.put(node, srb.buildForFragment(tokenizer.tokenize(retrieveSourceCode(fileMap, node, true))));
		}
		
		private String retrieveSourceCode(Map<String, SourceFile> fileMap, RastNode node, boolean bodyOnly) {
			try {
				Location location = node.getLocation();
				String sourceCode = fileMap.get(location.getFile()).getContent();
				if (bodyOnly) {
					return sourceCode.substring(location.getBodyBegin(), location.getBodyEnd());
				} else {
					return sourceCode.substring(location.getBegin(), location.getEnd());
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		RastDiff computeDiff() {
			matchExactChildren(diff.getBefore(), diff.getAfter());
			Collections.sort(similaritySame);
			adjustThreshold();
			matchPullUpAndPushDownMembers();
			matchExtractSuper();
			matchMovesOrRenames();
			matchExtract();
			matchInline();
			return diff;
		}
		
		private void adjustThreshold() {
			ArrayList<Double> similaritySame = new ArrayList<>();
			for (Entry<RastNode, RastNode> entry : mapBeforeToAfter.entrySet()) {
				RastNode n1 = entry.getKey();
				RastNode n2 = entry.getValue();
				if (!n1.hasStereotype(Stereotype.ABSTRACT) && !n2.hasStereotype(Stereotype.ABSTRACT)) {
					double similarity = srb.similarity(sourceRep(n1), sourceRep(n2));
					if (similarity < 1.0) {
						similaritySame.add(similarity);
					}
				}
			}/*
			ArrayList<Double> similarityNotSame = new ArrayList<>();
			Set<RastNode> nodesAfter = mapAfterToBefore.keySet();
			for (Entry<RastNode, RastNode> entry : mapBeforeToAfter.entrySet()) {
				RastNode n1 = entry.getKey();
				for (RastNode n2 : nodesAfter) {
					if (n2 != entry.getValue() && n1.getType().equals(n2.getType()) && sameLocation(n1, n2)) {
						double similarity = srb.similarity(sourceRep(n1), sourceRep(n2));
						similarityNotSame.add(similarity);
					}
				}
			}*/
			
			threshold.adjustTo(similaritySame);
			reportSimilarity(similaritySame);
			//reportSimilarity(similarityNotSame);
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
						if (score > threshold.getValue()) {
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
						addMatch(new Relationship(RelationshipType.CHANGE_SIGNATURE, n1, n2, candidate.getScore()));
					} else {
						addMatch(new Relationship(RelationshipType.RENAME, n1, n2, candidate.getScore()));
					}
				} else {
					if (sameSignature(n1, n2)) {
						addMatch(new Relationship(RelationshipType.MOVE, n1, n2, candidate.getScore()));
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
							double score = srb.partialSimilarity(bodySourceRep(n2), removedSource);
							if (score > threshold.getValue()) {
								relationships.add(new Relationship(RelationshipType.EXTRACT, n1, n2, score));
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
							T sourceN1 = bodySourceRep(n1);
							T sourceN1Caller = sourceRep(n1Caller);
							T sourceN1CallerAfter = sourceRep(n2);
							T addedCode = srb.minus(sourceN1CallerAfter, sourceN1Caller);
							double score = srb.partialSimilarity(sourceN1, addedCode);
							if (score > threshold.getValue()) {
								relationships.add(new Relationship(RelationshipType.INLINE, n1, n2, score));
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
			for (RastNode subtype : after.findReverseRelationships(RastNodeRelationshipType.SUBTYPE, nAfter)) {
				for (RastNode n1Member : children(nBefore, m -> m.hasStereotype(Stereotype.TYPE_MEMBER))) {
					Optional<RastNode> maybeN2Member = findByFullName(subtype, fullName(n1Member));
					if (maybeN2Member.isPresent() && maybeN2Member.get().hasStereotype(Stereotype.TYPE_MEMBER) && added(maybeN2Member.get())) {
						if (removed(n1Member)) {
							relationships.add(new Relationship(RelationshipType.PUSH_DOWN, n1Member, maybeN2Member.get()));
						} else {
							Optional<RastNode> maybeN1MemberAfter = matchingNodeAfter(n1Member);
							if (maybeN1MemberAfter.isPresent() && !n1Member.hasStereotype(Stereotype.ABSTRACT) && maybeN1MemberAfter.get().hasStereotype(Stereotype.ABSTRACT)) {
								relationships.add(new Relationship(RelationshipType.PUSH_DOWN_IMPL, n1Member, maybeN2Member.get()));
							}
						}
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
						if (optNode.isPresent() && optNode.get().hasStereotype(Stereotype.TYPE_MEMBER)) {
							if (removed(optNode.get())) {
								relationships.add(new Relationship(RelationshipType.PULL_UP, optNode.get(), addedMember));
								subtypesWithPulledUpMembers.add(subtypeBefore);
							} else if (addedMember.hasStereotype(Stereotype.ABSTRACT)) {
								relationships.add(new Relationship(RelationshipType.PULL_UP_SIGNATURE, optNode.get(), addedMember));
								subtypesWithPulledUpMembers.add(subtypeBefore);
							}
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
		
		private T bodySourceRep(RastNode n) {
			return srBodyMap.get(n);
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

		private void reportSimilarity(List<Double> similaritySame) {
			if (similaritySame.size() <= 1) {
				System.out.println(similaritySame.size() + " values");
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
