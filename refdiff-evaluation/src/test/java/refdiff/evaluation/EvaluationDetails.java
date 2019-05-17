package refdiff.evaluation;

import refdiff.evaluation.icse.EvaluationCsvReader.ResultRow;

public class EvaluationDetails {
	public String resultA;
	public String commentA;
	public String resultB;
	public String commentB;
	public String resultC;
	public String commentC;
	public String resultFinal;
	public String commentFinal;
	public String evaluators;
	
	public EvaluationDetails(ResultRow row) {
		this.resultA = emptyIfNull(row.resultA);
		this.commentA = emptyIfNull(row.commentA);
		this.resultB = emptyIfNull(row.resultB);
		this.commentB = emptyIfNull(row.commentB);
		this.resultC = emptyIfNull(row.resultC);
		this.commentC = emptyIfNull(row.commentC);
		this.resultFinal = emptyIfNull(row.resultFinal);
		this.commentFinal = emptyIfNull(row.commentFinal);
		this.evaluators = emptyIfNull(row.evaluators);
	}
	
	private static String emptyIfNull(String nullableString) {
		return nullableString == null ? "" : nullableString;
	}
	
	public String format() {
		return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", resultA, commentA, resultB, commentB, resultC, commentC, resultFinal, commentFinal);
	}
}
