package refdiff.evaluation.c;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

import refdiff.evaluation.ExternalProcess;

public class ListCommitsFromRepoExample {
	
	public static void main(String[] args) throws IOException {
		
		String tempFolder = "D:/tmp";
		String projectName = "netdata";
		String cloneUrl = "https://github.com/refdiff-study/" + projectName + ".git";
		File repoFolder = new File(tempFolder, projectName + ".git");
		
		if (!repoFolder.exists()) {
			ExternalProcess.execute(new File(tempFolder), "git", "clone", cloneUrl, "--bare", "--shallow-since=2017-01-01");
		}
		
		try (
			Repository repository = new RepositoryBuilder()
				.setGitDir(repoFolder)
				.readEnvironment()
				.build();
			RevWalk revWalk = new RevWalk(repository)) {
			
			RevCommit head = revWalk.parseCommit(repository.resolve("HEAD"));
			revWalk.markStart(head);
			revWalk.setRevFilter(RevFilter.NO_MERGES);
			
			for (RevCommit commit : revWalk) {
				LocalDateTime dateTime = LocalDateTime.ofEpochSecond(commit.getCommitTime(), 0, ZoneOffset.UTC);
				String sha1 = commit.getId().name();
				System.out.println(String.format("%s\t%s\t%s", sha1, dateTime, commit.getShortMessage()));
			}
		}
	}
	
}
