package refdiff.evaluation.benchmark;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import refdiff.core.api.GitService;

public class BuildBenchmarkDataset {

	private static int min = 5;
	private static Random randomGen = new Random(1L);
	private static Set<String> refactoringTypes = new HashSet<>(Arrays.asList(
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

	public static void main(String[] args) throws Exception {
		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		om.configure(SerializationFeature.INDENT_OUTPUT, true);
		String baseDir = "d:/tmp/";

		List<JsonCommit> commits = new ArrayList<>(Arrays.asList(om.readValue(new File("data/fse/refactorings.json"), JsonCommit[].class)));
		List<JsonCommit> selectedCommits = new ArrayList<>();
		selectedCommits.add(remove(commits, "https://github.com/linkedin/rest.li.git", "54fa890a6af4ccf564fb481d3e1b6ad4d084de9e"));
		
		Set<String> missing = refactoringsMissing(selectedCommits);
		while (!missing.isEmpty()) {
			JsonCommit c = selectCommit(selectedCommits, commits, missing);
			selectedCommits.add(c);
			missing = refactoringsMissing(selectedCommits);
		}

//		GitService gs = new GitServiceImpl();
//		for (JsonCommit commit : commits) {
//			if (testCheckout(baseDir, gs, commit)) {
//				commits2.add(commit);
//			}
//		}
		om.writeValue(new File("data/fse/refactorings2.json"), selectedCommits);
		
		System.out.println(selectedCommits.size());
	}

	private static JsonCommit remove(List<JsonCommit> commits, String repo, String sha1) {
		for (int i = 0; i < commits.size(); i++) {
			JsonCommit c = commits.get(i);
			if (c.repository.equals(repo)) {
				
			}
		}
		// TODO Auto-generated method stub
		return null;
	}

	private static JsonCommit selectCommit(List<JsonCommit> selectedCommits, List<JsonCommit> commits, Set<String> missing) {
		while (!commits.isEmpty()) {
			int i = randomGen.nextInt(commits.size());
			JsonCommit c = commits.remove(i);
			long neededRefactoringsCount = c.refactorings.stream().filter(r -> missing.contains(r.type)).collect(Collectors.counting());
			if (neededRefactoringsCount == 0) {
				continue;
			}
			return c;
		}
		throw new RuntimeException("No commits available");
	}

	private static Set<String> refactoringsMissing(List<JsonCommit> selectedCommits) {
		HashSet<String> missing = new HashSet<>(refactoringTypes);
		Map<String, List<JsonRefactoring>> mapByType = selectedCommits.stream().flatMap(c -> c.refactorings.stream()).collect(Collectors.groupingBy(r -> r.type));
		for (Map.Entry<String, List<JsonRefactoring>> entry : mapByType.entrySet()) {
			long count = entry.getValue().size();
			if (count >= min) {
				missing.remove(entry.getKey());
			}
		}
		return missing;
	}

	private static boolean testCheckout(String baseDir, GitService gs, JsonCommit commit) throws Exception {
		String folderName = baseDir + commit.repository.substring(commit.repository.lastIndexOf('/'), commit.repository.lastIndexOf('.'));
		try (Repository repo = gs.cloneIfNotExists(folderName, commit.repository)) {
			gs.checkout(repo, commit.sha1);
			return true;
		} catch (MissingObjectException e) {
			return false;
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