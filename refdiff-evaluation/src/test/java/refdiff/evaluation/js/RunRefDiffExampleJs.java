package refdiff.evaluation.js;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Repository;

import refdiff.core.diff.RastComparator;
import refdiff.core.diff.RastDiff;
import refdiff.core.diff.Relationship;
import refdiff.core.diff.RelationshipType;
import refdiff.core.io.GitHelper;
import refdiff.core.io.SourceFileSet;
import refdiff.core.util.PairBeforeAfter;
import refdiff.evaluation.ExternalProcess;
import refdiff.parsers.js.JsParser;

public class RunRefDiffExampleJs {
	
	public static void main(String[] args) throws Exception {
		
		JsParser parser = new JsParser();
		RastComparator rastComparator = new RastComparator(parser, parser);
		
		File tempFolder = new File("tmp");
		tempFolder.mkdirs();
		
		String cloneUrl = "https://github.com/refdiff-data/vue.git";
		File repoFolder = new File(tempFolder, "vue.git");
		
		if (!repoFolder.exists()) {
			ExternalProcess.execute(tempFolder, "git", "clone", cloneUrl, repoFolder.getPath(), "--bare", "--depth=1000");
		}
		
		GitHelper gh = new GitHelper();
		try (Repository repo = gh.openRepository(repoFolder)) {
			
			PairBeforeAfter<SourceFileSet> sources = gh.getSourcesBeforeAndAfterCommit(repo, "ef0b25097957ae9ef9970be732d6e65cc78902e9", parser.getAllowedFilesFilter());
			RastDiff diff = rastComparator.compare(sources.getBefore(), sources.getAfter());
			
			Set<Relationship> relationships = diff.getRelationships();
//			.stream()
//				.filter(relationship -> !relationship.getType().equals(RelationshipType.SAME))
//				.collect(Collectors.toSet());
			
			relationships.stream()
				.forEach(relationship -> {
					System.out.println(relationship.toString());
				});
		}
		
	}
	
}
