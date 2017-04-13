package refdiff.core.rm2.analysis;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import refdiff.core.api.GitRefactoringDetector;
import refdiff.core.api.RefactoringHandler;
import refdiff.core.rm2.model.SDModel;

class GitRefactoringDetectorImpl implements GitRefactoringDetector {

    private RefDiffConfig config;

    public GitRefactoringDetectorImpl() {
        this(new RefDiffConfigImpl());
    }

	public GitRefactoringDetectorImpl(RefDiffConfig config) {
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
