package refdiff.evaluation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FseDataset extends AbstractDataset {
	
	public FseDataset() {
		RefactoringDescriptionParser parser = new RefactoringDescriptionParser();
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
					add(rs);
				}
				rs.add(parser.parse(description));
				line = br.readLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
