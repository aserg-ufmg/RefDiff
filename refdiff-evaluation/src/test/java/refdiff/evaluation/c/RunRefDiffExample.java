package refdiff.evaluation.c;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Repository;

import refdiff.core.diff.CstComparator;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.Relationship;
import refdiff.core.diff.RelationshipType;
import refdiff.core.io.GitHelper;
import refdiff.core.io.SourceFileSet;
import refdiff.core.util.PairBeforeAfter;
import refdiff.evaluation.ExternalProcess;
import refdiff.parsers.c.CPlugin;

public class RunRefDiffExample {
	
	public static void main(String[] args) throws Exception {
		
		CPlugin parser = new CPlugin();
		CstComparator cstComparator = new CstComparator(parser);
		
		String tempFolder = "D:/tmp";
		String cloneUrl = "https://github.com/torvalds/linux.git";
		File repoFolder = new File(tempFolder, "linux.git");
		
		if (!repoFolder.exists()) {
			ExternalProcess.execute(new File(tempFolder), "git", "clone", cloneUrl, repoFolder.getPath(), "--bare", "--depth=1000");
		}
		
		GitHelper gh = new GitHelper();
		try (Repository repo = gh.openRepository(repoFolder)) {
			
			PairBeforeAfter<SourceFileSet> sources = gh.getSourcesBeforeAndAfterCommit(repo, "f72c3ab791ac0b2b75b5b5d4d51d8eb89ea1e515", parser.getAllowedFilesFilter());
			CstDiff diff = cstComparator.compare(sources.getBefore(), sources.getAfter());
			
			Set<Relationship> relationships = diff.getRelationships().stream()
				.filter(relationship -> !relationship.getType().equals(RelationshipType.SAME))
				.collect(Collectors.toSet());
			
			relationships.stream()
				.forEach(relationship -> {
					System.out.println(relationship.toString());
				});
		}
		
	}
	
}
