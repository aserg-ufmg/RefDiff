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

public class ResultComparator {
	
	Set<String> groupIds = new LinkedHashSet<>();
	Map<String, RefactoringSet> expectedMap = new LinkedHashMap<>();
	Map<String, RefactoringSet> notExpectedMap = new LinkedHashMap<>();
	Map<String, CompareResult> resultMap = new HashMap<>();
	
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
		compareWith(groupId, actual, new HashMap<>());
	}
	
	public void compareWith(String groupId, RefactoringSet actual, Map<KeyPair, String> fnExplanation) {
		groupIds.add(groupId);
		resultMap.put(getResultId(actual.getProject(), actual.getRevision(), groupId), computeResult(actual, fnExplanation));
	}
	
	public CompareResult computeResult(RefactoringSet actual, Map<KeyPair, String> fnExplanationMap) {
		List<RefactoringRelationship> truePositives = new ArrayList<>();
		List<RefactoringRelationship> falsePositives = new ArrayList<>();
		List<RefactoringRelationship> falseNegatives = new ArrayList<>();
		Map<RefactoringRelationship, String> details = new HashMap<>();
		
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
					String fpCause = findFpCause(r, expectedUnfiltered);
					if (fpCause != null) {
						details.put(r, fpCause);
					}
				}
			}
		}
		for (RefactoringRelationship r : expectedRefactorings) {
			falseNegatives.add(r);
			String fnCause = fnExplanationMap.get(new KeyPair(r.getEntityBefore(), r.getEntityAfter()));
			if (fnCause != null) {
				details.put(r, fnCause);
			}
		}
		
		return new CompareResult(truePositives, falsePositives, falseNegatives, details);
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
		printDetails(out, refTypesToConsider, groupId, (RefactoringSet rs, RefactoringRelationship r, String label, String cause) -> {
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
			
			String header = String.format("Ref Type\tEntity before\tEntity after\t%s\tDetails", groupId);
			
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
				out.println(getProjectRevisionId(expected.getProject(), expected.getRevision()));
				ArrayList<RefactoringRelationship> allList = new ArrayList<>();
				allList.addAll(all);
				Collections.sort(allList);
				for (RefactoringRelationship r : allList) {
					out.print(format(r));
					//out.print('\t');
					if (result != null) {
						Set<RefactoringRelationship> actualRefactorings = new HashSet<>();
						actualRefactorings.addAll(result.getTruePositives());
						actualRefactorings.addAll(result.getFalsePositives());
						int correct = expectedRefactorings.contains(r) ? 2 : 0;
						int found = actualRefactorings.contains(r) ? 1 : 0;
						String label = labels[correct + found];
						String cause = result.getDetails(r);
						if (label == "FP" && cause == null && !notExpectedRefactorings.contains(r)) {
							label = label + "?";
						}
						//out.print(label);
						rowPrinter.printDetails(expected, r, label, cause);
						/*
						if (label.equals("FP") || label.equals("FN")) {
							if (cause != null) {
								out.print('\t');
								out.print(cause);
							}
						}
						*/
					}
					out.println();
				}
			}
		}
		out.println();
	}
	
	public static String format(RefactoringRelationship r) {
		return String.format("%s\t%s\t%s", r.getRefactoringType().getDisplayName(), r.getEntityBefore(), r.getEntityAfter());
	}

	private String findFpCause(RefactoringRelationship r, Set<RefactoringRelationship> expectedUnfiltered) {
		if (isPullUpToExtractedSupertype(r, expectedUnfiltered)) {
			return "<ES>";
		}
		if (isMoveToRenamedType(r, expectedUnfiltered)) {
			return "<RT>";
		}
		if (isMoveToMovedType(r, expectedUnfiltered)) {
			return "<MT>";
		}
		if (r.getRefactoringType() == RefactoringType.MOVE_ATTRIBUTE || r.getRefactoringType() == RefactoringType.MOVE_OPERATION) {
			if (expectedUnfiltered.contains(new RefactoringRelationship(RefactoringType.EXTRACT_SUPERCLASS, parentOf(r.getEntityBefore()), parentOf(r.getEntityAfter())))) {
				return "<ES>";
			}
			if (expectedUnfiltered.contains(new RefactoringRelationship(RefactoringType.EXTRACT_INTERFACE, parentOf(r.getEntityBefore()), parentOf(r.getEntityAfter())))) {
				return "<ES>";
			}
			if (expectedUnfiltered.contains(new RefactoringRelationship(RefactoringType.PULL_UP_ATTRIBUTE, (r.getEntityBefore()), (r.getEntityAfter())))) {
				return "<PUF>";
			}
			if (expectedUnfiltered.contains(new RefactoringRelationship(RefactoringType.PUSH_DOWN_ATTRIBUTE, (r.getEntityBefore()), (r.getEntityAfter())))) {
				return "<PDF>";
			}
			if (expectedUnfiltered.contains(new RefactoringRelationship(RefactoringType.PULL_UP_OPERATION, (r.getEntityBefore()), (r.getEntityAfter())))) {
				return "<PUM>";
			}
			if (expectedUnfiltered.contains(new RefactoringRelationship(RefactoringType.PUSH_DOWN_OPERATION, (r.getEntityBefore()), (r.getEntityAfter())))) {
				return "<PDM>";
			}
		}
		return null;
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
		private final Map<RefactoringRelationship, String> details;
		
		public CompareResult(Collection<RefactoringRelationship> truePositives, Collection<RefactoringRelationship> falsePositives, Collection<RefactoringRelationship> falseNegatives) {
			this(truePositives, falsePositives, falseNegatives, new HashMap<>());
		}
		
		public CompareResult(Collection<RefactoringRelationship> truePositives, Collection<RefactoringRelationship> falsePositives, Collection<RefactoringRelationship> falseNegatives, Map<RefactoringRelationship, String> details) {
			this.truePositives = truePositives;
			this.falsePositives = falsePositives;
			this.falseNegatives = falseNegatives;
			this.details = details;
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
				this.falseNegatives.stream().filter(r -> isOneOf(r, refTypes)).collect(Collectors.toList()),
				this.details);
		}
		
		private boolean isOneOf(RefactoringRelationship r, EnumSet<RefactoringType> rts) {
			return rts.contains(r.getRefactoringType());
		}
		
		public void mergeWith(CompareResult other) {
			this.truePositives.addAll(other.truePositives);
			this.falsePositives.addAll(other.falsePositives);
			this.falseNegatives.addAll(other.falseNegatives);
			this.details.putAll(other.details);
		}
		
		public String getDetails(RefactoringRelationship r) {
			return details.get(r);
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
	
}
