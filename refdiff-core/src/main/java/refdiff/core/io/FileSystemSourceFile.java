package refdiff.core.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemSourceFile implements SourceFile {
	
	private final Path basePath;
	private final Path path;
	private final Charset charset;
	private String content;
	
	public FileSystemSourceFile(Path basePath, Path path) {
		this(basePath, path, Charset.defaultCharset());
	}
	
	public FileSystemSourceFile(Path basePath, Path path, Charset charset) {
		this.basePath = basePath;
		this.path = path;
		this.charset = charset;
	}
	
	public String getPath() {
		return path.toString().replace('\\', '/');
	}
	
	@Override
	public String getContent() throws IOException {
		if (content == null) {
			byte[] encoded = Files.readAllBytes(basePath.resolve(path));
			content = new String(encoded, charset);
		}
		return content;
	}
	
	@Override
	public String toString() {
		return basePath.resolve(path).toString();
	}
	
	public Path getBasePath() {
		return basePath;
	}
}
