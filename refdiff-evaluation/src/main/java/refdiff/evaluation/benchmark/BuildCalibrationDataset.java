package refdiff.evaluation.benchmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Repository;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import refdiff.core.api.GitService;
import refdiff.core.util.GitServiceImpl;
import refdiff.evaluation.BenchmarkDataset;
import refdiff.evaluation.benchmark.AbstractDataset.CommitEntry;
import refdiff.evaluation.utils.RefactoringRelationship;
import refdiff.evaluation.utils.RefactoringSet;

public class BuildCalibrationDataset {

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

	private static GitService gs = new GitServiceImpl();
	private static String baseDir = "c:/tmp";

	public static void main(String[] args) throws Exception {
//		ObjectMapper om = new ObjectMapper();
//		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//		om.configure(SerializationFeature.INDENT_OUTPUT, true);

		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);

		FseDataset fseDataset = new FseDataset();
		List<RefactoringSet> selectedCommits = new ArrayList<>();
		BenchmarkDataset bds = new BenchmarkDataset();
		for (RefactoringSet commit : bds.getExpected()) {
			if (testCheckout(commit.getProject(), commit.getRevision())) {
				selectedCommits.add(fseDataset.remove(commit.getProject(), commit.getRevision()));
				System.out.println(String.format("Selected %s %s", commit.getProject(), commit.getRevision()));
			} else {
				System.out.println(String.format("MissingObject %s %s", commit.getProject(), commit.getRevision()));
			}
		}
		
		Set<String> missing = refactoringsMissing(selectedCommits);
		while (!missing.isEmpty()) {
			RefactoringSet c = selectCommit(fseDataset, missing);
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
		
		System.out.println();
		System.out.println("Total commits: " + selectedCommits.size());
		for (String refactoringType : refactoringTypes) {
			long count = countRefactoringType(selectedCommits, refactoringType);
			System.out.println(refactoringType + " " + count);
		}
		System.out.println();
		for (RefactoringSet commit : selectedCommits) {
			System.out.println(String.format("%s\t%s", commit.getProject(), commit.getRevision()));
			for (RefactoringRelationship rr : commit.getRefactorings()) {
				System.out.println("\t" + rr.toString());
			}
		}
	}

	private static RefactoringSet selectCommit(FseDataset dataset, Set<String> missing) {
		List<CommitEntry> commits = dataset.commits;
		while (!commits.isEmpty()) {
			int i = randomGen.nextInt(commits.size());
			CommitEntry ce = commits.remove(i);
			RefactoringSet c = ce.getExpected();
			long neededRefactoringsCount = c.getRefactorings().stream().filter(r -> missing.contains(r.getRefactoringType().getDisplayName())).collect(Collectors.counting());
			if (neededRefactoringsCount == 0) {
				continue;
			}
			if (!testCheckout(c.getProject(), c.getRevision())) {
				System.out.println(String.format("MissingObject %s %s", c.getProject(), c.getRevision()));
				continue;
			}
			System.out.println(String.format("Selected %s %s", c.getProject(), c.getRevision()));
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