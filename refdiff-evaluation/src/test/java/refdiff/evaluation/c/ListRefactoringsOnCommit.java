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

public class ListRefactoringsOnCommit {
	
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("You need enter params <Repository Full Name> <Commit SHA>");
			return;
		}
		
		String repositoryFullName = args[0];
		String commitSHA = args[1];
		
		String[] repositoryNameParts = repositoryFullName.split("/");
		
		String repositoryOwner = repositoryNameParts[0];
		String repositoryName = repositoryNameParts[1];
		
		String repositoryUrl = "https://github.com/" + repositoryOwner + "/" + repositoryName + ".git";
		
		String tempFolder = System.getProperty("java.io.tmpdir");
		String repoFolderPath = tempFolder + repositoryName + ".git";
		File repoFolder = new File(tempFolder, repositoryName + ".git");
		
		if (!repoFolder.exists()) {
			System.out.println("Cloning " + repositoryUrl + " into " + repoFolderPath);
			ExternalProcess.execute(new File(tempFolder), "git", "clone", repositoryUrl, repoFolder.getPath(), "--bare", "--depth=1000");
//			ExternalProcess.execute(new File(tempFolder), "git", "clone", repositoryUrl, repoFolder.getPath(), "--bare", "--shallow-since=2007-01-01");
		}
		
		CPlugin parser = new CPlugin();
		CstComparator cstComparator = new CstComparator(parser);
		GitHelper gh = new GitHelper();
		
		try (Repository repo = gh.openRepository(repoFolder)) {
			
			PairBeforeAfter<SourceFileSet> sources = gh.getSourcesBeforeAndAfterCommit(
					repo, commitSHA, parser.getAllowedFilesFilter());
			CstDiff diff = cstComparator.compare(sources);
			
			Set<Relationship> relationships = diff.getRelationships().stream()
				.filter(relationship -> !relationship.getType().equals(RelationshipType.SAME))
				.collect(Collectors.toSet());
			
			System.out.println(relationships.size() + " relationships detected");
			
			relationships.stream()
				.forEach(relationship -> {
					System.out.println(relationship.toString());
				});
		}
	}
	
}
