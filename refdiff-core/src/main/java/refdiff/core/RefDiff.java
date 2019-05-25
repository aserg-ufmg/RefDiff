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
import refdiff.parsers.CstParser;

public class RefDiff {
	
	private final CstComparator comparator;
	private final FilePathFilter fileFilter;
	
	public RefDiff(CstParser parser) {
		this.comparator = new CstComparator(parser);
		this.fileFilter = parser.getAllowedFilesFilter();
	}
	
	public File cloneGitRepository(File destinationFolder, String cloneUrl) throws Exception {
		return GitHelper.cloneBareRepository(destinationFolder, cloneUrl);
	}
	
	public CstDiff computeDiffForCommit(File gitRepository, String commitSha1) {
		try (Repository repo = GitHelper.openRepository(gitRepository)) {
			PairBeforeAfter<SourceFileSet> beforeAndAfter = GitHelper.getSourcesBeforeAndAfterCommit(repo, commitSha1, fileFilter);
			return comparator.compare(beforeAndAfter);
		}
	}
	
	public void computeDiffForCommitHistory(File gitRepository, int maxDepth, BiConsumer<RevCommit, CstDiff> diffConsumer) {
		try (Repository repo = GitHelper.openRepository(gitRepository)) {
			GitHelper.forEachNonMergeCommit(repo, maxDepth, (revBefore, revAfter) -> {
				PairBeforeAfter<SourceFileSet> beforeAndAfter = GitHelper.getSourcesBeforeAndAfterCommit(repo, revBefore, revAfter, fileFilter);
				CstDiff diff = comparator.compare(beforeAndAfter);
				diffConsumer.accept(revAfter, diff);
			});
		}
	}
	
}
