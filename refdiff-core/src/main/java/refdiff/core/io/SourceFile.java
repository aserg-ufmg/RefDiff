package refdiff.core.io;

import java.nio.file.Path;

public final class SourceFile {
	
	private final Path path;
	
	public SourceFile(Path path) {
		this.path = path;
	}
	
	public String getPath() {
		return path.toString().replace('\\', '/');
	}
	
	@Override
	public String toString() {
		return getPath();
	}
	
}
