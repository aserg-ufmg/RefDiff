package refdiff.evaluation.js;

import java.io.File;
import java.util.Set;

import org.eclipse.jgit.lib.Repository;

import refdiff.core.diff.RastComparator;
import refdiff.core.diff.RastDiff;
import refdiff.core.diff.Relationship;
import refdiff.core.io.GitHelper;
import refdiff.core.io.SourceFileSet;
import refdiff.core.util.PairBeforeAfter;
import refdiff.evaluation.ExternalProcess;
import refdiff.parsers.js.JsParser;

public class RunRefDiffExampleJs {
	
	public static void main(String[] args) throws Exception {
		File tempFolder = new File("tmp");
		tempFolder.mkdirs();
		
		String cloneUrl = "https://github.com/refdiff-data/webpack.git";
		File repoFolder = new File(tempFolder, "react.git");
		
		if (!repoFolder.exists()) {
			ExternalProcess.execute(tempFolder, "git", "clone", cloneUrl, repoFolder.getPath(), "--bare", "--depth=1000");
		}
		
		GitHelper gh = new GitHelper();
		try (JsParser parser = new JsParser();
			Repository repo = gh.openRepository(repoFolder)) {
			RastComparator rastComparator = new RastComparator(parser);
			
			PairBeforeAfter<SourceFileSet> sources = gh.getSourcesBeforeAndAfterCommit(repo, "b50d4cf7c370dc0f9fa2c39ea0e73e28ca8918ac", parser.getAllowedFilesFilter());
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
