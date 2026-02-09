package refdiff.parsers.universal.go;

import static org.junit.Assert.assertThat;

import static refdiff.test.util.CstDiffMatchers.containsOnly;
import static refdiff.test.util.CstDiffMatchers.node;
import static refdiff.test.util.CstDiffMatchers.relationship;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import refdiff.core.diff.CstComparator;
import refdiff.core.diff.CstDiff;
import refdiff.core.io.SourceFolder;
import refdiff.parsers.LanguagePlugin;
import refdiff.core.diff.Relationship;
import refdiff.core.diff.RelationshipType;


public class TestGoCstComparator {
    private static final LanguagePlugin parser = new GoParser();
    private static final String TEST_DATA_BASE_PATH = "src/test/resources/go/refactor";

    private CstDiff diff(String folderName) throws Exception {
        Path baseFolderPath = Paths.get(TEST_DATA_BASE_PATH, folderName);
        SourceFolder sourcesBefore = SourceFolder.from(baseFolderPath.resolve("v0"), ".go");
        SourceFolder sourcesAfter = SourceFolder.from(baseFolderPath.resolve("v1"), ".go");
        CstComparator comparator = new CstComparator(parser);
        return comparator.compare(sourcesBefore, sourcesAfter);
    }

    @Test
    public void shouldMatchChangeSignature() throws Exception {
        CstDiff diff = diff("changeSignature");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.CHANGE_SIGNATURE, node("main.foo(a, b int)"), node("main.foo(c, d int)")),
            relationship(RelationshipType.SAME, node("main.bar()"), node("main.bar()"))
        ));
    }

    @Test
    public void shouldMatchRename() throws Exception {
        CstDiff diff = diff("rename");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.RENAME, node("main.foo()"), node("main.bar()"))
        ));
    }

    @Test
    public void shouldMatchMove() throws Exception {
        CstDiff diff = diff("move");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.MOVE, node("main.bar()"), node("bar.bar()")),
            relationship(RelationshipType.SAME, node("main.foo()"), node("main.foo()"))
        ));
    }

    // @Test // TODO レシーバを識別できるようにして検出できるようにする
    // public void shouldMatchInternalMove() throws Exception {
    //     CstDiff diff = diff("internalMove");
    //     for (Relationship r : diff.getRelationships()) {
    //         System.out.println(r);
    //     }
    //     assertThat(diff, containsOnly(
    //             relationship(RelationshipType.INTERNAL_MOVE, node("main.py/bar(self)"), node("main.py/bar(self)")),
    //             relationship(RelationshipType.SAME, node("main.py/foo(self)"), node("main.py/foo(self)"))));
    // }
    
    @Test
    public void shouldMatchMoveRename() throws Exception {
        CstDiff diff = diff("moveRename");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.MOVE_RENAME, node("main.bar()"), node("baz.baz()")),
            relationship(RelationshipType.SAME, node("main.foo()"), node("main.foo()"))
        ));
    }

    @Test
    public void shouldMatchExtract() throws Exception {
        CstDiff diff = diff("extract");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.EXTRACT, node("main.foo()"), node("main.extracted_method()")),
            relationship(RelationshipType.SAME, node("main.foo()"), node("main.foo()")),
            relationship(RelationshipType.SAME, node("main.bar()"), node("main.bar()"))
        ));
    }

    @Test
    public void shouldMatchInline() throws Exception {
        CstDiff diff = diff("inline");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.INLINE, node("main.calculateSum(a, b int)"), node("main.foo()")),
            relationship(RelationshipType.SAME, node("main.foo()"), node("main.foo()")),
            relationship(RelationshipType.SAME, node("main.bar()"), node("main.bar()"))
        ));
    }
}
