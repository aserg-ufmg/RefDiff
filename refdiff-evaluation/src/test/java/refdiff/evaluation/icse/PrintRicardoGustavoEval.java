package refdiff.evaluation.icse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import refdiff.evaluation.icse.EvaluationCsvReader.ResultCommit;
import refdiff.evaluation.icse.EvaluationCsvReader.ResultRow;

public class PrintRicardoGustavoEval {
	
	public static void main(String[] args) throws Exception {
		List<ResultCommit> eval = EvaluationCsvReader.readEvalRicardoGustavo();
		Map<String, Integer> counter = new LinkedHashMap<>();
		counter.put("TP/TP -> TP", 0);
		counter.put("FP/FP -> FP", 0);
		counter.put("? -> TP", 0);
		counter.put("? -> FP", 0);
		
		for (ResultCommit rc : eval) {
			for (ResultRow row : rc.rows) {
				if ("FP?".equals(row.result1)) {
					String resultA = row.resultA.replace("?", "");
					String resultB = row.resultB.replace("?", "");
					String result;
					if (resultA.equals(resultB)) {
						result = String.format("%s/%s -> %s", resultA, resultB, resultB);
					} else {
						result = String.format("? -> %s", row.resultFinal);
					}
					counter.put(result, counter.get(result) + 1);
				}
			}
		}
		
		for (Entry<String, Integer> entry : counter.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
	}
	
}
