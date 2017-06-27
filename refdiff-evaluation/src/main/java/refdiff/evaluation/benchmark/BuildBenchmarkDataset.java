package refdiff.evaluation.benchmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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

import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import refdiff.core.api.GitService;
import refdiff.evaluation.BenchmarkDataset;
import refdiff.evaluation.utils.RefactoringDescriptionParser;
import refdiff.evaluation.utils.RefactoringRelationship;
import refdiff.evaluation.utils.RefactoringSet;

public class BuildBenchmarkDataset {

	private static int min = 2;
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
	
	public static void main(String[] args) throws Exception {
		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		om.configure(SerializationFeature.INDENT_OUTPUT, true);
		String baseDir = "d:/tmp/";

		List<JsonCommit> commits = new ArrayList<>(Arrays.asList(om.readValue(new File("data/fse/refactorings.json"), JsonCommit[].class)));
		List<RefactoringSet> selectedCommits = new ArrayList<>();
		BenchmarkDataset bds = new BenchmarkDataset();
		for (RefactoringSet commit : bds.all()) {
		    remove(commits, commit.getProject(), commit.getRevision());
		    selectedCommits.add(commit);
		}
		
		Set<String> missing = refactoringsMissing(selectedCommits);
		while (!missing.isEmpty()) {
			JsonCommit c = selectCommit(commits, missing);
			selectedCommits.add(toRefactoringSet(c));
			missing = refactoringsMissing(selectedCommits);
		}

//		GitService gs = new GitServiceImpl();
//		for (JsonCommit commit : commits) {
//			if (testCheckout(baseDir, gs, commit)) {
//				commits2.add(commit);
//			}
//		}
		//om.writeValue(new File("data/fse/refactorings2.json"), selectedCommits);
		
		System.out.println(selectedCommits.size());
		for (String refactoringType : refactoringTypes) {
            long count = countRefactoringType(selectedCommits, refactoringType);
            System.out.println(refactoringType + " " + count);
        }
	}

	private static void read() {
	    try (BufferedReader br = new BufferedReader(new FileReader("data/fse/refactorings.txt"))) {
	        String line = br.readLine();
	        while (line != null && !line.isEmpty()) {
	            String[] parts = line.split("\t");
	            String repo = parts[0];
	            String sha1 = parts[1];
	            String description = parts[2];
	            // TODO
	            line = br.readLine();
	        }
	        
	    } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	private static RefactoringSet toRefactoringSet(JsonCommit c) {
        RefactoringSet rs = new RefactoringSet(c.repository, c.sha1);
        for (JsonRefactoring jr : c.refactorings) {
            List<RefactoringRelationship> list = parser.parse(jr.description);
            for (RefactoringRelationship rr : list) {
                rs.add(rr);
            }
        }
        return rs;
    }

    private static void remove(List<JsonCommit> commits, String repo, String sha1) {
		for (int i = 0; i < commits.size(); i++) {
			JsonCommit c = commits.get(i);
			if (c.repository.equals(repo) && c.sha1.equals(sha1)) {
			    commits.remove(i);
			    return;
			}
		}
	}

	private static JsonCommit selectCommit(List<JsonCommit> commits, Set<String> missing) {
		while (!commits.isEmpty()) {
			int i = randomGen.nextInt(commits.size());
			JsonCommit c = commits.remove(i);
			long neededRefactoringsCount = c.refactorings.stream().filter(r -> missing.contains(r.type)).collect(Collectors.counting());
			if (neededRefactoringsCount == 0) {
				continue;
			}
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