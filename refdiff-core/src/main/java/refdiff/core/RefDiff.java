package refdiff.core;

import java.io.File;
import java.util.function.Consumer;

import org.eclipse.jgit.lib.Repository;

import refdiff.core.diff.RastComparator;
import refdiff.core.diff.RastDiff;
import refdiff.core.io.FilePathFilter;
import refdiff.core.io.GitHelper;
import refdiff.core.io.SourceFileSet;
import refdiff.core.util.PairBeforeAfter;
import refdiff.parsers.RastParser;

public class RefDiff {
	
	private final RastComparator comparator;
	private final FilePathFilter fileFilter;
	
	public RefDiff(RastParser parser) {
		this.comparator = new RastComparator(parser);
		this.fileFilter = parser.getAllowedFilesFilter();
	}
	
	public File cloneGitRepository(File destinationFolder, String cloneUrl) throws Exception {
		return GitHelper.cloneBareRepository(destinationFolder, cloneUrl);
	}
	
	public RastDiff computeDiffForCommit(File gitRepository, String commitSha1) {
		try (Repository repo = GitHelper.openRepository(gitRepository)) {
			PairBeforeAfter<SourceFileSet> beforeAndAfter = GitHelper.getSourcesBeforeAndAfterCommit(repo, commitSha1, fileFilter);
			return comparator.compare(beforeAndAfter);
		}
	}
	
	public void computeDiffForCommitHistory(File gitRepository, int maxDepth, Consumer<RastDiff> diffConsumer) {
		try (Repository repo = GitHelper.openRepository(gitRepository)) {
			GitHelper.forEachNonMergeCommit(repo, maxDepth, (revBefore, revAfter) -> {
				PairBeforeAfter<SourceFileSet> beforeAndAfter = GitHelper.getSourcesBeforeAndAfterCommit(repo, revBefore, revAfter, fileFilter);
				RastDiff diff = comparator.compare(beforeAndAfter);
				diffConsumer.accept(diff);
			});
		}
	}
	
}
