package refdiff.parsers.universal.common;

import refdiff.core.io.SourceFile;
import refdiff.core.io.SourceFileSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceFileReader {

    public static Map<String, String> readAllSourceFiles(SourceFileSet folder) {
        Map<String, String> sourceFileContents = new HashMap<>();
        List<SourceFile> files = folder.getSourceFiles();
        for (SourceFile file : files) {
            try {
                String content = folder.readContent(file);
                sourceFileContents.put(file.getPath(), content);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read source file: " + file.getPath(), e);
            }
        }
        return sourceFileContents;
    }
}