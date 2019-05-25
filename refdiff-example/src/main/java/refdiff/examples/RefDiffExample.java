package refdiff.examples;

import java.io.File;

import refdiff.core.RefDiff;
import refdiff.core.diff.RastDiff;
import refdiff.core.diff.Relationship;
import refdiff.parsers.js.JsParser;

public class RefDiffExample {
	
	public static void main(String[] args) throws Exception {
		File tempFolder = new File("temp");
		
		try (JsParser jsParser = new JsParser()) {
			RefDiff refDiff = new RefDiff(jsParser);
			File gitRepository = refDiff.cloneGitRepository(new File(tempFolder, "angular.js"), "https://github.com/refdiff-study/angular.js.git");
			RastDiff diff = refDiff.computeDiffForCommit(gitRepository, "2636105c5e363f14cda890f19ac9c3bc57556dd2");
			printDiff(diff);
		}
	}
	
	private static void printDiff(RastDiff diff) {
		for (Relationship rel : diff.getRelationships()) {
			System.out.println(rel);
		}
	}
	
}
