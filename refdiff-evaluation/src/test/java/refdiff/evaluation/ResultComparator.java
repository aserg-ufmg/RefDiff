package refdiff.evaluation;

import static refdiff.evaluation.RefactoringRelationship.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import refdiff.core.diff.Relationship;

public class ResultComparator {
	
	Set<String> groupIds = new LinkedHashSet<>();
	Map<String, RefactoringSet> expectedMap = new LinkedHashMap<>();
	Map<String, RefactoringSet> notExpectedMap = new LinkedHashMap<>();
	Map<String, CompareResult> resultMap = new HashMap<>();
	Map<String, Map<KeyPair, String>> fnExplanations = new HashMap<>();
	
	private boolean ignorePullUpToExtractedSupertype = false;
	private boolean ignoreMoveToMovedType = false;
	private boolean ignoreMoveToRenamedType = false;
	
	public ResultComparator expect(RefactoringSet... sets) {
		for (RefactoringSet set : sets) {
			expectedMap.put(getProjectRevisionId(set.getProject(), set.getRevision()), set);
		}
		return this;
	}
	
	public ResultComparator expect(Iterable<RefactoringSet> sets) {
		for (RefactoringSet set : sets) {
			expectedMap.put(getProjectRevisionId(set.getProject(), set.getRevision()), set);
		}
		return this;
	}
	
	public ResultComparator dontExpect(RefactoringSet... sets) {
		for (RefactoringSet set : sets) {
			notExpectedMap.put(getProjectRevisionId(set.getProject(), set.getRevision()), set);
		}
		return this;
	}
	
	public void remove(String project, String revision) {
		String id = getProjectRevisionId(project, revision);
		expectedMap.remove(id);
		notExpectedMap.remove(id);
	}
	
	public ResultComparator dontExpect(Iterable<RefactoringSet> sets) {
		for (RefactoringSet set : sets) {
			notExpectedMap.put(getProjectRevisionId(set.getProject(), set.getRevision()), set);
		}
		return this;
	}
	
	public ResultComparator compareWith(String groupId, RefactoringSet... actualArray) {
		for (RefactoringSet actual : actualArray) {
			compareWith(groupId, actual);
		}
		return this;
	}
	
	public ResultComparator compareWith(String groupId, Iterable<RefactoringSet> actualArray) {
		for (RefactoringSet actual : actualArray) {
			compareWith(groupId, actual);
		}
		return this;
	}
	
	public void compareWith(String groupId, RefactoringSet actual) {
		groupIds.add(groupId);
		resultMap.put(getResultId(actual.getProject(), actual.getRevision(), groupId), computeResult(actual));
	}
	
	public CompareResult computeResult(RefactoringSet actual) {
		List<RefactoringRelationship> truePositives = new ArrayList<>();
		List<RefactoringRelationship> falsePositives = new ArrayList<>();
		List<RefactoringRelationship> falseNegatives = new ArrayList<>();
		
		RefactoringSet expected = expectedMap.get(getProjectRevisionId(actual.getProject(), actual.getRevision()));
		Set<RefactoringRelationship> expectedRefactorings = new HashSet<>(expected.getRefactorings());
		Set<RefactoringRelationship> expectedUnfiltered = expectedRefactorings;
		Set<RefactoringRelationship> actualRefactorings = actual.getRefactorings();
		for (RefactoringRelationship r : actualRefactorings) {
			if (expectedRefactorings.contains(r)) {
				truePositives.add(r);
				expectedRefactorings.remove(r);
			} else {
				boolean ignoreFp = ignoreMoveToMovedType && isMoveToMovedType(r, expectedUnfiltered) ||
					ignoreMoveToRenamedType && isMoveToRenamedType(r, expectedUnfiltered) ||
					ignorePullUpToExtractedSupertype && isPullUpToExtractedSupertype(r, expectedUnfiltered);
				if (!ignoreFp) {
					falsePositives.add(r);
				}
			}
		}
		for (RefactoringRelationship r : expectedRefactorings) {
			falseNegatives.add(r);
		}
		
		return new CompareResult(truePositives, falsePositives, falseNegatives);
	}
	
	public int getExpectedCount(EnumSet<RefactoringType> refTypesToConsider) {
		int sum = 0;
		EnumSet<RefactoringType> ignore = EnumSet.complementOf(refTypesToConsider);
		for (RefactoringSet set : expectedMap.values()) {
			sum += set.ignoring(ignore).getRefactorings().size();
		}
		return sum;
	}
	
	public void printSummary(PrintStream out, EnumSet<RefactoringType> refTypesToConsider) {
		for (String groupId : groupIds) {
			CompareResult r = getCompareResult(groupId, refTypesToConsider);
			out.println("# " + groupId + " #");
			out.println("Total  " + getResultLine(r.getTPCount(), r.getFPCount(), r.getFNCount()));
			
			for (RefactoringType refType : refTypesToConsider) {
				CompareResult resultForRefType = r.filterBy(refType);
				int tpRt = resultForRefType.getTPCount();
				int fpRt = resultForRefType.getFPCount();
				int fnRt = resultForRefType.getFNCount();
				if (tpRt > 0 || fpRt > 0 || fnRt > 0) {
					out.println(String.format("%-7s" + getResultLine(tpRt, fpRt, fnRt), refType.getAbbreviation()));
				}
			}
			out.println();
		}
		out.println();
	}
	
	public CompareResult getCompareResult(String groupId, EnumSet<RefactoringType> refTypesToConsider) {
		CompareResult merged = new CompareResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
		for (RefactoringSet expected : expectedMap.values()) {
			CompareResult result = resultMap.get(getResultId(expected.getProject(), expected.getRevision(), groupId));
			if (result != null) {
				merged.mergeWith(result.filterBy(refTypesToConsider));
			}
		}
		return merged;
	}
	
	private String getResultLine(int tp, int fp, int fn) {
		double precision = getPrecision(tp, fp, fn);
		double recall = getRecall(tp, fp, fn);
		double f1 = getF1(tp, fp, fn);
		// return String.format("& %3d & %3d & %3d & %3d & %.3f & %.3f \\", tp + fn, tp, fp, fn, precision, recall);
		return String.format("#: %3d  TP: %3d  FP: %3d  FN: %3d  Prec.: %.3f  Recall: %.3f  F1: %.3f", tp + fn, tp, fp, fn, precision, recall, f1);
	}
	
	private static double getPrecision(int tp, int fp, int fn) {
		return tp == 0 ? 0.0 : ((double) tp / (tp + fp));
	}
	
	private static double getRecall(int tp, int fp, int fn) {
		return tp == 0 ? 0.0 : ((double) tp) / (tp + fn);
	}
	
	private static double getF1(int tp, int fp, int fn) {
		double precision = ResultComparator.getPrecision(tp, fp, fn);
		double recall = ResultComparator.getRecall(tp, fp, fn);
		return tp == 0 ? 0.0 : 2.0 * precision * recall / (precision + recall);
	}
	
	public void printDetails(PrintStream out, EnumSet<RefactoringType> refTypesToConsider, String groupId) {
		printDetails(out, refTypesToConsider, groupId, (RefactoringSet rs, RefactoringRelationship r, String label, String cause, EvaluationDetails evaluationDetails) -> {
			out.print('\t');
			out.print(label);
		});
	}
	
	public void printDetails(PrintStream out, EnumSet<RefactoringType> refTypesToConsider, String groupId, ResultRowPrinter rowPrinter) {
		String[] labels = { "TN", "FP", "FN", "TP" };
		EnumSet<RefactoringType> ignore = EnumSet.complementOf(refTypesToConsider);
		boolean headerPrinted = false;
		for (RefactoringSet expected : expectedMap.values()) {
			Set<RefactoringRelationship> all = new HashSet<>();
			Set<RefactoringRelationship> expectedRefactorings = expected.ignoring(ignore).getRefactorings();
			String id = getProjectRevisionId(expected.getProject(), expected.getRevision());
			Set<RefactoringRelationship> notExpectedRefactorings = notExpectedMap.getOrDefault(id, new RefactoringSet(expected.getProject(), expected.getRevision())).getRefactorings();
			
			String header = String.format("Commit\tRef Type\tEntity before\tEntity after\t%s\tDetails", groupId);
			
			CompareResult result = resultMap.get(getResultId(expected.getProject(), expected.getRevision(), groupId));
			if (result != null) {
				CompareResult resultFiltered = result.filterBy(refTypesToConsider);
				all.addAll(resultFiltered.getTruePositives());
				all.addAll(resultFiltered.getFalsePositives());
				all.addAll(resultFiltered.getFalseNegatives());
			} else {
				all.addAll(expectedRefactorings); //
			}
			
			if (!headerPrinted) {
				out.println(header);
				headerPrinted = true;
			}
			if (!all.isEmpty()) {
				//out.println(getProjectRevisionId(expected.getProject(), expected.getRevision()));
				ArrayList<RefactoringRelationship> allList = new ArrayList<>();
				allList.addAll(all);
				Collections.sort(allList);
				for (RefactoringRelationship r : allList) {
					out.print(id);
					out.print('\t');
					out.print(format(r));
					// out.print('\t');
					if (result != null) {
						Set<RefactoringRelationship> actualRefactorings = new HashSet<>();
						actualRefactorings.addAll(result.getTruePositives());
						actualRefactorings.addAll(result.getFalsePositives());
						int correct = expectedRefactorings.contains(r) ? 2 : 0;
						int found = actualRefactorings.contains(r) ? 1 : 0;
						String label = labels[correct + found];
						String cause = "";
						EvaluationDetails evaluationDetails = findEvaluationDetails(r, expected.getRefactorings(), notExpectedRefactorings);
						
						if (label == "FP") {
							cause = findFpCause(r, expected.getRefactorings(), notExpectedRefactorings, evaluationDetails);
							if (cause.equals("?")) {
								label = label + "?";
							}
						} else if (label == "FN") {
							cause = findFnCause(r, actualRefactorings, this.fnExplanations.get(getProjectRevisionId(expected.getProject(), expected.getRevision())));
						}
						// out.print(label);
						rowPrinter.printDetails(expected, r, label, cause, evaluationDetails);
						/*
						 * if (label.equals("FP") || label.equals("FN")) { if (cause != null) { out.print('\t'); out.print(cause); } }
						 */
					}
					out.println();
				}
			}
		}
		out.println();
	}
	
	public void printDetails2(PrintStream out, EnumSet<RefactoringType> refTypesToConsider) {
		String[] labels = { "TN", "FP", "FN", "TP" };
		EnumSet<RefactoringType> ignore = EnumSet.complementOf(refTypesToConsider);
		String header = String.format("Commit\tRef Type\tDescription\tRelationship\tCst Node Before\tCst Node After\tExpected?");
		for (String groupId : this.groupIds) {
			header += "\t" + groupId;
		}
		header += "\tEvaluators\tEvaluators classification";
		out.println(header);
		
		for (RefactoringSet expected : expectedMap.values()) {
			Set<RefactoringRelationship> all = new HashSet<>();
			String id = getProjectRevisionId(expected.getProject(), expected.getRevision());
			Set<RefactoringRelationship> expectedRefactorings = expected.ignoring(ignore).getRefactorings();
			Set<RefactoringRelationship> notExpectedRefactorings = notExpectedMap.getOrDefault(id, new RefactoringSet(expected.getProject(), expected.getRevision())).getRefactorings();
			
			for (String groupId : this.groupIds) {
				CompareResult result = resultMap.get(getResultId(expected.getProject(), expected.getRevision(), groupId));
				if (result != null) {
					CompareResult resultFiltered = result.filterBy(refTypesToConsider);
					all.addAll(resultFiltered.getTruePositives());
					all.addAll(resultFiltered.getFalsePositives());
					//all.addAll(resultFiltered.getFalseNegatives());
				}
			}
			all.addAll(expectedRefactorings);
			
			if (!all.isEmpty()) {
				//out.println(getProjectRevisionId(expected.getProject(), expected.getRevision()));
				ArrayList<RefactoringRelationship> allList = new ArrayList<>();
				allList.addAll(all);
				Collections.sort(allList);
				for (RefactoringRelationship r : allList) {
					int correct = expectedRefactorings.contains(r) ? 2 : 0;
					
					out.print(id);
					out.print('\t');
					out.print(r.getRefactoringType().getDisplayName());
//					out.print('\t');
//					out.print(format(r));
					out.print('\t');
					String refDescriptionFromOracle = (correct > 0 ? expectedRefactorings : notExpectedRefactorings).stream().filter(i -> i.equals(r)).findFirst().map(i -> i.getDescription()).orElse("");
					out.print(refDescriptionFromOracle);
					
					out.print('\t');
					Relationship cstRel = r.getCstRelationship();
					if (cstRel != null) {
						out.print(cstRel.getStandardDescription());
					} else {
						out.print("\t\t");
					}
					
					out.print('\t');
					out.print(correct > 0 ? "T" : "F");
					
					for (String groupId : this.groupIds) {
						CompareResult result = resultMap.get(getResultId(expected.getProject(), expected.getRevision(), groupId));
						
						out.print('\t');
						if (result != null) {
							Set<RefactoringRelationship> actualRefactorings = new HashSet<>();
							actualRefactorings.addAll(result.getTruePositives());
							actualRefactorings.addAll(result.getFalsePositives());
							
							int found = actualRefactorings.contains(r) ? 1 : 0;
							String label = labels[correct + found];
							out.print(label);
						}
					}
					
					EvaluationDetails evaluationDetails = findEvaluationDetails(r, expected.getRefactorings(), notExpectedRefactorings);
					if (evaluationDetails != null && evaluationDetails.evaluators != null) {								
						String evaluators = evaluationDetails.evaluators;
						out.print('\t');
						out.print(evaluationDetails.evaluators);
						
						out.print('\t');
						String classification;
						if ("Gustavo/Ricardo".equals(evaluators)) {
							classification = evaluationDetails.resultA + "/" + evaluationDetails.resultB;
						} else if ("Gustavo/Danilo".equals(evaluators)) {
							classification = evaluationDetails.resultA + "/" + evaluationDetails.resultC;
						} else /* if ("Ricardo/Danilo".equals(evaluators)) */ {
							classification = evaluationDetails.resultB + "/" + evaluationDetails.resultC;
						}
						out.print(classification.replace("?", ""));
					} else {
						out.print('\t');
						out.print('\t');
					}
					
//					out.print('\t');
//					if (evaluationDetails != null) {
//						String fpCause = findFpCause(r, expected.getRefactorings(), notExpectedRefactorings, evaluationDetails);
//						if (!"?".equals(fpCause)) {
//							out.print(fpCause);
//						}
//					}
					
					out.println();
				}
			}
		}
		out.println();
	}
	
	public static String format(RefactoringRelationship r) {
		return String.format("%s\t%s\t%s", r.getRefactoringType().getDisplayName(), r.getEntityBefore(), r.getEntityAfter());
	}
	
	private String findFpCause(RefactoringRelationship r, Set<RefactoringRelationship> expectedUnfiltered, Set<RefactoringRelationship> blacklisted, EvaluationDetails evaluationDetails) {
		if (evaluationDetails != null) {
			if (evaluationDetails.commentFinal != null) {
				return evaluationDetails.commentFinal;
			}
		}
		if (isPullUpToExtractedSupertype(r, expectedUnfiltered)) {
			return "<PullUpToExtractedSupertype>";
		}
		if (isMoveToRenamedType(r, expectedUnfiltered)) {
			return "<MoveToRenamedType>";
		}
		if (isMoveToMovedType(r, expectedUnfiltered)) {
			return "<MoveToMovedType>";
		}
		if (r.getRefactoringType() == RefactoringType.MOVE_ATTRIBUTE || r.getRefactoringType() == RefactoringType.MOVE_OPERATION) {
			if (expectedUnfiltered.contains(new RefactoringRelationship(RefactoringType.PULL_UP_ATTRIBUTE, (r.getEntityBefore()), (r.getEntityAfter())))) {
				return "<ShouldBePullUp>";
			}
			if (expectedUnfiltered.contains(new RefactoringRelationship(RefactoringType.PUSH_DOWN_ATTRIBUTE, (r.getEntityBefore()), (r.getEntityAfter())))) {
				return "<ShouldBePushDown>";
			}
			if (expectedUnfiltered.contains(new RefactoringRelationship(RefactoringType.PULL_UP_OPERATION, (r.getEntityBefore()), (r.getEntityAfter())))) {
				return "<ShouldBePullUp>";
			}
			if (expectedUnfiltered.contains(new RefactoringRelationship(RefactoringType.PUSH_DOWN_OPERATION, (r.getEntityBefore()), (r.getEntityAfter())))) {
				return "<ShouldBePushDown>";
			}
		}
		if (blacklisted.contains(r)) {
			//RefactoringRelationship blacklistedR = blacklisted.stream().filter(br -> br.equals(r)).findFirst().get();
			//return blacklistedR.getComment() != null ? blacklistedR.getComment() : "<Blacklist>";
			return "<Blacklist>";
		}
		return "?";
	}
	
	private EvaluationDetails findEvaluationDetails(RefactoringRelationship r, Set<RefactoringRelationship> expected, Set<RefactoringRelationship> blacklisted) {
		if (expected.contains(r)) {
			return expected.stream().filter(br -> br.equals(r)).findFirst().get().getEvaluationDetails();
		}
		if (blacklisted.contains(r)) {
			return blacklisted.stream().filter(br -> br.equals(r)).findFirst().get().getEvaluationDetails();
		}
		return null;
	}
	
	private String findFnCause(RefactoringRelationship r, Set<RefactoringRelationship> actualRefactorings, Map<KeyPair, String> fnCauseMap) {
		if (fnCauseMap != null) {
			KeyPair keyPair = new KeyPair(r.getEntityBefore(), r.getEntityAfter());
			return fnCauseMap.getOrDefault(keyPair, "?");
		}
		return "?";
	}
	
	private boolean isPullUpToExtractedSupertype(RefactoringRelationship r, Set<RefactoringRelationship> expectedUnfiltered) {
		if (r.getRefactoringType() == RefactoringType.PULL_UP_ATTRIBUTE || r.getRefactoringType() == RefactoringType.PULL_UP_OPERATION) {
			if (expectedUnfiltered.contains(new RefactoringRelationship(RefactoringType.EXTRACT_SUPERCLASS, parentOf(r.getEntityBefore()), parentOf(r.getEntityAfter())))) {
				return true;
			}
			if (expectedUnfiltered.contains(new RefactoringRelationship(RefactoringType.EXTRACT_INTERFACE, parentOf(r.getEntityBefore()), parentOf(r.getEntityAfter())))) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isMoveToRenamedType(RefactoringRelationship r, Set<RefactoringRelationship> expectedUnfiltered) {
		if (r.getRefactoringType() == RefactoringType.MOVE_OPERATION || r.getRefactoringType() == RefactoringType.MOVE_ATTRIBUTE) {
			if (expectedUnfiltered.contains(new RefactoringRelationship(RefactoringType.RENAME_CLASS, parentOf(r.getEntityBefore()), parentOf(r.getEntityAfter())))) {
				return true;
			}
			if (expectedUnfiltered.contains(new RefactoringRelationship(RefactoringType.RENAME_CLASS, parentOf(parentOf(r.getEntityBefore())), parentOf(parentOf(r.getEntityAfter()))))) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isMoveToMovedType(RefactoringRelationship r, Set<?> expectedUnfiltered) {
		if (r.getRefactoringType() == RefactoringType.MOVE_OPERATION || r.getRefactoringType() == RefactoringType.MOVE_ATTRIBUTE) {
			if (expectedUnfiltered.contains(new RefactoringRelationship(RefactoringType.MOVE_CLASS, parentOf(r.getEntityBefore()), parentOf(r.getEntityAfter())))) {
				return true;
			}
			if (expectedUnfiltered.contains(new RefactoringRelationship(RefactoringType.MOVE_CLASS, parentOf(parentOf(r.getEntityBefore())), parentOf(parentOf(r.getEntityAfter()))))) {
				return true;
			}
			if (expectedUnfiltered.contains(new RefactoringRelationship(RefactoringType.MOVE_CLASS_FOLDER, parentOf(r.getEntityBefore()), parentOf(r.getEntityAfter())))) {
				return true;
			}
		}
		return false;
	}
	
	private String getProjectRevisionId(String project, String revision) {
		if (project.endsWith(".git")) {
			return project.substring(0, project.length() - 4) + "/commit/" + revision;
		}
		return project + "/commit/" + revision;
	}
	
	private String getResultId(String project, String revision, String groupId) {
		if (project.endsWith(".git")) {
			return project.substring(0, project.length() - 4) + "/commit/" + revision + ";" + groupId;
		}
		return project + "/commit/" + revision + ";" + groupId;
	}
	
	public static class CompareResult {
		private final Collection<RefactoringRelationship> truePositives;
		private final Collection<RefactoringRelationship> falsePositives;
		private final Collection<RefactoringRelationship> falseNegatives;
		
		public CompareResult(Collection<RefactoringRelationship> truePositives, Collection<RefactoringRelationship> falsePositives, Collection<RefactoringRelationship> falseNegatives) {
			this.truePositives = truePositives;
			this.falsePositives = falsePositives;
			this.falseNegatives = falseNegatives;
		}
		
		public int getTPCount() {
			return this.truePositives.size();
		}
		
		public int getFPCount() {
			return this.falsePositives.size();
		}
		
		public int getFNCount() {
			return this.falseNegatives.size();
		}
		
		public double getPrecision() {
			int tp = this.truePositives.size();
			int fp = this.falsePositives.size();
			int fn = this.falseNegatives.size();
			return ResultComparator.getPrecision(tp, fp, fn);
		}
		
		public double getRecall() {
			int tp = this.truePositives.size();
			int fp = this.falsePositives.size();
			int fn = this.falseNegatives.size();
			return ResultComparator.getRecall(tp, fp, fn);
		}
		
		public double getF1() {
			int tp = this.truePositives.size();
			int fp = this.falsePositives.size();
			int fn = this.falseNegatives.size();
			return ResultComparator.getF1(tp, fp, fn);
		}
		
		public CompareResult filterBy(RefactoringType... rts) {
			EnumSet<RefactoringType> refTypes = EnumSet.noneOf(RefactoringType.class);
			refTypes.addAll(Arrays.asList(rts));
			return filterBy(refTypes);
		}
		
		public CompareResult filterBy(EnumSet<RefactoringType> refTypes) {
			return new CompareResult(
				this.truePositives.stream().filter(r -> isOneOf(r, refTypes)).collect(Collectors.toList()),
				this.falsePositives.stream().filter(r -> isOneOf(r, refTypes)).collect(Collectors.toList()),
				this.falseNegatives.stream().filter(r -> isOneOf(r, refTypes)).collect(Collectors.toList()));
		}
		
		private boolean isOneOf(RefactoringRelationship r, EnumSet<RefactoringType> rts) {
			return rts.contains(r.getRefactoringType());
		}
		
		public void mergeWith(CompareResult other) {
			this.truePositives.addAll(other.truePositives);
			this.falsePositives.addAll(other.falsePositives);
			this.falseNegatives.addAll(other.falseNegatives);
		}
		
		public Collection<RefactoringRelationship> getTruePositives() {
			return truePositives;
		}
		
		public Collection<RefactoringRelationship> getFalsePositives() {
			return falsePositives;
		}
		
		public Collection<RefactoringRelationship> getFalseNegatives() {
			return falseNegatives;
		}
	}
	
	public boolean isIgnorePullUpToExtractedSupertype() {
		return ignorePullUpToExtractedSupertype;
	}
	
	public void setIgnorePullUpToExtractedSupertype(boolean ignorePullUpToExtractedSupertype) {
		this.ignorePullUpToExtractedSupertype = ignorePullUpToExtractedSupertype;
	}
	
	public boolean isIgnoreMoveToMovedType() {
		return ignoreMoveToMovedType;
	}
	
	public void setIgnoreMoveToMovedType(boolean ignoreMoveToMovedType) {
		this.ignoreMoveToMovedType = ignoreMoveToMovedType;
	}
	
	public boolean isIgnoreMoveToRenamedType() {
		return ignoreMoveToRenamedType;
	}
	
	public void setIgnoreMoveToRenamedType(boolean ignoreMoveToRenamedType) {
		this.ignoreMoveToRenamedType = ignoreMoveToRenamedType;
	}

	public void addFnExplanations(String project, String commit, Map<KeyPair, String> explanations) {
		String id = getProjectRevisionId(project, commit);
		RefactoringSet expected = this.expectedMap.get(id);
		if (expected != null) {
			Set<KeyPair> keyPairSet = expected.getRefactorings().stream()
				.map(r -> new KeyPair(r.getEntityBefore(), r.getEntityAfter()))
				.collect(Collectors.toSet());
			
			Map<KeyPair, String> filteredMap = explanations.entrySet().stream()
				.filter(e -> keyPairSet.contains(e.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			this.fnExplanations.put(id, filteredMap);
		}
	}
	
}
