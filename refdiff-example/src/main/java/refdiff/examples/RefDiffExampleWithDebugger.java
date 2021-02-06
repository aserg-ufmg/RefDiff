package refdiff.examples;

import java.io.File;

import refdiff.core.RefDiff;
import refdiff.core.diff.CstComparatorDebugger;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.Relationship;
import refdiff.parsers.java.JavaPlugin;

public class RefDiffExampleWithDebugger {
	
	public static void main(String[] args) throws Exception {
		runExample();
	}
	
	private static void runExample() throws Exception {
		File tempFolder = new File("temp");
		
		JavaPlugin javaPlugin = new JavaPlugin(tempFolder);
		RefDiff refDiffJava = new RefDiff(javaPlugin);
		
		String commitId = "0022080";
		File gitRepo = refDiffJava.cloneGitRepository(new File(tempFolder, "spring-boot"), "https://github.com/osmarleandro/spring-boot.git");
		
		CstDiff diff = refDiffJava.computeDiffForCommit(gitRepo, commitId, new CstComparatorDebugger());
		printRefactorings("Refactorings", diff);
	}
	
	private static void printRefactorings(String headLine, CstDiff diff) {
		System.out.println(headLine);
		for (Relationship rel : diff.getRefactoringRelationships()) {
			System.out.println(rel.getDescriptionWithScore());
		}
	}
	
}
