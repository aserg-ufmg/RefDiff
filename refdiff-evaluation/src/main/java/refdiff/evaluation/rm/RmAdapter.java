package refdiff.evaluation.rm;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import refdiff.core.api.GitRefactoringDetector;
import refdiff.core.api.RefactoringHandler;

public class RmAdapter implements GitRefactoringDetector {

    private final org.refactoringminer.api.GitHistoryRefactoringMiner rm;
    
    public RmAdapter(org.refactoringminer.api.GitHistoryRefactoringMiner rm) {
        this.rm = rm;
    }

    @Override
    public void detectAtCommit(Repository repository, String commitId, RefactoringHandler handler) {
        this.rm.detectAtCommit(repository, commitId, new RefactoringHandlerAdapter(handler));
    }

    @Override
    public String getConfigId() {
        return "rm";
    }

    private static class RefactoringHandlerAdapter extends org.refactoringminer.api.RefactoringHandler {
        private final RefactoringHandler handler;

        public RefactoringHandlerAdapter(RefactoringHandler handler) {
            this.handler = handler;
        }

        public void handle(RevCommit commitData, List<org.refactoringminer.api.Refactoring> refactorings) {
            handler.handle(commitData, refactorings.stream().map(RefactoringAdapter::from).collect(Collectors.toList()));
        }

        public void handleException(String commitId, Exception e) {
            handler.handleException(commitId, e);
        }

    }
    
}
