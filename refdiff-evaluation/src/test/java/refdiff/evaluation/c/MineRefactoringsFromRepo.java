package refdiff.evaluation.c;

import java.io.File;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import refdiff.core.diff.CstComparator;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.Relationship;
import refdiff.core.diff.RelationshipType;
import refdiff.core.io.GitHelper;
import refdiff.core.io.SourceFileSet;
import refdiff.core.util.PairBeforeAfter;
import refdiff.evaluation.ExternalProcess;
import refdiff.parsers.c.CPlugin;

public class MineRefactoringsFromRepo {
	
	public static void main(String[] args) throws Exception {
		
		String tempFolder = "D:/tmp";
		String cloneUrl = "https://github.com/torvalds/linux.git";
		File repoFolder = new File(tempFolder, "linux.git");
		
		if (!repoFolder.exists()) {
			ExternalProcess.execute(new File(tempFolder), "git", "clone", cloneUrl, repoFolder.getPath(), "--bare", "--depth=1000");
		}
		
		CPlugin parser = new CPlugin();
		CstComparator cstComparator = new CstComparator(parser);
		GitHelper gh = new GitHelper();
		
		try (Repository repository = gh.openRepository(repoFolder)) {
			
			gh.forEachNonMergeCommit(repository, 300, (RevCommit commitBefore, RevCommit commitAfter) -> {
				System.out.println(commitAfter.getId().getName());
				
				try {
					PairBeforeAfter<SourceFileSet> sources = gh.getSourcesBeforeAndAfterCommit(repository, commitBefore, commitAfter, parser.getAllowedFilesFilter());
					CstDiff diff = cstComparator.compare(sources);
					
					for (Relationship relationship : diff.getRelationships()) {
						if (relationship.getType() != RelationshipType.SAME) {
							System.out.println("  " + relationship);
						}
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
			});
		}
	}
	
}
