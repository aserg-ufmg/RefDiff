package refdiff.core.io;

import java.io.IOException;

public interface SourceReader {

    String readAllContent(String relativePath) throws IOException;

}
