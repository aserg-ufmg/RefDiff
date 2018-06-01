package refdiff.evaluation.c;

import refdiff.core.diff.RastComparator;
import refdiff.core.diff.RastDiff;
import refdiff.core.diff.Relationship;
import refdiff.core.diff.RelationshipType;
import refdiff.evaluation.EvaluationUtils;
import refdiff.parsers.c.CParser;

public class RunRefDiffExample {
	
	public static void main(String[] args) throws Exception {
		
		CParser parser = new CParser();
		RastComparator rastComparator = new RastComparator(parser, parser);
		EvaluationUtils evaluationUtils = new EvaluationUtils(rastComparator, "D:/tmp/");
		
		RastDiff diff = evaluationUtils.computeDiff("https://github.com/douban/beansdb.git", "e670a421928cfc5a842b355d096eb660d53743ea");
		
		for (Relationship rel : diff.getRelationships()) {
			if (!rel.getType().equals(RelationshipType.SAME)) {
				System.out.println(rel.toString());
			}
		}
		
	}
	
}
