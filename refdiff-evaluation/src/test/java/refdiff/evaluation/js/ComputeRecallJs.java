package refdiff.evaluation.js;

import static org.junit.Assert.*;
import static refdiff.test.util.RastDiffMatchers.*;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.lib.Repository;

import refdiff.core.diff.RastComparator;
import refdiff.core.diff.RastDiff;
import refdiff.core.diff.RelationshipType;
import refdiff.core.io.GitHelper;
import refdiff.core.io.SourceFileSet;
import refdiff.core.util.PairBeforeAfter;
import refdiff.evaluation.ExternalProcess;
import refdiff.parsers.js.JsParser;

public class ComputeRecallJs {
	
	private static final File tempFolder = new File("tmp");
	
	public static void main(String[] args) throws Exception {
		tempFolder.mkdirs();
		
		assertThat(
			diff("https://github.com/webpack/webpack/commit/b50d4cf7c370dc0f9fa2c39ea0e73e28ca8918ac"),
			contains(relationship(RelationshipType.MOVE, node("lib/lib/WebpackOptionsValidationError.js", "getSchemaPartText"), node("lib/util/lib/util/getSchemaPartText.js", "getSchemaPartText")))
		);
		
		assertThat(
			diff("https://github.com/atom/atom/commit/fc620b9e80d67ca99f962431461b8fc4d085d9df"),
			contains(relationship(RelationshipType.MOVE, node("spec/tooltip-manager-spec.js", "", "createElement"), node("spec/tooltip-manager-spec.js", "createElement")))
		);
	}
	
	private static RastDiff diff(String commitUrl) throws Exception, IOException {
		String[] url = commitUrl.split("/commit/");
		String commit = url[1];
		String project = url[0].substring(url[0].lastIndexOf("/") + 1);
		File repoFolder = new File(tempFolder, project + ".git");
		String cloneUrl = "https://github.com/refdiff-data/" + project + ".git";
		
		if (!repoFolder.exists()) {
			ExternalProcess.execute(tempFolder, "git", "clone", cloneUrl, repoFolder.getPath(), "--bare", "--depth=1000");
		}
		//ExternalProcess.execute(repoFolder, "git", "fetch", "--depth=5000");
		
		GitHelper gh = new GitHelper();
		try (JsParser parser = new JsParser();
			Repository repo = gh.openRepository(repoFolder)) {
			RastComparator rastComparator = new RastComparator(parser);
			
			PairBeforeAfter<SourceFileSet> sources = gh.getSourcesBeforeAndAfterCommit(repo, commit, parser.getAllowedFilesFilter());
			return rastComparator.compare(sources.getBefore(), sources.getAfter());
		}
	}
	
}
