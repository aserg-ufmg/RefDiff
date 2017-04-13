package refdiff.core.api;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;
import refdiff.core.api.Refactoring;

/**
 * Handler object that works in conjunction with {@link refdiff.core.api.GitRefactoringDetector}.
 * 
 */
public abstract class RefactoringHandler {

	/**
	 * This method is called after each commit is analyzed.
	 * You should override this method to do your custom logic with the list of detected refactorings.
	 * 
	 * @param commitData An object (from JGit library) that contains metadata information about the commit such as date, author, etc.
	 * @param refactorings List of refactorings detected in the commit.
	 */
	public void handle(RevCommit commitData, List<Refactoring> refactorings) {}

	/**
     * This method is called whenever an exception is thrown during the analysis of the given commit.
     * You should override this method to do your custom logic in the case of exceptions (e.g. skip or rethrow).
     * 
     * @param commitId The SHA key that identifies the commit.
     * @param e The exception thrown.
     */
    public void handleException(String commitId, Exception e) {
        throw new RuntimeException(e);
    }

}
