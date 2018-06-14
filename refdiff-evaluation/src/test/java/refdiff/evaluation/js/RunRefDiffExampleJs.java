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
import refdiff.parsers.js.EsprimaParser;

public class RunRefDiffExampleJs {
	
	public static void main(String[] args) throws Exception {
		
		EsprimaParser parser = new EsprimaParser();
		RastComparator rastComparator = new RastComparator(parser, parser);
		
		File tempFolder = new File("tmp");
		tempFolder.mkdirs();
		
		String cloneUrl = "https://github.com/angular/angular.js.git";
		File repoFolder = new File(tempFolder, "angular.js.git");
		
		if (!repoFolder.exists()) {
			ExternalProcess.execute(tempFolder, "git", "clone", cloneUrl, repoFolder.getPath(), "--bare", "--depth=1000");
		}
		
		GitHelper gh = new GitHelper();
		try (Repository repo = gh.openRepository(repoFolder)) {
			
			PairBeforeAfter<SourceFileSet> sources = gh.getSourcesBeforeAndAfterCommit(repo, "83f7980e2f2e9ea6a373a5e1e301edfc90c8daa8", parser.getAllowedFileExtensions());
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
