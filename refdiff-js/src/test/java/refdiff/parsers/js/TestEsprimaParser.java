package refdiff.parsers.js;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import refdiff.core.io.FileSystemReader;
import refdiff.core.io.SourceReader;
import refdiff.core.rast.RastRoot;

public class TestEsprimaParser {

    @Test
    public void shouldParseSimpleFile() throws Exception {
        EsprimaParser parser = new EsprimaParser();
        Set<String> files = Collections.singleton("src/test/java/refdiff/parsers/js/hello.js");
        SourceReader contentReader = new FileSystemReader();
        RastRoot root = parser.parse(files, contentReader);

        root.forEachNode((node, depth) -> {
            for (int i = 0; i < depth; i++) System.out.print("  ");
            System.out.println(node);
        });
    }
}
