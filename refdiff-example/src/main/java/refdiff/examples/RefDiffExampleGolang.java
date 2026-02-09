// package refdiff.examples;

// import java.io.File;

// import refdiff.core.RefDiff;
// import refdiff.core.diff.CstDiff;
// import refdiff.core.diff.Relationship;
// import refdiff.parsers.go.GoPlugin;

// public class RefDiffExampleGolang {
//   public static void main(String[] args) throws Exception {
//     runExample();
//   }

//   private static void runExample() throws Exception {
//     // This is a temp folder to clone or checkout git repositories.
//     File tempFolder = new File("temp");

//     // Creates a RefDiff instance configured with the Go plugin.
//     try (GoPlugin goPlugin = new GoPlugin(tempFolder)) {
//       RefDiff refDiffGo = new RefDiff(goPlugin);

//       File repo = refDiffGo.cloneGitRepository(
//           new File(tempFolder, "go-refactoring-example.git"),
//           "https://github.com/rodrigo-brito/go-refactoring-example.git");

//       CstDiff diffForCommit = refDiffGo.computeDiffForCommit(repo, "2a2bc542e3b9ea549936556c08ebeaaf7e98adbc");
//       printRefactorings("Refactorings found in go-refactoring-example 2a2bc542e3b9ea549936556c08ebeaaf7e98adbc",
//           diffForCommit);
//     }
//   }

//   private static void printRefactorings(String headLine, CstDiff diff) {
//     System.out.println(headLine);
//     for (Relationship rel : diff.getRefactoringRelationships()) {
//       System.out.println(rel.getStandardDescription());
//     }
//   }
// }