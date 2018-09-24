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
		
		String cloneUrl = "https://github.com/mrdoob/three.js.git";
		File repoFolder = new File(tempFolder, "three.js.git");
		
		if (!repoFolder.exists()) {
			ExternalProcess.execute(tempFolder, "git", "clone", cloneUrl, repoFolder.getPath(), "--bare", "--depth=1000");
		}
		
		// https://github.com/mrdoob/three.js/commit/f0e7bdc1de54a1b896089d819872111a86aa4185
		
		GitHelper gh = new GitHelper();
		try (JsParser parser = new JsParser();
			Repository repo = gh.openRepository(repoFolder)) {
			RastComparator rastComparator = new RastComparator(parser);
			RastComparatorDebbuger debbuger = new RastComparatorDebbuger();
			PairBeforeAfter<SourceFileSet> sources = gh.getSourcesBeforeAndAfterCommit(repo, "f0e7bdc1de54a1b896089d819872111a86aa4185", parser.getAllowedFilesFilter());
			RastDiff diff = rastComparator.compare(sources.getBefore(), sources.getAfter(), debbuger);
			
			Set<Relationship> relationships = diff.getRelationships();
			
			relationships.stream()
				.forEach(relationship -> {
					System.out.println(relationship.toString());
				});
		}
		
	}
	
}
