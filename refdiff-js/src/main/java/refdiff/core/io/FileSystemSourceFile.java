package refdiff.core.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileSystemSourceFile implements SourceFile {

    private final String basePath;
    private final String path;
    
    public FileSystemSourceFile(String basePath, String path) {
        this.basePath = basePath;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String getContent() throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(basePath + path));
        return new String(encoded, StandardCharsets.UTF_8);
    }
    
}
