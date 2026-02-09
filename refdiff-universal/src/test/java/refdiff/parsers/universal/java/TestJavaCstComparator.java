package refdiff.parsers.universal.java;

import static org.junit.Assert.assertThat;
import static refdiff.test.util.CstDiffMatchers.containsOnly;
import static refdiff.test.util.CstDiffMatchers.node;
import static refdiff.test.util.CstDiffMatchers.relationship;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import refdiff.core.diff.CstComparator;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.RelationshipType;
import refdiff.core.diff.Relationship;
import refdiff.core.io.SourceFolder;
import refdiff.parsers.LanguagePlugin;

public class TestJavaCstComparator {

    private static final LanguagePlugin parser = new JavaPlugin();
    private static final String TEST_DATA_BASE_PATH = "src/test/resources/java/refactor";

    private CstDiff diff(String folderName) throws Exception {
        Path baseFolderPath = Paths.get(TEST_DATA_BASE_PATH, folderName);
        SourceFolder sourcesBefore = SourceFolder.from(baseFolderPath.resolve("v0"), ".java");
        SourceFolder sourcesAfter = SourceFolder.from(baseFolderPath.resolve("v1"), ".java");
        CstComparator comparator = new CstComparator(parser);
        return comparator.compare(sourcesBefore, sourcesAfter);
    }

    @Test
    public void shouldMatchMoveClass() throws Exception {
        CstDiff diff = diff("moveClass");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.MOVE, node("v0.Foo.Foo"), node("v1.Moved.Foo")),
            relationship(RelationshipType.SAME, node("v0.Foo.Foo", "main(String[])"), node("v1.Moved.Foo", "main(String[])"))
        ));
    }

    @Test
    public void shouldMatchMoveMethod() throws Exception {
        CstDiff diff = diff("moveMethod");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.SAME, node("tmp.Foo"), node("tmp.Foo")),
            relationship(RelationshipType.MOVE, node("tmp.Foo", "hello()"), node("tmp.Bar", "hello()")),
            relationship(RelationshipType.SAME, node("tmp.Foo", "main(String[])"), node("tmp.Foo", "main(String[])"))
        ));
    }

    @Test
    public void shouldMatchMoveAndRenameClass() throws Exception {
        CstDiff diff = diff("moveAndRenameClass");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.SAME, node("v0.Foo.Foo", "main(String[])"), node("v1.Bar.Bar", "main(String[])")),
            relationship(RelationshipType.MOVE_RENAME, node("v0.Foo.Foo"), node("v1.Bar.Bar"))
        ));
    }

    @Test
    public void shouldMatchRenameClass() throws Exception {
        CstDiff diff = diff("renameClass");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.SAME, node("pkg.Foo", "main(String[])"), node("pkg.Bar", "main(String[])")),
            relationship(RelationshipType.RENAME, node("pkg.Foo"), node("pkg.Bar"))
        ));
    }

    @Test
    public void shouldMatchRenameMethod() throws Exception {
        CstDiff diff = diff("renameMethod");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.RENAME, node("pkg.User", "isOkay()"), node("pkg.User", "isAdult()")),
            relationship(RelationshipType.SAME, node("pkg.User"), node("pkg.User")),
            relationship(RelationshipType.RENAME, node("pkg.Foo", "hello()"), node("pkg.Foo", "greet()")),
            relationship(RelationshipType.SAME, node("pkg.Foo"), node("pkg.Foo")),
            relationship(RelationshipType.SAME, node("pkg.User", "new(int)"), node("pkg.User", "new(int)"))
        ));
    }

    @Test
    public void shouldMatchExtractInterface() throws Exception {
        CstDiff diff = diff("extractInterface");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.EXTRACT_SUPER, node("pkg.Foo"), node("pkg.Bar")),
            relationship(RelationshipType.SAME, node("pkg.Foo"), node("pkg.Foo"))
        ));
    }

    @Test
    public void shouldMatchExtractSuperclass() throws Exception {
        CstDiff diff = diff("extractSuperclass");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.EXTRACT_SUPER, node("pkg.Foo"), node("pkg.Bar")),
            relationship(RelationshipType.SAME, node("pkg.Foo"), node("pkg.Foo"))
        ));
    }

    @Test
    public void shouldMatchPullUpMethod() throws Exception {
        CstDiff diff = diff("pullUpMethod");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.SAME, node("pkg.Foo"), node("pkg.Foo")),
            relationship(RelationshipType.PULL_UP, node("pkg.Foo", "greet()"), node("pkg.Bar", "greet()")),
            relationship(RelationshipType.SAME, node("pkg.Bar"), node("pkg.Bar"))
        ));
    }

    @Test
    public void shouldMatchPushDownMethod() throws Exception {
        CstDiff diff = diff("pushDownMethod");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.SAME, node("pkg.Bar"), node("pkg.Bar")),
            relationship(RelationshipType.SAME, node("pkg.Foo"), node("pkg.Foo")),
            relationship(RelationshipType.PUSH_DOWN, node("pkg.Bar", "greet()"), node("pkg.Foo", "greet()"))
        ));
    }

    @Test
    public void shouldMatchExtractMethod() throws Exception {
        CstDiff diff = diff("extractMethod");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.SAME, node("pkg.Foo"), node("pkg.Foo")),
            relationship(RelationshipType.EXTRACT, node("pkg.Foo", "main(String[])"), node("pkg.Foo", "hello(String[])")),
            relationship(RelationshipType.SAME, node("pkg.Foo", "main(String[])"), node("pkg.Foo", "main(String[])"))
        ));
    }

    @Test
    public void shouldMatchExtractAndMoveMethod() throws Exception {
        CstDiff diff = diff("extractAndMoveMethod");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.EXTRACT_MOVE, node("pkg.Foo", "main(String[])"), node("pkg.Bar", "hello(String[])")),
            relationship(RelationshipType.SAME, node("pkg.Foo"), node("pkg.Foo")),
            relationship(RelationshipType.SAME, node("pkg.Foo", "main(String[])"), node("pkg.Foo", "main(String[])"))
        ));
    }

    @Test
    public void shouldMatchInlineMethod() throws Exception {
        CstDiff diff = diff("InlineMethod"); // Folder name is "InlineMethod"
        assertThat(diff, containsOnly(
            relationship(RelationshipType.INLINE, node("pkg.Foo", "hello(String[])"), node("pkg.Foo", "main(String[])")),
            relationship(RelationshipType.SAME, node("pkg.Foo", "main(String[])"), node("pkg.Foo", "main(String[])")),
            relationship(RelationshipType.SAME, node("pkg.Foo"), node("pkg.Foo"))
        ));
    }

    @Test
    public void shouldMatchInternalMoveMethod() throws Exception {
        CstDiff diff = diff("InternalMoveMethod"); // Folder name is "InternalMoveMethod"
        assertThat(diff, containsOnly(
            relationship(RelationshipType.SAME, node("pkg.Foo"), node("pkg.Foo")),
            relationship(RelationshipType.SAME, node("pkg.Foo", "Bar"), node("pkg.Foo", "Bar")),
            relationship(RelationshipType.INTERNAL_MOVE, node("pkg.Foo", "Bar", "hello(String[])"), node("pkg.Foo", "hello(String[])")),
            relationship(RelationshipType.SAME, node("pkg.Foo", "main(String[])"), node("pkg.Foo", "main(String[])"))
        ));
    }
}