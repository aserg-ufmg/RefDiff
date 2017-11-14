package refdiff.core.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemSourceFile implements SourceFile {
	
	private final Path basePath;
	private final Path path;
	private final Charset charset;
	private String content;
	
	public FileSystemSourceFile(Path basePath, Path path) {
		this(basePath, path, StandardCharsets.UTF_8);
	}
	
	public FileSystemSourceFile(Path basePath, Path path, Charset charset) {
		this.basePath = basePath;
		this.path = path;
		this.charset = charset;
	}
	
	public String getPath() {
		return path.toString();
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
}
