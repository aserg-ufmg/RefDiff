package refdiff.evaluation.js;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.eclipse.jgit.lib.Repository;

import refdiff.core.diff.CstComparator;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.Relationship;
import refdiff.core.io.GitHelper;
import refdiff.core.io.SourceFileSet;
import refdiff.core.util.PairBeforeAfter;
import refdiff.evaluation.ExternalProcess;
import refdiff.evaluation.CstComparatorDebbuger;
import refdiff.parsers.js.JsPlugin;

public class RunRefDiffExampleJs {
	
	public static void main(String[] args) throws Exception {
		runRefDiff("three.js", "73f083710d64acb493f55ba2c07e24c5a7f62899");
	}

	private static void runRefDiff(String projectName, String commit) throws Exception, IOException {
		File tempFolder = new File("tmp");
		tempFolder.mkdirs();
		
		String cloneUrl = "https://github.com/refdiff-study/" + projectName + ".git";
		File repoFolder = new File(tempFolder, projectName + ".git");
		
		if (!repoFolder.exists()) {
			ExternalProcess.execute(tempFolder, "git", "clone", cloneUrl, repoFolder.getPath(), "--bare", "--depth=1000");
		}
		
		GitHelper gh = new GitHelper();
		try (JsPlugin parser = new JsPlugin();
			Repository repo = gh.openRepository(repoFolder)) {
			CstComparator cstComparator = new CstComparator(parser);
			CstComparatorDebbuger debbuger = new CstComparatorDebbuger();
			PairBeforeAfter<SourceFileSet> sources = gh.getSourcesBeforeAndAfterCommit(repo, commit, parser.getAllowedFilesFilter());
			CstDiff diff = cstComparator.compare(sources.getBefore(), sources.getAfter(), debbuger);
			
			Set<Relationship> relationships = diff.getRelationships();
			
			relationships.stream()
				.forEach(relationship -> {
					System.out.println(relationship.toString());
				});
		}
	}
	
}
