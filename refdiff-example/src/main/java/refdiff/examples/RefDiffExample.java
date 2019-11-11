package refdiff.examples;

import java.io.File;

import refdiff.core.RefDiff;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.Relationship;
import refdiff.parsers.c.CPlugin;
import refdiff.parsers.java.JavaPlugin;
import refdiff.parsers.js.JsPlugin;

public class RefDiffExample {
	
	public static void main(String[] args) throws Exception {
		runExamples();
	}
	
	private static void runExamples() throws Exception {
		// This is a temp folder to clone or checkout git repositories.
		File tempFolder = new File("temp");
		
		// Creates a RefDiff instance configured with the JavaScript plugin.
		JsPlugin jsPlugin = new JsPlugin();
		RefDiff refDiffJs = new RefDiff(jsPlugin);
		
		// Clone the angular.js GitHub repo.
		File angularJsRepo = refDiffJs.cloneGitRepository(
			new File(tempFolder, "angular.js"),
			"https://github.com/refdiff-study/angular.js.git");
		
		// You can compute the relationships between the code elements in a commit with
		// its previous commit. The result of this operation is a CstDiff object, which
		// contains all relationships between CstNodes. Relationships whose type is different
		// from RelationshipType.SAME are refactorings.
		CstDiff diffForCommit = refDiffJs.computeDiffForCommit(angularJsRepo, "2636105");
		printRefactorings("Refactorings found in angular.js 2636105", diffForCommit);
		
		// You can also mine refactoring from the commit history. In this example we navigate
		// the commit graph backwards up to 5 commits. Merge commits are skipped.
		refDiffJs.computeDiffForCommitHistory(angularJsRepo, 5, (commit, diff) -> {
			printRefactorings("Refactorings found in angular.js " + commit.getId().name(), diff);
		});
		
		// The JsPlugin initializes JavaScript runtime to run the Babel parser. We should close it shut down.
		jsPlugin.close();
		
		
		// In this example, we use the plugin for C.
		CPlugin cPlugin = new CPlugin();
		RefDiff refDiffC = new RefDiff(cPlugin);
		
		File gitRepo = refDiffC.cloneGitRepository(
			new File(tempFolder, "git"),
			"https://github.com/refdiff-study/git.git");
		
		printRefactorings(
			"Refactorings found in git ba97aea",
			refDiffC.computeDiffForCommit(gitRepo, "ba97aea1659e249a3a58ecc5f583ee2056a90ad8"));
		
		
		// Now, we use the plugin for Java.
		JavaPlugin javaPlugin = new JavaPlugin(tempFolder);
		RefDiff refDiffJava = new RefDiff(javaPlugin);
		
		File eclipseThemesRepo = refDiffC.cloneGitRepository(
			new File(tempFolder, "eclipse-themes"),
			"https://github.com/icse18-refactorings/eclipse-themes.git");
		
		printRefactorings(
			"Refactorings found in eclipse-themes 72f61ec",
			refDiffJava.computeDiffForCommit(eclipseThemesRepo, "72f61ec"));
	}
	
	private static void printRefactorings(String headLine, CstDiff diff) {
		System.out.println(headLine);
		for (Relationship rel : diff.getRefactoringRelationships()) {
			System.out.println(rel.getStandardDescription());
		}
	}
	
}
