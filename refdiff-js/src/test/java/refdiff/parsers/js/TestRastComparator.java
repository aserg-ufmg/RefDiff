package refdiff.parsers.js;

import static org.junit.Assert.assertThat;
import static refdiff.test.util.RastDiffMatchers.contains;
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

    @Test
    public void diffShouldContain() throws Exception {
        EsprimaParser parser = new EsprimaParser();
        
        String basePathV0 = "src/test/resources/diff1/v0/";
        Set<SourceFile> sourceFilesBefore = Collections.singleton(new FileSystemSourceFile(basePathV0, "hello.js"));
        
        String basePathV1 = "src/test/resources/diff1/v1/";
        Set<SourceFile> sourceFilesAfter = Collections.singleton(new FileSystemSourceFile(basePathV1, "hello.js"));
        
        RastComparator comparator = new RastComparator(parser, parser);

        RastDiff diff = comparator.compare(sourceFilesBefore, sourceFilesAfter);
        
        assertThat(diff, contains(
            relationship(RelationshipType.SAME, "hello.js#hello", "hello.js#hello")
        ));
        
    }
    
    
}
