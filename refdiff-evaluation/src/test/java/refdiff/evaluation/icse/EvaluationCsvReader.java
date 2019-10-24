package refdiff.evaluation.icse;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import refdiff.evaluation.EvaluationDetails;
import refdiff.evaluation.RefactoringDescriptionParser;
import refdiff.evaluation.RefactoringRelationship;
import refdiff.evaluation.RefactoringSet;
import refdiff.evaluation.RefactoringType;
import refdiff.evaluation.ResultComparator;

public class EvaluationCsvReader {
	public static void main(String[] args) throws Exception {
		eval();
	}
	
	public static ResultComparator buildResultComparator(IcseDataset data, List<ResultCommit> list) throws FileNotFoundException, IOException {
		ResultComparator rc2 = new ResultComparator();
		Map<String, RefactoringSet> mapRs = new HashMap<>();
		Map<String, RefactoringSet> mapRsNotExpected = new HashMap<>();
		for (RefactoringSet rs : data.getExpected()) {
			RefactoringSet rs2 = new RefactoringSet(rs.getProject(), rs.getRevision());
			mapRs.put(rs.getRevision(), rs2);
			rs2.add(rs.getRefactorings());
			rc2.expect(rs2);
		}
		for (RefactoringSet rs : data.getNotExpected()) {
			RefactoringSet rs2 = new RefactoringSet(rs.getProject(), rs.getRevision());
			mapRsNotExpected.put(rs.getRevision(), rs2);
			rs2.add(rs.getRefactorings());
			rc2.dontExpect(rs2);
		}
		if (list != null) {
			for (ResultCommit commitResult : list) {
				String url = commitResult.commitUrl;
				String commit = url.substring(url.lastIndexOf("/") + 1);
				
				RefactoringSet expectedRefactorings = mapRs.get(commit);
				RefactoringSet notExpectedRefactorings = mapRsNotExpected.get(commit);
				for (ResultRow row : commitResult.rows) {
					boolean evaluatedAsTp = "TP".equals(row.resultFinal);
					if (evaluatedAsTp) {
						RefactoringType refType = RefactoringType.fromName(row.refType);
						RefactoringRelationship tpInstance = new RefactoringRelationship(refType, row.n1, row.n2);
						tpInstance.setEvaluationDetails(new EvaluationDetails(row));
						tpInstance.setDescription(RefactoringDescriptionParser.format(refType, row.n1, row.n2));
						if (!notExpectedRefactorings.getRefactorings().contains(tpInstance)) {
							expectedRefactorings.add(tpInstance);
							//System.out.println("ERROR TP: " + tpInstance.toString());
						}
					}
					boolean evaluatedAsFp = "FP".equals(row.resultFinal) || "FP?".equals(row.resultFinal);
					if (evaluatedAsFp) {
						RefactoringType refType = RefactoringType.fromName(row.refType);
						RefactoringRelationship fpInstance = new RefactoringRelationship(refType, row.n1, row.n2);
						fpInstance.setEvaluationDetails(new EvaluationDetails(row));
						fpInstance.setDescription(RefactoringDescriptionParser.format(refType, row.n1, row.n2));
						if (!expectedRefactorings.getRefactorings().contains(fpInstance)) {
							notExpectedRefactorings.add(fpInstance);
							//System.out.println("ERROR FP: " + fpInstance.toString());
						}
					}
				}
			}
		}
		return rc2;
	}
	
	public static void eval() throws Exception {
		
		IcseDataset data = new IcseDataset();
		List<ResultCommit> list = readEvalRicardoGustavo();
		
		ResultComparator rc = buildResultComparator(data, null);
		ResultComparator rc2 = buildResultComparator(data, list);
		
		for (RefactoringSet rs : data.getExpected()) {
			String project = rs.getProject();
			String commit = rs.getRevision();
			rc.compareWith("RefDiff", new RefactoringSet(project, commit));
			rc2.compareWith("RefDiff", new RefactoringSet(project, commit));
		}
		
		Map<String, ResultRow> map = new HashMap<>();
		for (ResultCommit commitResult : list) {
			String url = commitResult.commitUrl;
			String project = url.substring(0, url.lastIndexOf("/commit/")) + ".git";
			String commit = url.substring(url.lastIndexOf("/") + 1);
			
			RefactoringSet rs = new RefactoringSet(project, commit);
			
			// System.out.println(commitResult.commitUrl);
			for (ResultRow row : commitResult.rows) {
				// System.out.println(row);
				if (!row.description.isEmpty()) {
					RefactoringType refType = RefactoringType.fromName(row.refType);
					rs.add(new RefactoringRelationship(refType, row.n1, row.n2));
					map.put(getKey(commit, refType, row.n1, row.n2), row);
				}
			}
			
			rc.compareWith("RefDiff", rs);
			rc2.compareWith("RefDiff", rs);
		}
		
		rc2.compareWith("RMiner", data.getrMinerRefactorings());
		
		rc.printDetails(System.out, RunIcseEval.refactoringTypes, "RefDiff", (RefactoringSet expected, RefactoringRelationship r, String label, String cause, EvaluationDetails evaluationDetails) -> {
			ResultRow row = map.get(getKey(expected.getRevision(), r.getRefactoringType(), r.getEntityBefore(), r.getEntityAfter()));
			if (row != null) {
				System.out.printf("\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", row.description, label, row.resultA, row.commentA, row.resultB, row.commentB, row.getResult2(label), row.resultC, row.commentC, row.resultFinal, cause != null ? cause : row.commentFinal, row.getResult3(label));
			} else {
				System.out.printf("\t\t%s\t\t\t\t\t%s\t\t\t\t\t%s", label, label, label);
			}
		});
		System.out.println();
		rc.printSummary(System.out, RunIcseEval.refactoringTypes);
		
		rc2.printSummary(System.out, RunIcseEval.refactoringTypes);
	}
	
	private static String getKey(String commit, RefactoringType refType, String n1, String n2) {
		return commit + " " + refType.name() + " " + n1 + " " + n2;
	}
	
	public static List<ResultCommit> readEvalRicardoGustavo() throws IOException, FileNotFoundException {
		List<ResultCommit> list = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("data/java-evaluation/FixExtractBefore.txt"), StandardCharsets.UTF_8))) {
			String line;
			String commitUrl = "";
			ResultCommit resultCommit = null;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\t", -1);
				String c0 = parts[0];
				if (c0.startsWith("https://")) {
					commitUrl = c0;
					resultCommit = new ResultCommit();
					list.add(resultCommit);
					resultCommit.commitUrl = commitUrl;
				} else {
					ResultRow row = new ResultRow();
					row.commitUrl = commitUrl;
					row.refType = parts[0];
					row.n1 = parts[1];
					row.n2 = parts[2];
					row.description = parts[3];
					row.result1 = parts[4];
					row.resultA = parts[5];
					row.commentA = parts[6];
					row.resultB = parts[7];
					row.commentB = parts[8];
					row.resultC = parts[10];
					row.commentC = parts[11];
					row.resultFinal = parts[12];
					computeResultFinal(row);
					row.commentFinal = parts[13];
					//row.evaluators = "FP?".equals(row.result1) ? String.format("Gustavo/Ricardo: %s/%s", row.resultA, row.resultB) : "";
					if ("FP?".equals(row.result1)) {						
						row.evaluators = "Gustavo/Ricardo";
					}
					resultCommit.rows.add(row);
				}
			}
		}
		return list;
	}
	
	public static List<ResultCommit> readEvalGustavoRicardoDaniloAndMerge() throws IOException, FileNotFoundException {
		List<ResultRow> listGustavo = CsvReader.readCsv("data/java-evaluation/eval-gustavo-2.txt", parts -> {
			ResultRow row = new ResultRow();
			row.commitUrl = parts[0];
			row.refType = parts[1];
			row.n1 = parts[2];
			row.n2 = parts[3];
			row.resultA = parts[4];
			row.commentA = parts[5];
			row.evaluators = "Gustavo/Danilo";
			return row;
		});
		
		List<ResultRow> listRicardo = CsvReader.readCsv("data/java-evaluation/eval-ricardo-2.txt", parts -> {
			ResultRow row = new ResultRow();
			row.commitUrl = parts[0];
			row.refType = parts[1];
			row.n1 = parts[2];
			row.n2 = parts[3];
			row.resultB = parts[4];
			row.commentB = parts[5];
			row.evaluators = "Ricardo/Danilo";
			return row;
		});
		
		List<ResultRow> listDanilo = CsvReader.readCsv("data/java-evaluation/eval-danilo.txt", parts -> {
			ResultRow row = new ResultRow();
			row.commitUrl = parts[0];
			row.refType = parts[1];
			row.n1 = parts[2];
			row.n2 = parts[3];
			row.resultC = parts[4];
			row.commentC = parts[5];
			return row;
		});
		
		List<ResultRow> listConsensus = CsvReader.readCsv("data/java-evaluation/eval-phase2.txt", parts -> {
			ResultRow row = new ResultRow();
			row.commitUrl = parts[0];
			row.refType = parts[1];
			row.n1 = parts[2];
			row.n2 = parts[3];
			row.resultFinal = parts[4];
			row.commentFinal = parts[5];
			return row;
		});
		
		Stream<ResultRow> allRows = Stream.concat(Stream.concat(Stream.concat(listGustavo.stream(), listRicardo.stream()), listDanilo.stream()), listConsensus.stream());
		Map<String, List<ResultRow>> map = allRows.collect(Collectors.groupingBy(row -> String.format("%s\t%s\t%s\t%s", row.commitUrl, row.refType, row.n1, row.n2)));
		Map<String, List<ResultRow>> map2 = map.entrySet().stream().map(e -> {
			ResultRow row = new ResultRow();
			for (ResultRow partialRow : e.getValue()) {
				row.commitUrl = partialRow.commitUrl;
				row.refType = partialRow.refType;
				row.n1 = partialRow.n1;
				row.n2 = partialRow.n2;
				if (partialRow.resultA != null && !partialRow.resultA.isEmpty()) {
					row.resultA = partialRow.resultA;
					row.commentA = partialRow.commentA;
				}
				if (partialRow.resultB != null && !partialRow.resultB.isEmpty()) {
					row.resultB = partialRow.resultB;
					row.commentB = partialRow.commentB;
				}
				if (partialRow.resultC != null && !partialRow.resultC.isEmpty()) {
					row.resultC = partialRow.resultC;
					row.commentC = partialRow.commentC;
				}
				if (partialRow.resultFinal != null && !partialRow.resultFinal.isEmpty()) {
					row.resultFinal = partialRow.resultFinal;
					row.commentFinal = partialRow.commentFinal;
				}
				if (partialRow.evaluators != null) {
					row.evaluators = partialRow.evaluators;
				}
			}
			computeResultFinal(row);
			return row;
		}).collect(Collectors.groupingBy(row -> row.commitUrl));
		return map2.entrySet().stream().map(e -> {
			ResultCommit resultCommit = new ResultCommit();
			resultCommit.commitUrl = e.getKey();
			resultCommit.rows = e.getValue();
			return resultCommit; 
		}).collect(Collectors.toList());
	}
	
	private static void computeResultFinal(ResultRow row) {
		if (isEmpty(row.resultFinal)) {
			if (("TP".equals(row.resultA) && "TP".equals(row.resultB)) || ("TP".equals(row.resultA) && "TP".equals(row.resultC)) || ("TP".equals(row.resultB) && "TP".equals(row.resultC))) {
				row.resultFinal = "TP";
			} else if (("FP".equals(row.resultA) && "FP".equals(row.resultB)) || ("FP".equals(row.resultA) && "FP".equals(row.resultC)) || ("FP".equals(row.resultB) && "FP".equals(row.resultC))) {
				row.resultFinal = "FP";
			} else if (!isEmpty(row.resultA) || !isEmpty(row.resultB) || !isEmpty(row.resultB)) {
				row.resultFinal = "FP?";
			}
		}
		if (!isEmpty(row.resultA) && !isEmpty(row.resultB)) {
			row.evaluators = "Gustavo/Ricardo";
		} else if (!isEmpty(row.resultA) && !isEmpty(row.resultC)) {
			row.evaluators = "Gustavo/Danilo";
		} else if (!isEmpty(row.resultB) && !isEmpty(row.resultC)) {
			row.evaluators = "Ricardo/Danilo";
		}
	}
	
	private static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}
	
	public static List<ResultCommit> readEvalAll() throws IOException, FileNotFoundException {
		List<ResultCommit> list = new ArrayList<>();
		list.addAll(readEvalRicardoGustavo());
		list.addAll(readEvalGustavoRicardoDaniloAndMerge());
		return list;
	}
	
	public static class ResultCommit {
		public String commitUrl;
		public List<ResultRow> rows = new ArrayList<>();
	}
	
	public static class ResultRow {
		public String commitUrl;
		public String refType;
		public String n1;
		public String n2;
		public String description;
		public String result1;
		public String resultA;
		public String commentA;
		public String resultB;
		public String commentB;
		public String resultC;
		public String commentC;
		public String commentFinal;
		public String resultFinal;
		public String evaluators;
		
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return String.format("%s\t%s\t%s\t%s\t%s\t%s", n1, n2, resultA, commentA, resultB, commentB);
		}
		
		public String getResult2(String result1) {
			if (result1.equals("FP?")) {
				if (resultA.equals(resultB)) {
					return resultA;
				} else {
					return "FP?";
				}
			} else {
				return result1;
			}
		}
		
		public String getResult3(String result1) {
			if (resultFinal.isEmpty()) {
				return getResult2(result1);
			} else {
				return resultFinal;
			}
		}
	}
	
}
