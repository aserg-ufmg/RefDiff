package refdiff.core;

import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import refdiff.core.api.GitRefactoringDetector;
import refdiff.core.api.GitService;
import refdiff.core.api.Refactoring;
import refdiff.core.api.RefactoringHandler;
import refdiff.core.rm2.analysis.GitHistoryStructuralDiffAnalyzer;
import refdiff.core.rm2.analysis.RefDiffConfig;
import refdiff.core.rm2.analysis.RefDiffConfigImpl;
import refdiff.core.rm2.analysis.StructuralDiffHandler;
import refdiff.core.rm2.model.SDModel;
import refdiff.core.util.GitServiceImpl;

public class RefDiff implements GitRefactoringDetector {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: RefDiff <git-repo-folder> <commit-SHA1>");
        }
        final String folder = args[0];
        final String commitId = args[1];
        
        GitService gitService = new GitServiceImpl(); 
        try (Repository repo = gitService.openRepository(folder)) {
            GitRefactoringDetector detector = new RefDiff();
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

    private RefDiffConfig config;

    public RefDiff() {
        this(new RefDiffConfigImpl());
    }

    public RefDiff(RefDiffConfig config) {
        this.config = config;
    }

    private final class HandlerAdpater extends StructuralDiffHandler {
        private final RefactoringHandler handler;

        private HandlerAdpater(RefactoringHandler handler) {
            this.handler = handler;
        }

        @Override
        public void handle(RevCommit commitData, SDModel sdModel) {
            handler.handle(commitData, sdModel.getRefactorings());
        }

        @Override
        public void handleException(String commitId, Exception e) {
            handler.handleException(commitId, e);
        }
        
    }

    @Override
    public void detectAtCommit(Repository repository, String commitId, RefactoringHandler handler) {
        GitHistoryStructuralDiffAnalyzer sda = new GitHistoryStructuralDiffAnalyzer(config);
        sda.detectAtCommit(repository, commitId, new HandlerAdpater(handler));
    }

    @Override
    public String getConfigId() {
        return config.getId();
    }
}
