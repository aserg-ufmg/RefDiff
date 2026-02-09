package refdiff.parsers.universal.ruby;

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


public class TestRubyCstComparator {
  
    private static final LanguagePlugin parser = new RubyParser();
    private static final String TEST_DATA_BASE_PATH = "src/test/resources/ruby/refactor";

    private CstDiff diff(String folderName) throws Exception {
        Path baseFolderPath = Paths.get(TEST_DATA_BASE_PATH, folderName);
        SourceFolder sourcesBefore = SourceFolder.from(baseFolderPath.resolve("v0"), ".rb");
        SourceFolder sourcesAfter = SourceFolder.from(baseFolderPath.resolve("v1"), ".rb");
        CstComparator comparator = new CstComparator(parser);
        return comparator.compare(sourcesBefore, sourcesAfter);
    }

    @Test
    public void shouldMatchChangeSignature() throws Exception {
        CstDiff diff = diff("changeSignature");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.CHANGE_SIGNATURE, node("main.rb/foo(a, b)"), node("main.rb/foo(c, d)")),
            relationship(RelationshipType.SAME, node("main.rb/bar()"), node("main.rb/bar()"))
        ));
    }

    @Test
    public void shouldMatchRename() throws Exception {
        CstDiff diff = diff("rename");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.RENAME, node("main.rb/foo()"), node("main.rb/bar()"))
        ));
    }

    @Test
    public void shouldMatchMove() throws Exception {
        CstDiff diff = diff("move");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.MOVE, node("main.rb/bar()"), node("bar.rb/bar()")),
            relationship(RelationshipType.SAME, node("main.rb/foo()"), node("main.rb/foo()"))
        ));
    }

    // @Test
    public void shouldMatchInternalMove() throws Exception {
        CstDiff diff = diff("internalMove");
        for (Relationship r : diff.getRelationships()) {
            System.out.println(r);
        }
        assertThat(diff, containsOnly(
                relationship(RelationshipType.INTERNAL_MOVE, node("main.rb/bar()"), node("bar.rb/bar()")),
                relationship(RelationshipType.SAME, node("main.rb/foo()"), node("main.rb/foo()"))));
    }
    
    @Test
    public void shouldMatchMoveRename() throws Exception {
        CstDiff diff = diff("moveRename");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.MOVE_RENAME, node("main.rb/bar()"), node("baz.rb/baz()")),
            relationship(RelationshipType.SAME, node("main.rb/foo()"), node("main.rb/foo()"))
        ));
    }

    @Test
    public void shouldMatchExtract() throws Exception {
        CstDiff diff = diff("extract");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.EXTRACT, node("main.rb/foo()"), node("main.rb/extracted_method()")),
            relationship(RelationshipType.SAME, node("main.rb/foo()"), node("main.rb/foo()")),
            relationship(RelationshipType.SAME, node("main.rb/bar()"), node("main.rb/bar()"))
        ));
    }

    @Test
    public void shouldMatchInline() throws Exception {
        CstDiff diff = diff("inline");
        assertThat(diff, containsOnly(
            relationship(RelationshipType.INLINE, node("main.rb/calculate_sum(a, b)"), node("main.rb/foo()")),
            relationship(RelationshipType.SAME, node("main.rb/foo()"), node("main.rb/foo()")),
            relationship(RelationshipType.SAME, node("main.rb/bar()"), node("main.rb/bar()"))
        ));
    }
}
