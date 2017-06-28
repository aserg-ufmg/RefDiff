package refdiff.evaluation.benchmark;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import refdiff.evaluation.utils.RefactoringDescriptionParser;
import refdiff.evaluation.utils.RefactoringSet;

public class FseDataset {

	private final List<RefactoringSet> commits;

	public FseDataset() {
		RefactoringDescriptionParser parser = new RefactoringDescriptionParser();
		commits = new ArrayList<>();
		RefactoringSet rs = null;
		try (BufferedReader br = new BufferedReader(new FileReader("data/fse/refactorings.txt"))) {
			String line = br.readLine();
			while (line != null && !line.isEmpty()) {
				String[] parts = line.split("\t");
				String repo = parts[0];
				String sha1 = parts[1];
				String description = parts[2];
				if (rs == null || !rs.getProject().equals(repo) || !rs.getRevision().equals(sha1)) {
					rs = new RefactoringSet(repo, sha1);
					commits.add(rs);
				}
				rs.add(parser.parse(description));
				line = br.readLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<RefactoringSet> getCommits() {
		return commits;
	}

	public RefactoringSet remove(String repo, String sha1) {
		for (int i = 0; i < commits.size(); i++) {
			RefactoringSet c = commits.get(i);
			if (c.getProject().equals(repo) && c.getRevision().equals(sha1)) {
				return commits.remove(i);
			}
		}
		throw new RuntimeException(String.format("Not found: %s %s", repo, sha1));
	}
}
