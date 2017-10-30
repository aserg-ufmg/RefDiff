package refdiff.parsers.js;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import refdiff.core.io.FileSystemSourceFile;
import refdiff.core.io.SourceFile;
import refdiff.core.rast.RastRoot;

public class TestEsprimaParser {

    @Test
    public void shouldParseSimpleFile() throws Exception {
        EsprimaParser parser = new EsprimaParser();
        String basePath = "src/test/resources/";
        Set<SourceFile> sourceFiles = Collections.singleton(new FileSystemSourceFile(basePath, "hello.js"));
        RastRoot root = parser.parse(sourceFiles);

        root.forEachNode((node, depth) -> {
            for (int i = 0; i < depth; i++) System.out.print("  ");
            System.out.println(node);
        });
    }
}
