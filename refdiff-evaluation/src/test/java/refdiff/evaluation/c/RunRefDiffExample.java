package refdiff.evaluation.c;

import java.util.Set;
import java.util.stream.Collectors;

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
		EvaluationUtils evaluationUtils = new EvaluationUtils(rastComparator, System.getProperty("java.io.tmpdir"));
		
		RastDiff diff = evaluationUtils.computeDiff("https://github.com/douban/beansdb.git", "e670a421928cfc5a842b355d096eb660d53743ea");
		
		Set<Relationship> relationships = diff.getRelationships().stream()
				.filter(relationship -> !relationship.getType().equals(RelationshipType.SAME))
				.collect(Collectors.toSet());
		
		System.out.println("Number of relationships found which are not of type SAME: " + relationships.size());
		
		relationships.stream()
			.forEach(relationship -> {System.out.println(relationship.toString());});
	}
	
}
