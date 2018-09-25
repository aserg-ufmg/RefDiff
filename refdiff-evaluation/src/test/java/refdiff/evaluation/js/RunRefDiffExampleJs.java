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
import refdiff.evaluation.RastComparatorDebbuger;
import refdiff.parsers.js.JsParser;

public class RunRefDiffExampleJs {
	
	public static void main(String[] args) throws Exception {
		File tempFolder = new File("tmp");
		tempFolder.mkdirs();
		
		String cloneUrl = "https://github.com/facebook/react.git";
		File repoFolder = new File(tempFolder, "react.git");
		
		if (!repoFolder.exists()) {
			ExternalProcess.execute(tempFolder, "git", "clone", cloneUrl, repoFolder.getPath(), "--bare", "--depth=1000");
		}
		
		// https://github.com/facebook/react/commit/366600d0b2b99ece8cd03d60e2a5454a02857502
		
		GitHelper gh = new GitHelper();
		try (JsParser parser = new JsParser();
			Repository repo = gh.openRepository(repoFolder)) {
			RastComparator rastComparator = new RastComparator(parser);
			RastComparatorDebbuger debbuger = new RastComparatorDebbuger();
			PairBeforeAfter<SourceFileSet> sources = gh.getSourcesBeforeAndAfterCommit(repo, "366600d0b2b99ece8cd03d60e2a5454a02857502", parser.getAllowedFilesFilter());
			RastDiff diff = rastComparator.compare(sources.getBefore(), sources.getAfter(), debbuger);
			
			Set<Relationship> relationships = diff.getRelationships();
			
			relationships.stream()
				.forEach(relationship -> {
					System.out.println(relationship.toString());
				});
		}
		
	}
	
}
