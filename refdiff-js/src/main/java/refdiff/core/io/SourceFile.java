package refdiff.core.io;

import java.io.IOException;

public interface SourceFile {

    String getPath();
    
    String getContent() throws IOException;
}
