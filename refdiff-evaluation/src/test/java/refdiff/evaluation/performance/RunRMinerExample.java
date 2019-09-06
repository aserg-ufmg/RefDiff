package refdiff.evaluation.performance;

import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

public class RunRMinerExample {
	
	public static void main(String[] args) throws Exception {
		
		GitService gitService = new GitServiceImpl();
		GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
		
		Repository repo = gitService.cloneIfNotExists(
			"tmp/refactoring-toy-example",
			"https://github.com/danilofes/refactoring-toy-example.git");
		
		miner.detectAtCommit(repo, "05c1e773878bbacae64112f70964f4f2f7944398", new RefactoringHandler() {
			@Override
			public void handle(String commitId, List<Refactoring> refactorings) {
				System.out.println("Refactorings at " + commitId);
				for (Refactoring ref : refactorings) {
					System.out.println(ref.toString());
				}
			}
		});
		
	}
	
}
