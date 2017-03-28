package refdiff.evaluation.rm;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import refdiff.core.api.GitHistoryRefactoringMiner;
import refdiff.core.api.RefactoringHandler;

public class RmAdapter implements GitHistoryRefactoringMiner {

    private final org.refactoringminer.api.GitHistoryRefactoringMiner rm;
    
    public RmAdapter(org.refactoringminer.api.GitHistoryRefactoringMiner rm) {
        this.rm = rm;
    }

    @Override
    public void detectAll(Repository repository, String branch, RefactoringHandler handler) throws Exception {
        this.rm.detectAll(repository, branch, new RefactoringHandlerAdapter(handler));
    }

    @Override
    public void fetchAndDetectNew(Repository repository, RefactoringHandler handler) throws Exception {
        this.rm.fetchAndDetectNew(repository, new RefactoringHandlerAdapter(handler));
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

        public boolean skipCommit(String commitId) {
            return handler.skipCommit(commitId);
        }

        public void handle(RevCommit commitData, List<org.refactoringminer.api.Refactoring> refactorings) {
            handler.handle(commitData, refactorings.stream().map(RefactoringAdapter::from).collect(Collectors.toList()));
        }

        public void handleException(String commitId, Exception e) {
            handler.handleException(commitId, e);
        }

        public void onFinish(int refactoringsCount, int commitsCount, int errorCommitsCount) {
            handler.onFinish(refactoringsCount, commitsCount, errorCommitsCount);
        }
    }
    
}
