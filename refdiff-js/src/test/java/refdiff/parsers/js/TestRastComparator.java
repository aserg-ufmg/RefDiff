package refdiff.parsers.js;

import static org.junit.Assert.assertThat;
import static refdiff.test.util.RastDiffMatchers.containsOnly;
import static refdiff.test.util.RastDiffMatchers.node;
import static refdiff.test.util.RastDiffMatchers.relationship;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import refdiff.core.diff.RastComparator;
import refdiff.core.diff.RastDiff;
import refdiff.core.diff.RelationshipType;
import refdiff.core.io.FileSystemSourceFile;
import refdiff.core.io.SourceFile;

public class TestRastComparator {

    private EsprimaParser parser;
    
    public TestRastComparator() throws Exception {
        this.parser = new EsprimaParser();
    }
    
    @Test
    public void shouldMatchWithSameNamePath() throws Exception {
        assertThat(diff("diff1/"), containsOnly(
            relationship(RelationshipType.SAME, node("hello.js"), node("hello.js")),
            relationship(RelationshipType.SAME, node("hello.js", "foo"), node("hello.js", "foo")),
            relationship(RelationshipType.SAME, node("hello.js", "bar"), node("hello.js", "bar"))
        ));
    }

    private RastDiff diff(String folder) throws Exception {
        String basePath = "src/test/resources/" + folder;
        
        String basePathV0 = basePath + "v0/";
        Set<SourceFile> sourceFilesBefore = Collections.singleton(new FileSystemSourceFile(basePathV0, "hello.js"));
        
        String basePathV1 = basePath + "v1/";
        Set<SourceFile> sourceFilesAfter = Collections.singleton(new FileSystemSourceFile(basePathV1, "hello.js"));
        
        RastComparator comparator = new RastComparator(this.parser, this.parser);

        RastDiff diff = comparator.compare(sourceFilesBefore, sourceFilesAfter);
        return diff;
    }
    
}
