package refdiff.parsers;

import java.util.List;

public interface SourceTokenizer {

    List<String> tokenize(String source);

}
