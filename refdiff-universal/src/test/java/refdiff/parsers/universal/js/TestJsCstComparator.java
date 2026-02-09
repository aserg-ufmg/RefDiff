package refdiff.parsers.universal.js;

import static org.junit.Assert.assertThat;

import static refdiff.test.util.CstDiffMatchers.containsOnly;
import static refdiff.test.util.CstDiffMatchers.node;
import static refdiff.test.util.CstDiffMatchers.relationship;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import refdiff.core.diff.Relationship;
import refdiff.core.diff.CstComparator;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.RelationshipType;
import refdiff.core.io.SourceFolder;
import refdiff.parsers.LanguagePlugin;

public class TestJsCstComparator {

    private static final LanguagePlugin parser = new JsPlugin();
    private static final String TEST_DATA_BASE_PATH = "src/test/resources/js/refactor";

    private CstDiff diff(String folderName) throws Exception {
        Path baseFolderPath = Paths.get(TEST_DATA_BASE_PATH, folderName);
        SourceFolder sourcesBefore = SourceFolder.from(baseFolderPath.resolve("v0"), ".js");
        SourceFolder sourcesAfter = SourceFolder.from(baseFolderPath.resolve("v1"), ".js");
        CstComparator comparator = new CstComparator(parser);
        return comparator.compare(sourcesBefore, sourcesAfter);
    }

    @Test
    public void shouldMatchMoveFile() throws Exception {
        CstDiff diff = diff("moveFile");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.MOVE, node("script.js"), node("dir/script.js")),
            relationship(RelationshipType.SAME, node("script.js", "sayHello(name)"), node("dir/script.js", "sayHello(name)"))
        ));
    }

    @Test
    public void shouldMatchMoveClass() throws Exception {
        CstDiff diff = diff("moveClass");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.MOVE, node("script.js", "MyClass"), node("MyMovedClass.js", "MyClass")),
            relationship(RelationshipType.SAME, node("script.js"), node("script.js")),
            relationship(RelationshipType.SAME, node("script.js", "foo()"), node("script.js", "foo()"))
        ));
    }

    @Test
    public void shouldMatchMoveFunction() throws Exception {
        CstDiff diff = diff("moveFunction");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.MOVE, node("script.js", "foo()"), node("moved.js", "foo()")),
            relationship(RelationshipType.SAME, node("script.js"), node("script.js")),
            relationship(RelationshipType.SAME, node("script.js", "bar()"), node("script.js", "bar()"))
        ));
    }

    @Test
    public void shoudMatchRenameFile() throws Exception {
        CstDiff diff = diff("renameFile");
        assertThat(diff, containsOnly(
                relationship(RelationshipType.RENAME, node("script.js"), node("renamed.js")),
                relationship(RelationshipType.SAME, node("script.js", "sayHello(name)"), node("renamed.js", "sayHello(name)"))
        ));
    }

    @Test
    public void shoudMatchRenameClass() throws Exception {
        CstDiff diff = diff("renameClass");
        assertThat(diff, containsOnly(
                relationship(RelationshipType.RENAME, node("script.js", "MyClass"), node("script.js", "MyRenamedClass")),
                relationship(RelationshipType.SAME, node("script.js"), node("script.js"))
        ));
    }

    @Test
    public void shoudMatchRenameFunction() throws Exception {
        CstDiff diff = diff("renameFunction");
        assertThat(diff, containsOnly(
                relationship(RelationshipType.RENAME, node("script.js", "foo(msg)"), node("script.js", "renamed(msg)")),
                relationship(RelationshipType.SAME, node("script.js"), node("script.js"))));
    }

    @Test
    public void shouldMatchMoveAndRenameFile() throws Exception {
        CstDiff diff = diff("moveAndRenameFile");
        assertThat(diff, containsOnly(
                relationship(RelationshipType.MOVE_RENAME, node("script.js"), node("dir/greet.js")),
                relationship(RelationshipType.SAME, node("script.js", "sayHello(name)"), node("dir/greet.js", "sayHello(name)"))
        ));
    }

    @Test
    public void shouldMatchMoveAndRenameFunction() throws Exception {
        CstDiff diff = diff("moveAndRenameFunction");
        assertThat(diff, containsOnly(
                relationship(RelationshipType.MOVE_RENAME, node("script.js", "foo(name)"), node("foo.js", "renamed(name)")),
                relationship(RelationshipType.SAME, node("script.js", "bar()"), node("script.js", "bar()")),
                relationship(RelationshipType.SAME, node("script.js"), node("script.js"))
        ));
    }

    @Test
    public void shouldMatchExtractFunction() throws Exception {
        CstDiff diff = diff("extractFunction");
        assertThat(diff, containsOnly(
                relationship(RelationshipType.EXTRACT, node("script.js", "foo()"), node("script.js", "bar()")),
                relationship(RelationshipType.SAME, node("script.js", "foo()"), node("script.js", "foo()")),
                relationship(RelationshipType.SAME, node("script.js"), node("script.js"))));
    }
    
    @Test
    public void shouldMatchInlineFunction() throws Exception {
        CstDiff diff = diff("inlineFunction");
        assertThat(diff, containsOnly(
                relationship(RelationshipType.INLINE, node("script.js", "bar()"), node("script.js", "foo()")),
                relationship(RelationshipType.SAME, node("script.js", "foo()"), node("script.js", "foo()")),
                relationship(RelationshipType.SAME, node("script.js"), node("script.js"))));
    }
}
