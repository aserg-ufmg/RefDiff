package refdiff.core.diff;

import static refdiff.core.diff.RastRootHelper.*;

import java.util.ArrayList;
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
		long start = System.currentTimeMillis();
		DiffBuilder<?> diffBuilder = new DiffBuilder<>(new TfIdfSourceRepresentationBuilder(), sourcesBefore, sourcesAfter, monitor);
		RastDiff diff = diffBuilder.computeDiff();
		long end = System.currentTimeMillis();
		monitor.afterCompare(end - start);
		return diff;
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
				addRelationship(r);
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
			findMatchesById();
			findMatchesBySimilarity(false);
			findMatchesBySimilarity(true);
			findMatchesByChildren();
			createRelationshipsForMatchings();
			findPullPushDownAbstract();
			findAdditionalPullUpAndPushDown();
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
		
//		@SuppressWarnings("unused")
//		private void adjustThreshold() {
//			ArrayList<Double> similaritySame = new ArrayList<>();
//			for (Entry<RastNode, RastNode> entry : mapBeforeToAfter.entrySet()) {
//				RastNode n1 = entry.getKey();
//				RastNode n2 = entry.getValue();
//				if (!n1.hasStereotype(Stereotype.ABSTRACT) && !n2.hasStereotype(Stereotype.ABSTRACT)) {
//					double similarity = srb.similarity(before.sourceRep(n1), after.sourceRep(n2));
//					if (similarity < 1.0) {
//						similaritySame.add(similarity);
//					}
//				}
//			}
//			
//			threshold.adjustTo(similaritySame);
//			//reportSimilarity(similaritySame);
//			//reportSimilarity(similarityNotSame);
//		}
		
		private void findMatchesBySimilarity(boolean onlySafe) {
			List<PotentialMatch> candidates = new ArrayList<>();
			for (RastNode n1 : removed) {
				for (RastNode n2 : added) {
					if (sameType(n1, n2) && !anonymous(n1) && !anonymous(n2)) {
						if (!onlySafe || (sameName(n1, n2) || sameLocation(n1, n2))) {
							Optional<RelationshipType> optRelationshipType = findRelationshipForCandidate(n1, n2);
							if (optRelationshipType.isPresent()) {
								RelationshipType type = optRelationshipType.get();
								double score = srb.similarity(before.sourceRep(n1), after.sourceRep(n2));
								if (type.isById() || score > threshold.getValue()) {
									PotentialMatch candidate = new PotentialMatch(n1, n2, Math.max(before.depth(n1), after.depth(n2)), score);
									candidates.add(candidate);
								} else {
									monitor.reportDiscardedMatch(n1, n2, score);
								}
							}
						}
						
					}
				}
			}
			Collections.sort(candidates);
			for (PotentialMatch candidate : candidates) {
				addMatch(candidate.getNodeBefore(), candidate.getNodeAfter());
			}
		}
		
		private void findMatchesByChildren() {
			List<PotentialMatch> candidates = new ArrayList<>();
			for (RastNode n1 : removed) {
				for (RastNode n2 : added) {
					if (sameType(n1, n2) && !anonymous(n1) && !anonymous(n2) && existsMatchingChild(n1, n2)) {
						PotentialMatch candidate = new PotentialMatch(n1, n2, Math.max(before.depth(n1), after.depth(n2)), 1.0);
						if (sameName(n1, n2)) {
							candidates.add(candidate);
						}
					}
				}
			}
			for (PotentialMatch candidate : candidates) {
				addMatch(candidate.getNodeBefore(), candidate.getNodeAfter());
			}
		}
		
		private Optional<RelationshipType> findRelationshipForCandidate(RastNode n1, RastNode n2) {
			if (sameLocation(n1, n2) && sameSignature(n1, n2)) {
				return Optional.of(RelationshipType.SAME);
			}
			else if (sameSignature(n1, n2) && n1.hasStereotype(Stereotype.TYPE_MEMBER) && after.hasRelationship(RastNodeRelationshipType.SUBTYPE, matchingNodeAfter(n1.getParent()), n2.getParent())) {
				return Optional.of(RelationshipType.PULL_UP);
			} else if (sameSignature(n1, n2) && n1.hasStereotype(Stereotype.TYPE_MEMBER) && after.hasRelationship(RastNodeRelationshipType.SUBTYPE, n2.getParent(), matchingNodeAfter(n1.getParent()))) {
				return Optional.of(RelationshipType.PUSH_DOWN);
			}
			else if (sameLocation(n1, n2)) {
				if (sameName(n1, n2)) {
					return Optional.of(RelationshipType.CHANGE_SIGNATURE);
				} else {
					return Optional.of(RelationshipType.RENAME);
				}
			} else if (!n1.hasStereotype(Stereotype.TYPE_CONSTRUCTOR) && !n2.hasStereotype(Stereotype.TYPE_CONSTRUCTOR)) {
				if (sameSignature(n1, n2) || sameName(n1, n2)) {
					if (sameRootNode(n1, n2)) {
						return Optional.of(RelationshipType.INTERNAL_MOVE);
					} else {
						return Optional.of(RelationshipType.MOVE);
					}
				} else {
					if (sameRootNode(n1, n2)) {
						return Optional.of(RelationshipType.INTERNAL_MOVE_RENAME);
					} else {
						return Optional.of(RelationshipType.MOVE_RENAME);
					}
				}
			}
			return Optional.empty();
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
		
		private void findMatchesById() {
			findMatchesById(diff.getBefore(), diff.getAfter());
		}

		private void findMatchesById(HasChildrenNodes parentBefore, HasChildrenNodes parentAfter) {
			for (RastNode n1 : children(parentBefore, this::removed)) {
				for (RastNode n2 : children(parentAfter, this::added)) {
					if (sameNamespace(n1, n2) && sameSignature(n1, n2)) {
						addMatch(n1, n2);
					}
				}
			}
		}

		private void findAdditionalPullUpAndPushDown() {
			List<Relationship> relationships = new ArrayList<>();
			
			diff.getRelationships()
				.stream()
				.filter(r -> r.getType().equals(RelationshipType.PULL_UP))
				.forEach(r -> {
					RastNode n2 = r.getNodeAfter();
					RastNode supertypeAfter = n2.getParent().get();
					for (RastNode n1ParentAfter : after.findReverseRelationships(RastNodeRelationshipType.SUBTYPE, supertypeAfter)) {
						Optional<RastNode> n1ParentBefore = matchingNodeBefore(n1ParentAfter);
						if (n1ParentBefore.isPresent()) {
							for (RastNode n1 : children(n1ParentBefore.get(), this::removed)) {
								if (n1.hasStereotype(Stereotype.TYPE_MEMBER) && sameSignature(n1, n2)) {
									relationships.add(new Relationship(RelationshipType.PULL_UP, n1, n2));
								}
							}
						}
					}
				});
			
			diff.getRelationships()
				.stream()
				.filter(r -> r.getType().equals(RelationshipType.PUSH_DOWN))
				.forEach(r -> {
					RastNode n1 = r.getNodeBefore();
					RastNode n1ParentAfter = matchingNodeAfter(n1.getParent()).get();
					for (RastNode n2ParentAfter : after.findReverseRelationships(RastNodeRelationshipType.SUBTYPE, n1ParentAfter)) {
						for (RastNode n2 : children(n2ParentAfter, this::added)) {
							if (n2.hasStereotype(Stereotype.TYPE_MEMBER) && sameSignature(n1, n2)) {
								relationships.add(new Relationship(RelationshipType.PUSH_DOWN, n1, n2));
							}
						}
					}
				});
			
			for (Relationship relationship : relationships) {
				diff.addRelationships(relationship);
			}
		}

		private void findPullUpAndPushOnMatch(RastNode parentBefore, RastNode parentAfter) {
			// Find Pull Up
			{
				RastNode subtypeAfter = (RastNode) parentAfter;
				for (RastNode supertypeAfter : after.findRelationships(RastNodeRelationshipType.SUBTYPE, subtypeAfter)) {
					for (RastNode n1 : parentBefore.getNodes()) {
						for (RastNode n2 : children(supertypeAfter, this::added)) {
							if (n1.hasStereotype(Stereotype.TYPE_MEMBER) && sameSignature(n1, n2)) {
								Optional<RastNode> optN1After = matchingNodeAfter(n1);
								if (optN1After.isPresent()) {
									if (n2.hasStereotype(Stereotype.ABSTRACT)) {
										addRelationship(new Relationship(RelationshipType.PULL_UP_SIGNATURE, n1, n2));
									}
								}
							}
						}
					}
				}
			}
			// Find Pull Up from removed
			/*
			{
				RastNode supertypeBefore = (RastNode) parentBefore;
				for (RastNode subtypeBefore : before.findReverseRelationships(RastNodeRelationshipType.SUBTYPE, supertypeBefore)) {
					for (RastNode n1 : subtypeBefore.getNodes()) {
						for (RastNode n2 : parentAfter.getNodes()) {
							if (n1.hasStereotype(Stereotype.TYPE_MEMBER) && sameSignature(n1, n2)) {
								Optional<RastNode> optN1After = matchingNodeAfter(n1);
								if (!optN1After.isPresent()) {
									apply(new Relationship(RelationshipType.PULL_UP, n1, n2));
								} else {
									if (n2.hasStereotype(Stereotype.ABSTRACT)) {
										apply(new Relationship(RelationshipType.PULL_UP_SIGNATURE, n1, n2));
									}
								}
							}
						}
					}
				}
			}
			*/
			
			// Find Push Down
			{
				RastNode supertypeAfter = (RastNode) parentAfter;
				for (RastNode subtypeAfter : after.findReverseRelationships(RastNodeRelationshipType.SUBTYPE, supertypeAfter)) {
					for (RastNode n1 : parentBefore.getNodes()) {
						for (RastNode n2 : children(subtypeAfter, this::added)) {
							if (n1.hasStereotype(Stereotype.TYPE_MEMBER) && sameSignature(n1, n2)) {
								Optional<RastNode> optN1After = matchingNodeAfter(n1);
								if (optN1After.isPresent()) {
									RastNode n1After = optN1After.get();
									if (!n1.hasStereotype(Stereotype.ABSTRACT) && n1After.hasStereotype(Stereotype.ABSTRACT) && !n2.hasStereotype(Stereotype.ABSTRACT)) {
										addRelationship(new Relationship(RelationshipType.PUSH_DOWN_IMPL, n1, n2));
									}
								}
							}
						}
					}
				}
			}
			// Find Push Down from removed
			/*
			{
				RastNode subtypeBefore = (RastNode) parentBefore;
				for (RastNode supertypeBefore : before.findRelationships(RastNodeRelationshipType.SUBTYPE, subtypeBefore)) {
					for (RastNode n1 : supertypeBefore.getNodes()) {
						for (RastNode n2 : parentAfter.getNodes()) {
							if (n1.hasStereotype(Stereotype.TYPE_MEMBER) && sameSignature(n1, n2)) {
								Optional<RastNode> optN1After = matchingNodeAfter(n1);
								if (!optN1After.isPresent()) {
									apply(new Relationship(RelationshipType.PUSH_DOWN, n1, n2));
								} else {
									RastNode n1After = optN1After.get();
									if (n1After.hasStereotype(Stereotype.ABSTRACT) && !n2.hasStereotype(Stereotype.ABSTRACT)) {
										apply(new Relationship(RelationshipType.PUSH_DOWN_IMPL, n1, n2));
									}
								}
							}
						}
					}
				}
			}
			*/
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
		
		private void addMatch(RastNode nBefore, RastNode nAfter) {
			if (mapBeforeToAfter.containsKey(nBefore) || mapAfterToBefore.containsKey(nAfter)) {
				monitor.reportDiscardedConflictingMatch(nBefore, nAfter);
			} else {
				mapBeforeToAfter.put(nBefore, nAfter);
				mapAfterToBefore.put(nAfter, nBefore);
				removed.remove(nBefore);
				added.remove(nAfter);
				findMatchesById(nBefore, nAfter);
			}
		}
		
		private void createRelationshipsForMatchings() {
			for (Entry<RastNode, RastNode> entry : mapBeforeToAfter.entrySet()) {
				RastNode n1 = entry.getKey();
				RastNode n2 = entry.getValue();
				Optional<RelationshipType> type = findRelationshipForCandidate(n1, n2);
				if (type.isPresent()) {
					diff.addRelationships(new Relationship(type.get(), n1, n2));
				}
			}
		}

		private void findPullPushDownAbstract() {
			for (Entry<RastNode, RastNode> entry : mapBeforeToAfter.entrySet()) {
				RastNode n1 = entry.getKey();
				RastNode n2 = entry.getValue();
				findPullUpAndPushOnMatch(n1, n2);
			}
		}
		
		private void addRelationship(Relationship relationship) {
			RastNode nBefore = relationship.getNodeBefore();
			RastNode nAfter = relationship.getNodeAfter();
			RelationshipType type = relationship.getType();
			
			if (type.isUnmarkRemoved() && !removed(nBefore) || type.isUnmarkAdded() && !added(nAfter)) {
				monitor.reportDiscardedConflictingMatch(nBefore, nAfter);
			} else {
				diff.addRelationships(relationship);
			}
		}
		
		private Optional<RastNode> matchingNodeBefore(RastNode n2) {
			return Optional.ofNullable(mapAfterToBefore.get(n2));
		}
		
		private Optional<RastNode> matchingNodeAfter(RastNode n1) {
			return Optional.ofNullable(mapBeforeToAfter.get(n1));
		}
		
		private Optional<RastNode> matchingNodeAfter(Optional<RastNode> optN1) {
			if (optN1.isPresent()) {
				return matchingNodeAfter(optN1.get());
			}
			return Optional.empty();
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
		
		private boolean childrenMatch(RastNode n1, RastNode n2) {
			boolean existsChildMatch = false;
			if (n1.getNodes().size() == 0 || n2.getNodes().size() == 0) {
				return false;
			}
			for (RastNode n1Child : n1.getNodes()) {
				Optional<RastNode> maybeN2Child = matchingNodeAfter(n1Child);
				if (maybeN2Child.isPresent()) {
					if (childOf(maybeN2Child.get(), n2)) {
						existsChildMatch = true;
					} else {
						return false;
					}
				}
			}
			for (RastNode n2Child : n2.getNodes()) {
				Optional<RastNode> maybeN1Child = matchingNodeBefore(n2Child);
				if (maybeN1Child.isPresent() && !childOf(maybeN1Child.get(), n1)) {
					return false;
				}
			}
			return existsChildMatch;
		}
		
		private boolean existsMatchingChild(RastNode n1, RastNode n2) {
			if (n1.getNodes().size() == 0 || n2.getNodes().size() == 0) {
				return false;
			}
			for (RastNode n1Child : n1.getNodes()) {
				Optional<RastNode> maybeN2Child = matchingNodeAfter(n1Child);
				if (maybeN2Child.isPresent()) {
					if (childOf(maybeN2Child.get(), n2)) {
						return true;
					}
				}
			}
			return false;
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
