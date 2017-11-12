package refdiff.core.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemSourceFile implements SourceFile {

    private final Path basePath;
    private final Path path;
    
    public FileSystemSourceFile(Path basePath, Path path) {
        this.basePath = basePath;
        this.path = path;
    }

    public String getPath() {
        return path.toString();
    }

    @Override
    public String getContent() throws IOException {
        byte[] encoded = Files.readAllBytes(basePath.resolve(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }
    
    @Override
    public String toString() {
        return basePath.resolve(path).toString();
    }
}
