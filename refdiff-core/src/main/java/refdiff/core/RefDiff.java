package refdiff.core;

import java.io.File;
import java.util.function.BiConsumer;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import refdiff.core.diff.CstComparator;
import refdiff.core.diff.CstDiff;
import refdiff.core.io.FilePathFilter;
import refdiff.core.io.GitHelper;
import refdiff.core.io.SourceFileSet;
import refdiff.core.util.PairBeforeAfter;
import refdiff.parsers.LanguagePlugin;

/**
 * High level API of RefDiff, providing methods to compute CST diffs between revisions (commits) of a git repository.
 */
public class RefDiff {
	
	private final CstComparator comparator;
	private final FilePathFilter fileFilter;
	
	/**
	 * Build a RefDiff instance with the specified language plugin. E.g.: {@code new RefDiff(new JsParser())}.
	 * 
	 * @param parser A language parser
	 */
	public RefDiff(LanguagePlugin parser) {
		this.comparator = new CstComparator(parser);
		this.fileFilter = parser.getAllowedFilesFilter();
	}
	
	/**
	 * Clone a git repository in a local folder.
	 * Note that the repository will be clone in bare mode (see {@link https://git-scm.com/docs/git-clone}).
	 * 
	 * @param destinationFolder Folder in which the repository will be cloned.
	 * @param cloneUrl The URL of the repository.
	 * @return The destination folder.
	 */
	public File cloneGitRepository(File destinationFolder, String cloneUrl) {
		return GitHelper.cloneBareRepository(destinationFolder, cloneUrl);
	}
	
	/**
	 * Compute a CST diff between a commit and its parent commit (previous revision).
	 * This method will throw an exception if the given commit has more than one parent (e.g., merge commits).
	 * 
	 * @param gitRepository The folder of the git repository (you should pass the .git folder if the repository is not on bare mode).
	 * @param commitSha1 SHA1 (or git object reference) that identifies the commit.
	 * @return The computed CST diff.
	 */
	public CstDiff computeDiffForCommit(File gitRepository, String commitSha1) {
		try (Repository repo = GitHelper.openRepository(gitRepository)) {
			PairBeforeAfter<SourceFileSet> beforeAndAfter = GitHelper.getSourcesBeforeAndAfterCommit(repo, commitSha1, fileFilter);
			return comparator.compare(beforeAndAfter);
		}
	}
	
	/**
	 * Compute the CST diff for each commit in the git repository, starting from HEAD.
	 * 
	 * @param gitRepository The folder of the git repository (you should pass the .git folder if the repository is not on bare mode).
	 * @param maxDepth Number of commits that will be navigated backwards at maximum.
	 * @param diffConsumer Consumer function that will be called for each computed CST diff.
	 */
	public void computeDiffForCommitHistory(File gitRepository, int maxDepth, BiConsumer<RevCommit, CstDiff> diffConsumer) {
		computeDiffForCommitHistory(gitRepository, "HEAD", maxDepth, diffConsumer);
	}
	
	/**
	 * Compute the CST diff for each commit in the git repository, starting from the specified commit. Merge comits are skipped.
	 * 
	 * @param gitRepository The folder of the git repository (you should pass the .git folder if the repository is not on bare mode).
	 * @param startAt git object reference of the starting commit.
	 * @param maxDepth Number of commits that will be navigated backwards at maximum.
	 * @param diffConsumer Consumer function that will be called for each computed CST diff.
	 */
	public void computeDiffForCommitHistory(File gitRepository, String startAt, int maxDepth, BiConsumer<RevCommit, CstDiff> diffConsumer) {
		try (Repository repo = GitHelper.openRepository(gitRepository)) {
			GitHelper.forEachNonMergeCommit(repo, startAt, maxDepth, (revBefore, revAfter) -> {
				PairBeforeAfter<SourceFileSet> beforeAndAfter = GitHelper.getSourcesBeforeAndAfterCommit(repo, revBefore, revAfter, fileFilter);
				CstDiff diff = comparator.compare(beforeAndAfter);
				diffConsumer.accept(revAfter, diff);
			});
		}
	}
	
	/**
	 * Low level method that computes the CST diff between two arbitrary revisions.
	 * This method operates directly with jgit objects such as {@code Repository} and {@code RevCommit}.
	 * 
	 * For more details on jgit library, please refer to {@link https://wiki.eclipse.org/JGit/User_Guide#Concepts}.
	 * 
	 * @param repo The jgit repository object.
	 * @param revBefore The jgit commit object before the change.
	 * @param revAfter The jgit commit object after the change.
	 * @return The computed CST diff between revisions.
	 */
	public CstDiff computeDiffBetweenRevisions(Repository repo, RevCommit revBefore, RevCommit revAfter) {
		PairBeforeAfter<SourceFileSet> beforeAndAfter = GitHelper.getSourcesBeforeAndAfterCommit(repo, revBefore, revAfter, fileFilter);
		return comparator.compare(beforeAndAfter);
	}
	
}
