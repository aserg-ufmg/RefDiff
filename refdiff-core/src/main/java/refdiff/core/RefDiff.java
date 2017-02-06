package refdiff.core;

import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import refdiff.core.api.GitHistoryRefactoringMiner;
import refdiff.core.api.GitService;
import refdiff.core.api.Refactoring;
import refdiff.core.api.RefactoringHandler;
import refdiff.core.rm2.analysis.GitHistoryRefactoringMiner2;
import refdiff.core.util.GitServiceImpl;

public class RefDiff {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: RefDiff <git-repo-folder> <commit-SHA1>");
        }
        final String folder = args[0];
        final String commitId = args[1];
        
        GitService gitService = new GitServiceImpl(); 
        try (Repository repo = gitService.openRepository(folder)) {
            GitHistoryRefactoringMiner detector = new GitHistoryRefactoringMiner2();
            detector.detectAtCommit(repo, commitId, new RefactoringHandler() {
                @Override
                public void handle(RevCommit commitData, List<Refactoring> refactorings) {
                    if (refactorings.isEmpty()) {
                        System.out.println("No refactorings found in commit " + commitId);
                    } else {
                        System.out.println(refactorings.size() + " refactorings found in commit " + commitId + ": ");
                        for (Refactoring ref : refactorings) {
                            System.out.println("  " + ref);
                        }
                    }
                }
                @Override
                public void handleException(String commit, Exception e) {
                    System.err.println("Error processing commit " + commitId);
                    e.printStackTrace(System.err);
                }
            });
        }
    }

}
