package refdiff.core.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SourceFolder extends SourceFileSet {
	
	private final Path basePath;
	private final Charset charset;
	
	public static SourceFolder from(Path basePath, String... fileExtensions) {
		try {
			List<SourceFile> files = getSourceFiles(basePath, fileExtensions);
			return new SourceFolder(basePath, StandardCharsets.UTF_8, files);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static SourceFolder from(Path basePath, Collection<Path> paths) {
		List<SourceFile> files = paths.stream().map(path -> new SourceFile(path)).collect(Collectors.toList());
		return new SourceFolder(basePath, StandardCharsets.UTF_8, files);
	}
	
	public static SourceFolder from(Path basePath, Path... paths) {
		return from(basePath, Arrays.asList(paths));
	}
	
	private SourceFolder(Path folderPath, Charset charset, List<SourceFile> files) {
		super(files);
		this.basePath = folderPath;
		this.charset = charset;
	}
	
	private static List<SourceFile> getSourceFiles(Path basePath, String... fileExtensions) throws IOException {
		try (Stream<Path> stream = Files.walk(basePath)) {
			return stream.filter(path -> hasExtension(path, fileExtensions))
				.map(path -> new SourceFile(basePath.relativize(path)))
				.collect(Collectors.toList());
		}
	}
	
	private static boolean hasExtension(Path filePath, String... fileExtensions) {
		for (String fileExtension : fileExtensions) {
			if (filePath.getFileName().toString().endsWith(fileExtension)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String readContent(SourceFile sourceFile) throws IOException {
		byte[] encoded = Files.readAllBytes(basePath.resolve(sourceFile.getPath()));
		return new String(encoded, charset);
	}
	
	@Override
	public Optional<Path> getBasePath() {
		return Optional.of(basePath);
	}

	@Override
	public String describeLocation(SourceFile sourceFile) {
		return basePath.resolve(sourceFile.getPath()).toString();
	}
}
