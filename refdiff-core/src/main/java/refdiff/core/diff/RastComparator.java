package refdiff.core.diff;

import static refdiff.core.diff.RastRootHelper.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
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
import refdiff.core.io.SourceFileSet;
import refdiff.core.rast.HasChildrenNodes;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastNodeRelationshipType;
import refdiff.core.rast.RastRoot;
import refdiff.core.rast.Stereotype;
import refdiff.parsers.RastParser;

public class RastComparator {
	
	private final RastParser parser;
	
	public RastComparator(RastParser parser) {
		this.parser = parser;
	}
	
	public RastDiff compare(SourceFileSet sourcesBefore, SourceFileSet sourcesAfter) throws Exception {
		return compare(sourcesBefore, sourcesAfter, new RastComparatorMonitor() {});
	}
	
	public RastDiff compare(SourceFileSet sourcesBefore, SourceFileSet sourcesAfter, RastComparatorMonitor monitor) throws Exception {
		DiffBuilder<?> diffBuilder = new DiffBuilder<>(new TfIdfSourceRepresentationBuilder(), sourcesBefore, sourcesAfter, monitor);
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
		private RastComparatorMonitor monitor;
		
		private final Map<RastNode, RastNode> mapBeforeToAfter = new HashMap<>();
		private final Map<RastNode, RastNode> mapAfterToBefore = new HashMap<>();
		
		DiffBuilder(SourceRepresentationBuilder<T> srb, SourceFileSet sourcesBefore, SourceFileSet sourcesAfter, RastComparatorMonitor monitor) throws Exception {
			this.srb = srb;
			RastRoot rastRootBefore = parser.parse(sourcesBefore);
			RastRoot rastRootAfter = parser.parse(sourcesAfter);
			this.diff = new RastDiff(rastRootBefore, rastRootAfter);
			this.before = new RastRootHelper<>(this.diff.getBefore(), sourcesBefore, srb, true);
			this.after = new RastRootHelper<>(this.diff.getAfter(), sourcesAfter, srb, false);
			this.removed = new HashSet<>();
			this.monitor = monitor;
			
			Map<String, String> fileMapBefore = new HashMap<>();
			Map<String, String> fileMapAfter = new HashMap<>();
			
			for (SourceFile fileBefore : sourcesBefore.getSourceFiles()) {
				fileMapBefore.put(fileBefore.getPath(), sourcesBefore.readContent(fileBefore));
			}
			for (SourceFile fileAfter : sourcesAfter.getSourceFiles()) {
				fileMapAfter.put(fileAfter.getPath(), sourcesAfter.readContent(fileAfter));
			}
			this.diff.getBefore().forEachNode((node, depth) -> {
				this.removed.add(node);
			});
			this.added = new HashSet<>();
			this.diff.getAfter().forEachNode((node, depth) -> {
				this.added.add(node);
			});
			monitor.beforeCompare(before, after);
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
		
		private T getTokensToUseNode(RastNode node) {
			return srb.buildForFragment(Collections.emptyList());
//			List<String> tokens = new ArrayList<>();
//			tokens.add(node.getSimpleName());
//			tokens.add("(");
//			tokens.add(")");
//			if (node.getParameters() != null) {
//				for (int i = 1; i < node.getParameters().size(); i++) {
//					tokens.add(",");
//				}
//			}
//			return srb.buildForFragment(tokens);
		}
		
		RastDiff computeDiff() {
			computeSourceRepresentationForRemovedAndAdded();
			matchExactChildren(diff.getBefore(), diff.getAfter());
			Collections.sort(similaritySame);
			//adjustThreshold();
			matchMovesOrRenames(false, false);
			matchMovesOrRenames(false, true);
			matchPullUpAndPushDownMembers();
			matchPullUpToAdded();
			matchMovesOrRenames(true, false);
			matchMovesOrRenames(true, true);
			inferExtractSuper();
			matchExtract();
			matchInline();
			return diff;
		}
		
		private void computeSourceRepresentationForRemovedAndAdded() {
			for (RastNode node : removed) {
				before.computeSourceRepresentation(node);
			}
			for (RastNode node : added) {
				after.computeSourceRepresentation(node);
			}
		}
		
		@SuppressWarnings("unused")
		private void adjustThreshold() {
			ArrayList<Double> similaritySame = new ArrayList<>();
			for (Entry<RastNode, RastNode> entry : mapBeforeToAfter.entrySet()) {
				RastNode n1 = entry.getKey();
				RastNode n2 = entry.getValue();
				if (!n1.hasStereotype(Stereotype.ABSTRACT) && !n2.hasStereotype(Stereotype.ABSTRACT)) {
					double similarity = srb.similarity(before.sourceRep(n1), after.sourceRep(n2));
					if (similarity < 1.0) {
						similaritySame.add(similarity);
					}
				}
			}
			
			threshold.adjustTo(similaritySame);
			//reportSimilarity(similaritySame);
			//reportSimilarity(similarityNotSame);
		}
		
		private void matchPullUpToAdded() {
			Set<Relationship> relationships = new HashSet<>();
			for (RastNode potentialSupertype : added) {
				relationships.addAll(findPullUpMembers(potentialSupertype));
			}
			apply(relationships);
		}

		private void matchMovesOrRenames(boolean includeLeaves, boolean includeNonLocal) {
			List<PotentialMatch> candidates = new ArrayList<>();
			for (RastNode n1 : removed) {
				for (RastNode n2 : added) {
					if (sameType(n1, n2) && !anonymous(n1) && !anonymous(n2)) {
						if (!includeNonLocal && !sameLocation(n1, n2)) {
							continue;
						}
						if (!includeLeaves && !(!leaf(n1) && !leaf(n2))) {
							continue;
						}
						double score = srb.similarity(before.sourceRep(n1), after.sourceRep(n2));
						if (score > threshold.getValue()) {
							PotentialMatch candidate = new PotentialMatch(n1, n2, Math.max(before.depth(n1), after.depth(n2)), score);
							candidates.add(candidate);
						} else {
							monitor.reportDiscardedMatch(n1, n2, score);
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
				} else if (!n1.hasStereotype(Stereotype.TYPE_CONSTRUCTOR) && !n2.hasStereotype(Stereotype.TYPE_CONSTRUCTOR)) {
					if (sameSignature(n1, n2) || sameName(n1, n2)) {
						if (sameRootNode(n1, n2)) {
							addMatch(new Relationship(RelationshipType.INTERNAL_MOVE, n1, n2, candidate.getScore()));
						} else {
							addMatch(new Relationship(RelationshipType.MOVE, n1, n2, candidate.getScore()));
						}
					} else {
						if (sameRootNode(n1, n2)) {
							addMatch(new Relationship(RelationshipType.INTERNAL_MOVE_RENAME, n1, n2, candidate.getScore()));
						} else {
							addMatch(new Relationship(RelationshipType.MOVE_RENAME, n1, n2, candidate.getScore()));
						}
					}
				}
			}
		}
		
		private void matchExtract() {
			Set<Relationship> relationships = new HashSet<>();
			for (RastNode n2 : added) {
//				if (n2.getLocalName().equals("has(NSString)") && n2.getParent().isPresent() && n2.getParent().get().getLocalName().equals("CBConnectPeripheralOptions")) {
//					n2.getLocalName();
//				}
				for (RastNode n1After : after.findReverseRelationships(RastNodeRelationshipType.USE, n2)) {
					Optional<RastNode> optMatchingNode = matchingNodeBefore(n1After);
					if (optMatchingNode.isPresent()) {
						RastNode n1 = optMatchingNode.get();
						if (sameType(n1, n2)/* && !n2.hasStereotype(Stereotype.FIELD_ACCESSOR) && !n2.hasStereotype(Stereotype.FIELD_MUTATOR)*/) {
							T sourceN1After = after.sourceRep(n1After);
							T sourceN1Before = before.sourceRep(n1);
							T removedSource = srb.minus(sourceN1Before, srb.minus(sourceN1After, getTokensToUseNode(n2)));
							T bodySourceN2 = after.bodySourceRep(n2);
							double score1 = srb.partialSimilarity(bodySourceN2, removedSource);
							double score2 = srb.partialSimilarity(removedSource, bodySourceN2);
							double score = Math.max(score1, score2);
							if (score > threshold.getValue()) {
								if (sameLocation(n1, n2)) {
									relationships.add(new Relationship(RelationshipType.EXTRACT, n1, n2, score));
								} else {
									relationships.add(new Relationship(RelationshipType.EXTRACT_MOVE, n1, n2, score));
								}
							} else {
								monitor.reportDiscardedExtract(n1, n2, score);
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
						if (sameType(n1, n2)/* && !n1.hasStereotype(Stereotype.FIELD_ACCESSOR) && !n1.hasStereotype(Stereotype.FIELD_MUTATOR)*/) {
							T sourceN1 = before.bodySourceRep(n1);
							T sourceN1Caller = before.sourceRep(n1Caller);
							T sourceN1CallerAfter = after.sourceRep(n2);
							T addedCode = srb.minus(sourceN1CallerAfter, srb.minus(sourceN1Caller, getTokensToUseNode(n1)));
							double score = srb.partialSimilarity(sourceN1, addedCode);
							//double score1 = srb.partialSimilarity(sourceN1, addedCode);
							//double score2 = srb.partialSimilarity(addedCode, sourceN1);
							//double score = Math.max(score1, score2);
							if (score > threshold.getValue()) {
								relationships.add(new Relationship(RelationshipType.INLINE, n1, n2, score));
							} else {
								monitor.reportDiscardedInline(n1, n2, score);
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
				for (RastNode n1Member : nBefore.getNodes()) {
					Optional<RastNode> maybeN2Member = findByFullName(subtype, fullName(n1Member));
					if (maybeN2Member.isPresent() && n1Member.hasStereotype(Stereotype.TYPE_MEMBER) && maybeN2Member.get().hasStereotype(Stereotype.TYPE_MEMBER) && added(maybeN2Member.get())) {
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
			/*
			if (!subtypesWithPulledUpMembers.isEmpty() && added(supertypeAfter)) {
				for (RastNode subtype : subtypesWithPulledUpMembers) {
					relationships.add(new Relationship(RelationshipType.EXTRACT_SUPER, subtype, supertypeAfter));
				}
			}
			*/
			return relationships;
		}
		
		private void inferExtractSuper() {
			Set<Relationship> relationships = new HashSet<>();
			EnumSet<RelationshipType> pullUpLike = EnumSet.of(RelationshipType.PULL_UP, RelationshipType.PULL_UP_SIGNATURE);
			
			diff.getRelationships()
				.stream()
				.filter(r -> pullUpLike.contains(r.getType()))
				.forEach(r -> {
					RastNode supertypeAfter = r.getNodeAfter().getParent().get();
					RastNode subtypeBefore = r.getNodeBefore().getParent().get();
					if (added(supertypeAfter)) {
						relationships.add(new Relationship(RelationshipType.EXTRACT_SUPER, subtypeBefore, supertypeAfter));
					}
				});
			
			apply(relationships);
		} 
		
		private void addMatch(Relationship relationship) {
			RastNode nBefore = relationship.getNodeBefore();
			RastNode nAfter = relationship.getNodeAfter();
			if (removed(nBefore) && added(nAfter)) {
				apply(relationship);
				mapBeforeToAfter.put(nBefore, nAfter);
				mapAfterToBefore.put(nAfter, nBefore);
				matchExactChildren(nBefore, nAfter);
			} else {
				monitor.reportDiscardedConflictingMatch(nBefore, nAfter);
			}
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
		
		private boolean sameRootNode(RastNode n1, RastNode n2) {
			Optional<RastNode> n1Root = n1.getRootParent();
			Optional<RastNode> n2Root = n2.getRootParent();
			if (n1Root.isPresent() && n2Root.isPresent()) {
				return matchingNodeAfter(n1Root.get()).equals(n2Root);
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

		/*
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
		*/
	}

	public RastParser getParser() {
		return parser;
	}

}
