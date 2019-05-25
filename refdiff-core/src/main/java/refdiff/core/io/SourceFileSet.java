package refdiff.core.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public abstract class SourceFileSet {
	
	private List<SourceFile> sourceFiles;
	
	public SourceFileSet(List<SourceFile> sourceFiles) {
		this.sourceFiles = sourceFiles;
	}
	
	public List<SourceFile> getSourceFiles() {
		return sourceFiles;
	}
	
	public abstract String readContent(SourceFile sourceFile) throws IOException;
	
	public Optional<Path> getBasePath() {
		return Optional.empty();
	}
	
	public abstract String describeLocation(SourceFile sourceFile);
	
	public void materializeAt(Path folder) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	public void materializeAtBase(Path baseFolder) throws IOException {
		throw new UnsupportedOperationException();
	}
}
