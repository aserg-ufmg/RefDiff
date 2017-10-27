package refdiff.parsers.js;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import refdiff.parsers.FileContentReader;
import refdiff.rast.RastRoot;

public class TestEsprimaParser {

    @Test
    public void shouldParseSimpleFile() throws Exception {
        EsprimaParser parser = new EsprimaParser();
        Set<String> files = Collections.singleton("src/test/java/refdiff/parsers/js/hello.js");
        FileContentReader contentReader = new FileContentReader();
        RastRoot root = parser.parse(files, contentReader);

        root.forEachNode((node, depth) -> {
            for (int i = 0; i < depth; i++) System.out.print("  ");
            System.out.println(node);
        });
    }
}
