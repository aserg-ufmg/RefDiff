package refdiff.core.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileSystemReader implements SourceReader {

    public String readAllContent(String relativePath) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(relativePath));
        return new String(encoded, StandardCharsets.UTF_8);
    }

}
