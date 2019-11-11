package refdiff.core.diff;

import static refdiff.core.diff.CstRootHelper.*;

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
import refdiff.core.cst.HasChildrenNodes;
import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstNodeRelationshipType;
import refdiff.core.cst.CstRoot;
import refdiff.core.cst.Stereotype;
import refdiff.core.util.PairBeforeAfter;
import refdiff.parsers.LanguagePlugin;

public class CstComparator {
	
	private final LanguagePlugin languagePlugin;
	
	public CstComparator(LanguagePlugin parser) {
		this.languagePlugin = parser;
	}
	
	public CstDiff compare(PairBeforeAfter<SourceFileSet> beforeAndAfter) {
		return compare(beforeAndAfter.getBefore(), beforeAndAfter.getAfter(), new CstComparatorMonitor() {});
	}
	
	public CstDiff compare(SourceFileSet sourcesBefore, SourceFileSet sourcesAfter) {
		return compare(sourcesBefore, sourcesAfter, new CstComparatorMonitor() {});
	}
	
	public CstDiff compare(SourceFileSet sourcesBefore, SourceFileSet sourcesAfter, CstComparatorMonitor monitor) {
		try {
			long start = System.currentTimeMillis();
			DiffBuilder<?> diffBuilder = new DiffBuilder<>(new TfIdfSourceRepresentationBuilder(), sourcesBefore, sourcesAfter, monitor);
			CstDiff diff = diffBuilder.computeDiff();
			long end = System.currentTimeMillis();
			monitor.afterCompare(end - start, diffBuilder);
			return diff;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public class DiffBuilder<T> {
		private final SourceRepresentationBuilder<T> srb;
		private CstDiff diff;
		private CstRootHelper<T> before;
		private CstRootHelper<T> after;
		private Set<CstNode> removed;
		private Set<CstNode> added;
		//private ArrayList<Double> similaritySame = new ArrayList<>();
		private ThresholdsProvider threshold = new ThresholdsProvider();
		private CstComparatorMonitor monitor;
		
		private final Map<CstNode, CstNode> mapBeforeToAfter = new HashMap<>();
		private final Map<CstNode, CstNode> mapAfterToBefore = new HashMap<>();
		
		DiffBuilder(SourceRepresentationBuilder<T> srb, SourceFileSet sourcesBefore, SourceFileSet sourcesAfter, CstComparatorMonitor monitor) throws Exception {
			this.srb = srb;
			CstRoot cstRootBefore = languagePlugin.parse(sourcesBefore);
			CstRoot cstRootAfter = languagePlugin.parse(sourcesAfter);
			this.diff = new CstDiff(cstRootBefore, cstRootAfter);
			this.before = new CstRootHelper<>(this.diff.getBefore(), sourcesBefore, srb, true);
			this.after = new CstRootHelper<>(this.diff.getAfter(), sourcesAfter, srb, false);
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
		
		public CstDiff getDiff() {
			return diff;
		}

		private void apply(Set<Relationship> relationships) {
			for (Relationship r : relationships) {
				addRelationship(r);
			}
		}

		private T getTokensToUseNode(CstNode node) {
			//return srb.buildForFragment(Collections.emptyList());
			List<String> tokens = new ArrayList<>();
			tokens.add(node.getSimpleName());
			tokens.add("(");
			tokens.add(")");
			if (node.getParameters() != null) {
				for (int i = 1; i < node.getParameters().size(); i++) {
					tokens.add(",");
				}
			}
			return srb.buildForFragment(tokens);
		}
		
		CstDiff computeDiff() {
			computeSourceRepresentationForRemovedAndAdded();
			findMatchesById();
			//findMatchesByName();
			findMatchesByUniqueName(0.75);
			findMatchesBySimilarity(true);
			findMatchesBySimilarity(false);
			//findMatchesByUniqueName(0.25);
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
			for (CstNode node : removed) {
				before.computeSourceRepresentation(node);
			}
			for (CstNode node : added) {
				after.computeSourceRepresentation(node);
			}
		}
		
//		@SuppressWarnings("unused")
//		private void adjustThreshold() {
//			ArrayList<Double> similaritySame = new ArrayList<>();
//			for (Entry<CstNode, CstNode> entry : mapBeforeToAfter.entrySet()) {
//				CstNode n1 = entry.getKey();
//				CstNode n2 = entry.getValue();
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
		
//		private void findMatchesByName() {
//			List<PotentialMatch> candidates = new ArrayList<>();
//			Map<String, PairBeforeAfter<List<CstNode>>> nodesGroupedByName = new HashMap<>();
//			for (CstNode n1 : removed) {
//				PairBeforeAfter<List<CstNode>> pair = nodesGroupedByName.computeIfAbsent(n1.getSimpleName(), name -> new PairBeforeAfter<List<CstNode>>(new ArrayList<>(), new ArrayList<>()));
//				pair.getBefore().add(n1);
//			}
//			for (CstNode n2 : added) {
//				PairBeforeAfter<List<CstNode>> pair = nodesGroupedByName.computeIfAbsent(n2.getSimpleName(), name -> new PairBeforeAfter<List<CstNode>>(new ArrayList<>(), new ArrayList<>()));
//				pair.getAfter().add(n2);
//			}
//			for (PairBeforeAfter<List<CstNode>> pair : nodesGroupedByName.values()) {
//				if (pair.getBefore().size() == 1 && pair.getAfter().size() == 1) {
//					CstNode n1 = pair.getBefore().get(0);
//					CstNode n2 = pair.getAfter().get(0);
//					if (sameType(n1, n2) && !anonymous(n1) && !anonymous(n2)) {
//						Optional<RelationshipType> optRelationshipType = findRelationshipForCandidate(n1, n2);
//						if (optRelationshipType.isPresent()) {
//							RelationshipType type = optRelationshipType.get();
//							double score = computeLightSimilarityScore(n1, n2);
//							if (type.isById() || score > threshold.getMinimum()) {
//								PotentialMatch candidate = new PotentialMatch(n1, n2, Math.max(before.depth(n1), after.depth(n2)), score);
//								candidates.add(candidate);
//							} else {
//								monitor.reportDiscardedMatch(n1, n2, score);
//							}
//						}
//					}
//				}
//			}
//			Collections.sort(candidates);
//			for (PotentialMatch candidate : candidates) {
//				addMatch(candidate.getNodeBefore(), candidate.getNodeAfter());
//			}
//		}
		
		private void findMatchesByUniqueName(double threshold) {
			List<PotentialMatch> candidates = new ArrayList<>();
			for (CstNode n1 : removed) {
				String name = n1.getLocalName();
				if (before.findByLocalName(name).size() == 1) {
					List<CstNode> n2WithSameName = after.findByLocalName(name);
					if (n2WithSameName.size() == 1) {
						CstNode n2 = n2WithSameName.get(0);
						if (added(n2) && sameType(n1, n2)) {
							Optional<RelationshipType> optRelationshipType = findRelationshipForCandidate(n1, n2);
							if (optRelationshipType.isPresent()) {								
								double score = computeHardSimilarityScore(n1, n2);
								boolean emptyBody = isAbstract(n1, n2);
								if (!emptyBody && score > threshold) {
									PotentialMatch candidate = new PotentialMatch(n1, n2, Math.max(before.depth(n1), after.depth(n2)), score);
									candidates.add(candidate);
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
		
		private void findMatchesBySimilarity(boolean onlySafe) {
			List<PotentialMatch> candidates = new ArrayList<>();
			for (CstNode n1 : removed) {
				for (CstNode n2 : added) {
					if (sameType(n1, n2) && !anonymous(n1) && !anonymous(n2)) {
						boolean safePair = sameName(n1, n2) || sameLocation(n1, n2);
						double thresholdValue = safePair ? threshold.getMinimum() : threshold.getIdeal();
						if (!onlySafe || safePair) {
							Optional<RelationshipType> optRelationshipType = findRelationshipForCandidate(n1, n2);
							if (optRelationshipType.isPresent()) {
								RelationshipType type = optRelationshipType.get();
								double score = computeHardSimilarityScore(n1, n2);
								//double scoreLight = computeLightSimilarityScore(n1, n2);
								double rankScore = srb.rawSimilarity(before.sourceRep(n1), after.sourceRep(n2)) * score;
								
								//boolean emptyBody = isAbstract(n1, n2);
								
								if (type.isById() || score > thresholdValue) {
									PotentialMatch candidate = new PotentialMatch(n1, n2, Math.max(before.depth(n1), after.depth(n2)), rankScore);
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
			for (CstNode n1 : removed) {
				for (CstNode n2 : added) {
					int matchingChild = countMatchingChild(n1, n2);
					if (sameType(n1, n2) && !anonymous(n1) && !anonymous(n2) && matchingChild > 1) {
						double nameScore = computeNameSimilarity(n1, n2);
						
//						double matchingChildrenRatio = ((double) matchingChild) / n1.getNodes().size();
						
						if (nameScore > 0.5) {
							Optional<RelationshipType> optRelationshipType = findRelationshipForCandidate(n1, n2);
							
							
							if (optRelationshipType.isPresent()) {
								double score = computeLightSimilarityScore(n1, n2);
								//if (score > threshold.getIdeal()) {
								PotentialMatch candidate = new PotentialMatch(n1, n2, Math.max(before.depth(n1), after.depth(n2)), score);
								candidates.add(candidate);
								//}
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
		
		private double computeHardSimilarityScore(CstNode n1, CstNode n2) {
			return srb.similarity(before.sourceRep(n1), after.sourceRep(n2));
		}
		
		private double computeNameSimilarity(CstNode n1, CstNode n2) {
			double s1 = Math.max(
				srb.partialSimilarity(before.nameSourceRep(n1), after.nameSourceRep(n2)), 
				srb.partialSimilarity(after.nameSourceRep(n2), before.nameSourceRep(n1)));
			//double s2 = srb.similarity(before.nameSourceRep(n1), after.nameSourceRep(n2));
			return s1;
		}
		
//		private double computeMixedSimilarityScore(CstNode n1, CstNode n2) {
//			return (computeNameSimilarity(n1, n2) + 2 * computeHardSimilarityScore(n1, n2)) / 3.0;
//		}
		
		private double computeLightSimilarityScore(CstNode n1, CstNode n2) {
			double score1 = srb.partialSimilarity(before.sourceRep(n1), after.sourceRep(n2));
			double score2 = srb.partialSimilarity(after.sourceRep(n2), before.sourceRep(n1));
			return Math.max(score1, score2);
		}
		
		private Optional<RelationshipType> findRelationshipForCandidate(CstNode n1, CstNode n2) {
			if (sameLocation(n1, n2) && sameSignature(n1, n2)) {
				return Optional.of(RelationshipType.SAME);
			} else if (sameSignature(n1, n2) && n1.hasStereotype(Stereotype.TYPE_MEMBER) && after.hasRelationship(CstNodeRelationshipType.SUBTYPE, matchingNodeAfter(n1.getParent()), n2.getParent())) {
				return Optional.of(RelationshipType.PULL_UP);
			} else if (sameSignature(n1, n2) && n1.hasStereotype(Stereotype.TYPE_MEMBER) && after.hasRelationship(CstNodeRelationshipType.SUBTYPE, n2.getParent(), matchingNodeAfter(n1.getParent()))) {
				return Optional.of(RelationshipType.PUSH_DOWN);
			} else if (sameLocation(n1, n2)) {
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
			for (CstNode n2 : added) {
//				if (n2.getLocalName().equals("getNodes()") && n2.getLocation().getFile().equals("core/src/main/java/com/graphhopper/storage/LevelGraphImpl.java") && n2.getLocation().getLine() == 144) {
//					n2.getLocalName();
//				}
				for (CstNode n1After : after.findReverseRelationships(CstNodeRelationshipType.USE, n2)) {
					Optional<CstNode> optMatchingNode = matchingNodeBefore(n1After);
					if (optMatchingNode.isPresent()) {
						CstNode n1 = optMatchingNode.get();
						if (sameType(n1, n2)/* && !n2.hasStereotype(Stereotype.FIELD_ACCESSOR) && !n2.hasStereotype(Stereotype.FIELD_MUTATOR) */) {
							T sourceN1After = after.sourceRep(n1After);
							T sourceN1Before = before.sourceRep(n1);
							T bodySourceN2 = after.bodySourceRep(n2);
							T removedSource = srb.minus(srb.combine(sourceN1Before, getTokensToUseNode(n2)), sourceN1After);
//							double score1 = srb.partialSimilarity(bodySourceN2, removedSource);
//							double score2 = srb.partialSimilarity(removedSource, bodySourceN2);
//							double scoreMax = Math.max(score1, score2);
							double score = srb.partialSimilarity(bodySourceN2, removedSource);
//							double rawScore = srb.rawSimilarity(bodySourceN2, removedSource);
							//double finalScore = rawScore * score;
							
//							if (n2.getSimpleName().equals("parseAndValidateMetadata") && n2.getLocation().getFile().equals("core/src/test/java/feign/DefaultContractTest.java")) {
//								System.out.println("danilo"); 
//							}
							
							boolean sameLocation = sameLocation(n1, n2);
							if (score > threshold.getIdeal()) {
								if (sameLocation) {
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
			for (CstNode n1 : removed) {
				for (CstNode n1Caller : before.findReverseRelationships(CstNodeRelationshipType.USE, n1)) {
					Optional<CstNode> optMatchingNode = matchingNodeAfter(n1Caller);
					if (optMatchingNode.isPresent()) {
						CstNode n2 = optMatchingNode.get();
						if (sameType(n1, n2)/* && !n1.hasStereotype(Stereotype.FIELD_ACCESSOR) && !n1.hasStereotype(Stereotype.FIELD_MUTATOR) */) {
							T sourceN1 = before.bodySourceRep(n1);
							T sourceN1Caller = before.sourceRep(n1Caller);
							T sourceN1CallerAfter = after.sourceRep(n2);
							T addedCode = srb.minus(srb.minus(sourceN1CallerAfter, getTokensToUseNode(n1)), sourceN1Caller);
							//double score = srb.partialSimilarity(sourceN1, addedCode);
//							boolean sameLocation = sameLocation(n1, n2);
//							double score1 = srb.partialSimilarity(sourceN1, addedCode);
//							double score2 = srb.partialSimilarity(addedCode, sourceN1);
//							double score = Math.max(score1, score2);
							double score = srb.partialSimilarity(sourceN1, addedCode);
							if (score > threshold.getIdeal()) {
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
			for (CstNode n1 : children(parentBefore, this::removed)) {
				for (CstNode n2 : children(parentAfter, this::added)) {
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
					CstNode n2 = r.getNodeAfter();
					CstNode supertypeAfter = n2.getParent().get();
					for (CstNode n1ParentAfter : after.findReverseRelationships(CstNodeRelationshipType.SUBTYPE, supertypeAfter)) {
						Optional<CstNode> n1ParentBefore = matchingNodeBefore(n1ParentAfter);
						if (n1ParentBefore.isPresent()) {
							for (CstNode n1 : children(n1ParentBefore.get(), this::removed)) {
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
					CstNode n1 = r.getNodeBefore();
					CstNode n1ParentAfter = matchingNodeAfter(n1.getParent()).get();
					for (CstNode n2ParentAfter : after.findReverseRelationships(CstNodeRelationshipType.SUBTYPE, n1ParentAfter)) {
						for (CstNode n2 : children(n2ParentAfter, this::added)) {
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

		private void findPullUpAndPushOnMatch(CstNode parentBefore, CstNode parentAfter) {
			// Find Pull Up
			{
				CstNode subtypeAfter = (CstNode) parentAfter;
				for (CstNode supertypeAfter : after.findRelationships(CstNodeRelationshipType.SUBTYPE, subtypeAfter)) {
					for (CstNode n1 : parentBefore.getNodes()) {
						for (CstNode n2 : children(supertypeAfter, this::added)) {
							if (n1.hasStereotype(Stereotype.TYPE_MEMBER) && sameSignature(n1, n2)) {
								Optional<CstNode> optN1After = matchingNodeAfter(n1);
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
				CstNode supertypeBefore = (CstNode) parentBefore;
				for (CstNode subtypeBefore : before.findReverseRelationships(CstNodeRelationshipType.SUBTYPE, supertypeBefore)) {
					for (CstNode n1 : subtypeBefore.getNodes()) {
						for (CstNode n2 : parentAfter.getNodes()) {
							if (n1.hasStereotype(Stereotype.TYPE_MEMBER) && sameSignature(n1, n2)) {
								Optional<CstNode> optN1After = matchingNodeAfter(n1);
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
				CstNode supertypeAfter = (CstNode) parentAfter;
				for (CstNode subtypeAfter : after.findReverseRelationships(CstNodeRelationshipType.SUBTYPE, supertypeAfter)) {
					for (CstNode n1 : parentBefore.getNodes()) {
						for (CstNode n2 : children(subtypeAfter, this::added)) {
							if (n1.hasStereotype(Stereotype.TYPE_MEMBER) && sameSignature(n1, n2)) {
								Optional<CstNode> optN1After = matchingNodeAfter(n1);
								if (optN1After.isPresent()) {
									CstNode n1After = optN1After.get();
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
				CstNode subtypeBefore = (CstNode) parentBefore;
				for (CstNode supertypeBefore : before.findRelationships(CstNodeRelationshipType.SUBTYPE, subtypeBefore)) {
					for (CstNode n1 : supertypeBefore.getNodes()) {
						for (CstNode n2 : parentAfter.getNodes()) {
							if (n1.hasStereotype(Stereotype.TYPE_MEMBER) && sameSignature(n1, n2)) {
								Optional<CstNode> optN1After = matchingNodeAfter(n1);
								if (!optN1After.isPresent()) {
									apply(new Relationship(RelationshipType.PUSH_DOWN, n1, n2));
								} else {
									CstNode n1After = optN1After.get();
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
					CstNode supertypeAfter = r.getNodeAfter().getParent().get();
					CstNode subtypeBefore = r.getNodeBefore().getParent().get();
					
					// If there is the same supertype before, ignore
					for (CstNode superTypeBefore : before.findRelationships(CstNodeRelationshipType.SUBTYPE, subtypeBefore)) {
						if (superTypeBefore.getSimpleName().equals(supertypeAfter.getSimpleName())) {
							return;
						}
					}
					
					boolean hadNoSupertypesBefore = true;//before.findRelationships(CstNodeRelationshipType.SUBTYPE, subtypeBefore).isEmpty();
					if (added(supertypeAfter) && hadNoSupertypesBefore) {
						relationships.add(new Relationship(RelationshipType.EXTRACT_SUPER, subtypeBefore, supertypeAfter));
					}
				});
			
			inferExtractEmptySuper(relationships);
			apply(relationships);
		}
		
		private void inferExtractEmptySuper(Set<Relationship> relationships) {
			for (CstNode n2 : added) {
				if (n2.getNodes().isEmpty()) {
					for (CstNode subtypeAfter : after.findReverseRelationships(CstNodeRelationshipType.SUBTYPE, n2)) {
						Optional<CstNode> optN1Before = matchingNodeBefore(subtypeAfter);
						if (optN1Before.isPresent()) {
							relationships.add(new Relationship(RelationshipType.EXTRACT_SUPER, optN1Before.get(), n2));
						}
					}
				}
			}
		}
		
		private void addMatch(CstNode nBefore, CstNode nAfter) {
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
			for (Entry<CstNode, CstNode> entry : mapBeforeToAfter.entrySet()) {
				CstNode n1 = entry.getKey();
				CstNode n2 = entry.getValue();
				Optional<RelationshipType> type = findRelationshipForCandidate(n1, n2);
				if (type.isPresent()) {
					diff.addRelationships(new Relationship(type.get(), n1, n2));
				}
			}
		}

		private void findPullPushDownAbstract() {
			for (Entry<CstNode, CstNode> entry : mapBeforeToAfter.entrySet()) {
				CstNode n1 = entry.getKey();
				CstNode n2 = entry.getValue();
				findPullUpAndPushOnMatch(n1, n2);
			}
		}
		
		private void addRelationship(Relationship relationship) {
			CstNode nBefore = relationship.getNodeBefore();
			CstNode nAfter = relationship.getNodeAfter();
			RelationshipType type = relationship.getType();
			
			if (type.isUnmarkRemoved() && !removed(nBefore) || type.isUnmarkAdded() && !added(nAfter)) {
				monitor.reportDiscardedConflictingMatch(nBefore, nAfter);
			} else {
				diff.addRelationships(relationship);
			}
		}
		
		public Optional<CstNode> matchingNodeBefore(CstNode n2) {
			return Optional.ofNullable(mapAfterToBefore.get(n2));
		}
		
		public Optional<CstNode> matchingNodeAfter(CstNode n1) {
			return Optional.ofNullable(mapBeforeToAfter.get(n1));
		}
		
		public Optional<CstNode> matchingNodeAfter(Optional<CstNode> optN1) {
			if (optN1.isPresent()) {
				return matchingNodeAfter(optN1.get());
			}
			return Optional.empty();
		}
		
		public boolean sameLocation(CstNode n1, CstNode n2) {
			if (n1.getParent().isPresent() && n2.getParent().isPresent()) {
				return matchingNodeAfter(n1.getParent().get()).equals(n2.getParent());
			} else if (!n1.getParent().isPresent() && !n1.getParent().isPresent()) {
				return sameNamespace(n1, n2);
			} else {
				return false;
			}
		}
		
		public boolean sameRootNode(CstNode n1, CstNode n2) {
			Optional<CstNode> n1Root = n1.getRootParent();
			Optional<CstNode> n2Root = n2.getRootParent();
			if (n1Root.isPresent() && n2Root.isPresent()) {
				return matchingNodeAfter(n1Root.get()).equals(n2Root);
			} else {
				return false;
			}
		}
		
		public boolean isAbstract(CstNode n1, CstNode n2) {
			return n1.hasStereotype(Stereotype.ABSTRACT) || n2.hasStereotype(Stereotype.ABSTRACT);
		}
		
		public boolean removed(CstNode n) {
			return this.removed.contains(n);
		}
		
		public boolean added(CstNode n) {
			return this.added.contains(n);
		}
		
//		private boolean childrenMatch(CstNode n1, CstNode n2) {
//			boolean existsChildMatch = false;
//			if (n1.getNodes().size() == 0 || n2.getNodes().size() == 0) {
//				return false;
//			}
//			for (CstNode n1Child : n1.getNodes()) {
//				Optional<CstNode> maybeN2Child = matchingNodeAfter(n1Child);
//				if (maybeN2Child.isPresent()) {
//					if (childOf(maybeN2Child.get(), n2)) {
//						existsChildMatch = true;
//					} else {
//						return false;
//					}
//				}
//			}
//			for (CstNode n2Child : n2.getNodes()) {
//				Optional<CstNode> maybeN1Child = matchingNodeBefore(n2Child);
//				if (maybeN1Child.isPresent() && !childOf(maybeN1Child.get(), n1)) {
//					return false;
//				}
//			}
//			return existsChildMatch;
//		}
		
//		private boolean existsMatchingChild(CstNode n1, CstNode n2) {
//			if (n1.getNodes().size() == 0 || n2.getNodes().size() == 0) {
//				return false;
//			}
//			for (CstNode n1Child : n1.getNodes()) {
//				Optional<CstNode> maybeN2Child = matchingNodeAfter(n1Child);
//				if (maybeN2Child.isPresent()) {
//					if (childOf(maybeN2Child.get(), n2)) {
//						return true;
//					}
//				}
//			}
//			return false;
//		}
		
		public int countMatchingChild(CstNode n1, CstNode n2) {
			if (n1.getNodes().size() == 0 || n2.getNodes().size() == 0) {
				return 0;
			}
			int count = 0;
			for (CstNode n1Child : n1.getNodes()) {
				Optional<CstNode> maybeN2Child = matchingNodeAfter(n1Child);
				if (maybeN2Child.isPresent()) {
					if (childOf(maybeN2Child.get(), n2)) {
						count++;
					}
				}
			}
			return count;
		}
		
		public List<CstNode> children(HasChildrenNodes nodeWithChildren, Predicate<CstNode> predicate) {
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

	public LanguagePlugin getLanguagePlugin() {
		return languagePlugin;
	}

}
