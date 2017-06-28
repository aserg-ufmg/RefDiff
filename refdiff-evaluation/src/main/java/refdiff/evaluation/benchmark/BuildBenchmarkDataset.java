package refdiff.evaluation.benchmark;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import refdiff.core.api.GitService;
import refdiff.core.util.GitServiceImpl;
import refdiff.evaluation.BenchmarkDataset;
import refdiff.evaluation.utils.RefactoringDescriptionParser;
import refdiff.evaluation.utils.RefactoringRelationship;
import refdiff.evaluation.utils.RefactoringSet;

public class BuildBenchmarkDataset {

	private static int min = 10;
	private static Random randomGen = new Random(1L);
	private static Set<String> refactoringTypes = new LinkedHashSet<>(Arrays.asList(
			"Move Class",
//			"Rename Class",
			"Extract Interface",
			"Extract Superclass",
			
			"Extract Method",
//			"Rename Method"
			"Move Method",
			"Inline Method",
			"Pull Up Method",
			"Push Down Method",
			
			"Pull Up Attribute",
			"Push Down Attribute",
			"Move Attribute"));

	private static RefactoringDescriptionParser parser = new RefactoringDescriptionParser();
	private static GitService gs = new GitServiceImpl();
	private static String baseDir = "d:/tmp/repos";

	public static void main(String[] args) throws Exception {
//		ObjectMapper om = new ObjectMapper();
//		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//		om.configure(SerializationFeature.INDENT_OUTPUT, true);

		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);

		//testCheckout("https://github.com/linkedin/rest.li.git", "54fa890a6af4ccf564fb481d3e1b6ad4d084de9e");
		List<RefactoringSet> commits = readFseData();
		List<RefactoringSet> selectedCommits = new ArrayList<>();
		BenchmarkDataset bds = new BenchmarkDataset();
		for (RefactoringSet commit : bds.all()) {
		    remove(commits, commit.getProject(), commit.getRevision());
		    selectedCommits.add(commit);
		}
		
		Set<String> missing = refactoringsMissing(selectedCommits);
		while (!missing.isEmpty()) {
			RefactoringSet c = selectCommit(commits, missing);
			selectedCommits.add(c);
			missing = refactoringsMissing(selectedCommits);
		}

//		GitService gs = new GitServiceImpl();
//		for (JsonCommit commit : commits) {
//			if (testCheckout(baseDir, gs, commit)) {
//				commits2.add(commit);
//			}
//		}
		//om.writeValue(new File("data/fse/refactorings2.json"), selectedCommits);
		
		System.out.println("Total commits: " + selectedCommits.size());
		for (String refactoringType : refactoringTypes) {
			long count = countRefactoringType(selectedCommits, refactoringType);
			System.out.println(refactoringType + " " + count);
		}
		System.out.println();
		for (RefactoringSet commit : selectedCommits) {
			System.out.println(String.format("%s\t%s", commit.getProject(), commit.getRevision()));
			for (RefactoringRelationship rr : commit.getRefactorings()) {
				System.out.println("  " + rr.toString());
			}
		}
	}

	private static List<RefactoringSet> readFseData() {
		List<RefactoringSet> list = new ArrayList<>();
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
					list.add(rs);
				}
				rs.add(parser.parse(description));
				line = br.readLine();
			}
			return list;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

    private static void remove(List<RefactoringSet> commits, String repo, String sha1) {
		for (int i = 0; i < commits.size(); i++) {
			RefactoringSet c = commits.get(i);
			if (c.getProject().equals(repo) && c.getRevision().equals(sha1)) {
			    commits.remove(i);
			    return;
			}
		}
	}

	private static RefactoringSet selectCommit(List<RefactoringSet> commits, Set<String> missing) {
		while (!commits.isEmpty()) {
			int i = randomGen.nextInt(commits.size());
			RefactoringSet c = commits.remove(i);
			long neededRefactoringsCount = c.getRefactorings().stream().filter(r -> missing.contains(r.getRefactoringType().getDisplayName())).collect(Collectors.counting());
			if (neededRefactoringsCount == 0) {
				continue;
			}
//			if (!testCheckout(c.getProject(), c.getRevision())) {
//				continue;
//			}
			return c;
		}
		throw new RuntimeException("No commits available: " + missing);
	}

	private static Set<String> refactoringsMissing(List<RefactoringSet> selectedCommits) {
		HashSet<String> missing = new HashSet<>(refactoringTypes);
		for (String refactoringType : refactoringTypes) {
			long count = countRefactoringType(selectedCommits, refactoringType);
			if (count >= min) {
				missing.remove(refactoringType);
			}
		}
		return missing;
	}

	private static long countRefactoringType(List<RefactoringSet> selectedCommits, String refactoringType) {
        return selectedCommits.stream().flatMap(c -> c.getRefactorings().stream().filter(r -> r.getRefactoringType().getDisplayName().equals(refactoringType))).collect(Collectors.counting());
    }

	private static boolean testCheckout(String repository, String commitId) {
		String folderName = baseDir + repository.substring(repository.lastIndexOf('/'), repository.lastIndexOf('.'));
		try (Repository repo = gs.cloneIfNotExists(folderName, repository)) {
			return gs.resolveCommit(repo, commitId) != null;
		} catch (Exception e) {
		    System.out.println(String.format("%s %s %s", e.getMessage(), repository, commitId));
			throw new RuntimeException(e);
		}
	}

}

class JsonCommit {
	public String repository;
	public String sha1;
	public List<JsonRefactoring> refactorings;
}

class JsonRefactoring {
	public String type;
	public String description;
}