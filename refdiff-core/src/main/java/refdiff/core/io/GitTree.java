package refdiff.core.io;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

public class GitTree {
	
	private static String fetchBlob(Repository repo, String revSpec, String path) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		
		// Resolve the revision specification
		final ObjectId id = repo.resolve(revSpec);
		
		try (ObjectReader reader = repo.newObjectReader()) {
			// Get the commit object for that revision
			RevWalk walk = new RevWalk(reader);
			RevCommit commit = walk.parseCommit(id);
			
			// Get the revision's file tree
			RevTree tree = commit.getTree();
			// .. and narrow it down to the single file's path
			TreeWalk treewalk = TreeWalk.forPath(reader, path, tree);
			
			if (treewalk != null) {
				// use the blob id to read the file's data
				byte[] data = reader.open(treewalk.getObjectId(0)).getBytes();
				return new String(data, "utf-8");
			} else {
				return "";
			}
		}
	}
	
}
