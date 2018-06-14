package refdiff.core.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

public class GitSourceTree extends SourceFileSet {
	
	private final Repository repo;
	private final ObjectId sha1;
	
	public GitSourceTree(Repository repo, ObjectId commitSha1, List<SourceFile> sourceFiles) {
		super(sourceFiles);
		this.repo = repo;
		this.sha1 = commitSha1;
	}
	
	@Override
	public String readContent(SourceFile sourceFile) throws IOException {
		try (ObjectReader reader = repo.newObjectReader(); RevWalk walk = new RevWalk(reader)) {
			RevCommit commit = walk.parseCommit(sha1);
			
			RevTree tree = commit.getTree();
			TreeWalk treewalk = TreeWalk.forPath(reader, sourceFile.getPath(), tree);
			
			if (treewalk != null) {
				byte[] data = reader.open(treewalk.getObjectId(0)).getBytes();
				return new String(data, StandardCharsets.UTF_8.name());
			} else {
				throw new FileNotFoundException(sourceFile.getPath());
			}
		}
	}
	
	@Override
	public String describeLocation(SourceFile sourceFile) {
		return String.format("%s:%s:%s", repo.getDirectory().getName(), sha1.abbreviate(7).name(), sourceFile.getPath());
	}
}
