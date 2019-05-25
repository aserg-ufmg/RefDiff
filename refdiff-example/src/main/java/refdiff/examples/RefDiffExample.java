package refdiff.examples;

import java.io.File;

import refdiff.core.RefDiff;
import refdiff.core.diff.RastDiff;
import refdiff.core.diff.Relationship;
import refdiff.parsers.c.CParser;
import refdiff.parsers.java.JavaParser;
import refdiff.parsers.js.JsParser;

public class RefDiffExample {
	
	public static void main(String[] args) throws Exception {
		runExamples();
	}
	
	private static void runExamples() throws Exception {
		// This is a temp folder to clone or checkout git repositories
		File tempFolder = new File("temp");
		
		JsParser jsParser = new JsParser();
		RefDiff refDiffJs = new RefDiff(jsParser);
		
		File angularJsRepo = refDiffJs.cloneGitRepository(
			new File(tempFolder, "angular.js"),
			"https://github.com/refdiff-study/angular.js.git");
		
		RastDiff diffForCommit = refDiffJs.computeDiffForCommit(angularJsRepo, "2636105");
		printRefactorings("Refactorings found in angular.js 2636105", diffForCommit);
		
		refDiffJs.computeDiffForCommitHistory(angularJsRepo, 5, (commit, diff) -> {
			printRefactorings("Refactorings found in angular.js " + commit.getId().name(), diff);
		});
		
		jsParser.close();
		
		
		CParser cParser = new CParser();
		RefDiff refDiffC = new RefDiff(cParser);
		
		File gitRepo = refDiffC.cloneGitRepository(
			new File(tempFolder, "git"),
			"https://github.com/refdiff-study/git.git");
		
		printRefactorings(
			"Refactorings found in git ba97aea",
			refDiffC.computeDiffForCommit(gitRepo, "ba97aea1659e249a3a58ecc5f583ee2056a90ad8"));
		
		JavaParser javaParser = new JavaParser(tempFolder);
		RefDiff refDiffJava = new RefDiff(javaParser);
		
		File eclipseThemesRepo = refDiffC.cloneGitRepository(
			new File(tempFolder, "eclipse-themes"),
			"https://github.com/icse18-refactorings/eclipse-themes.git");
		
		printRefactorings(
			"Refactorings found in eclipse-themes 72f61ec",
			refDiffJava.computeDiffForCommit(eclipseThemesRepo, "72f61ec"));
	}
	
	private static void printRefactorings(String headLine, RastDiff diff) {
		System.out.println(headLine);
		for (Relationship rel : diff.getRefactoringRelationships()) {
			System.out.println(rel.getStandardDescription());
		}
	}
	
}
