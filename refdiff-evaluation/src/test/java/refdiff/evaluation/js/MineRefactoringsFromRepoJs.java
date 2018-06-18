package refdiff.evaluation.js;

import java.io.File;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import refdiff.core.diff.RastComparator;
import refdiff.core.diff.RastDiff;
import refdiff.core.diff.Relationship;
import refdiff.core.diff.RelationshipType;
import refdiff.core.io.GitHelper;
import refdiff.core.io.SourceFileSet;
import refdiff.core.util.PairBeforeAfter;
import refdiff.evaluation.ExternalProcess;
import refdiff.parsers.js.EsprimaParser;

public class MineRefactoringsFromRepoJs {
	
	public static void main(String[] args) throws Exception {
		
		File tempFolder = new File("tmp");
		tempFolder.mkdirs();
		
		//mineRepository(tempFolder, "https://github.com/refdiff-data/react.git");
		mineRepository(tempFolder, "https://github.com/refdiff-data/vue.git");
//		mineRepository(tempFolder, "https://github.com/d3/d3.git");
		//mineRepository(tempFolder, "https://github.com/angular/angular.js.git");
	}

	private static void mineRepository(File tempFolder, String cloneUrl) throws Exception {
		String projectName = cloneUrl.substring(cloneUrl.lastIndexOf('/') + 1);
		File repoFolder = new File(tempFolder, projectName);
		
		
		if (!repoFolder.exists()) {
			ExternalProcess.execute(tempFolder, "git", "clone", cloneUrl, projectName, "--bare", "--depth=1000");
		}
		
		EsprimaParser parser = new EsprimaParser();
		RastComparator rastComparator = new RastComparator(parser, parser);
		GitHelper gh = new GitHelper();
		
		try (Repository repository = gh.openRepository(repoFolder)) {
			
			gh.forEachNonMergeCommit(repository, 200, (RevCommit commitBefore, RevCommit commitAfter) -> {
				System.out.println(commitAfter.getId().getName());
				
				try {
					PairBeforeAfter<SourceFileSet> sources = gh.getSourcesBeforeAndAfterCommit(repository, commitBefore, commitAfter, parser.getAllowedFileExtensions());
					RastDiff diff = rastComparator.compare(sources.getBefore(), sources.getAfter());
					
					for (Relationship relationship : diff.getRelationships()) {
						if (relationship.getType() != RelationshipType.SAME) {
							System.out.println("  " + relationship);
						}
					}
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
				
			});
		}
	}
	
}
