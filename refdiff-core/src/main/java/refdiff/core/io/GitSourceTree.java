package refdiff.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
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
	private Path checkoutFolder;
	
	public GitSourceTree(Repository repo, ObjectId commitSha1, List<SourceFile> sourceFiles) {
		super(sourceFiles);
		this.repo = repo;
		this.sha1 = commitSha1;
	}
	
	@Override
	public String readContent(SourceFile sourceFile) throws IOException {
		return new String(readBytes(sourceFile), StandardCharsets.UTF_8.name());
	}
	
	private byte[] readBytes(SourceFile sourceFile) throws MissingObjectException, IncorrectObjectTypeException, IOException, CorruptObjectException, UnsupportedEncodingException, FileNotFoundException {
		try (ObjectReader reader = repo.newObjectReader(); RevWalk walk = new RevWalk(reader)) {
			RevCommit commit = walk.parseCommit(sha1);
			
			RevTree tree = commit.getTree();
			TreeWalk treewalk = TreeWalk.forPath(reader, sourceFile.getPath(), tree);
			
			if (treewalk != null) {
				return reader.open(treewalk.getObjectId(0)).getBytes();
			} else {
				throw new FileNotFoundException(sourceFile.getPath());
			}
		}
	}
	
	@Override
	public String describeLocation(SourceFile sourceFile) {
		return String.format("%s:%s:%s", repo.getDirectory().getName(), sha1.abbreviate(7).name(), sourceFile.getPath());
	}
	
	@Override
	public void materializeAt(Path folderPath) throws IOException {
		File folder = folderPath.toFile();
		if (folder.exists() || folder.mkdirs()) {
			for (SourceFile sf : getSourceFiles()) {
				File destinationFile = new File(folder, sf.getPath());
				if (!destinationFile.exists()) {
					byte[] content = readBytes(sf);
					Files.createDirectories(destinationFile.getParentFile().toPath());
					Files.write(destinationFile.toPath(), content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);					
				}
			}
			checkoutFolder = folderPath;
		} else {
			throw new IOException("Failed to create directory " + folderPath);
		}
	}
	
	@Override
	public void materializeAtBase(Path baseFolderPath) throws IOException {
		Path folder = baseFolderPath.resolve(repo.getDirectory().getName() + "-" + sha1.abbreviate(7).name());
		materializeAt(folder);
	}
	
	@Override
	public Optional<Path> getBasePath() {
		return Optional.ofNullable(checkoutFolder);
	}
}
