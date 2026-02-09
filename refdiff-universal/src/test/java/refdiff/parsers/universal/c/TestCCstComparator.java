package refdiff.parsers.universal.c;

import static org.hamcrest.MatcherAssert.assertThat;

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

public class TestCCstComparator {
  private static final LanguagePlugin parser = new CPlugin();
  private static final String TEST_DATA_BASE_PATH = "src/test/resources/c/refactor";

  private CstDiff diff(String folderName) throws Exception {
    Path baseFolderPath = Paths.get(TEST_DATA_BASE_PATH, folderName);
    SourceFolder sourcesBefore = SourceFolder.from(baseFolderPath.resolve("v0"), ".c");
    SourceFolder sourcesAfter = SourceFolder.from(baseFolderPath.resolve("v1"), ".c");
    CstComparator comparator = new CstComparator(parser);
    return comparator.compare(sourcesBefore, sourcesAfter);
  }

  @Test
  public void shouldMatchChangeSignature() throws Exception {
    CstDiff diff = diff("changeSignature");
    assertThat(diff, containsOnly(
        relationship(RelationshipType.CHANGE_SIGNATURE, node("main.c", "print()"), node("main.c", "print(char, int)")),
        relationship(RelationshipType.SAME, node("main.c"), node("main.c")),
        relationship(RelationshipType.SAME, node("main.c", "main()"), node("main.c", "main()"))));
  }

  @Test
  public void shouldMatchMoveFile() throws Exception {
    CstDiff diff = diff("moveFile");
    assertThat(diff, containsOnly(
        relationship(RelationshipType.MOVE, node("main.c"), node("foo/bar/main.c")),
        relationship(RelationshipType.SAME, node("main.c", "main()"), node("foo/bar/main.c", "main()"))));
  }

  @Test
  public void shouldMatchMoveFunction() throws Exception {
    CstDiff diff = diff("moveFunction");
    assertThat(diff, containsOnly(
        relationship(RelationshipType.MOVE, node("main.c", "foo()"), node("foo.c", "foo()")),
        relationship(RelationshipType.SAME, node("main.c", "main()"), node("main.c", "main()")),
        relationship(RelationshipType.SAME, node("main.c"), node("main.c"))));
  }

  @Test
  public void shouldMatchRenameFile() throws Exception {
    CstDiff diff = diff("renameFile");
    assertThat(diff, containsOnly(
        relationship(RelationshipType.RENAME, node("main.c"), node("foo.c")),
        relationship(RelationshipType.SAME, node("main.c", "main()"), node("foo.c", "main()"))));
  }

  @Test
  public void shouldMatchRenameFunction() throws Exception {
    CstDiff diff = diff("renameFunction");
    assertThat(diff, containsOnly(
        relationship(RelationshipType.SAME, node("main.c", "main()"), node("main.c", "main()")),
        relationship(RelationshipType.RENAME, node("main.c", "f2()"), node("main.c", "f3()")),
        relationship(RelationshipType.SAME, node("main.c"), node("main.c")),
        relationship(RelationshipType.SAME, node("main.c", "f1()"), node("main.c", "f1()"))));
  }

  @Test
  public void shouldMatchMoveAndRenameFile() throws Exception {
    CstDiff diff = diff("moveAndRenameFile");
    assertThat(diff, containsOnly(
        relationship(RelationshipType.SAME, node("main.c", "main()"), node("foo/foo.c", "main()")),
        relationship(RelationshipType.MOVE_RENAME, node("main.c"), node("foo/foo.c"))));
  }

  @Test
  public void shouldMatchMoveAndRenameFunction() throws Exception {
    CstDiff diff = diff("moveAndRenameFunction");
    assertThat(diff, containsOnly(
        relationship(RelationshipType.SAME, node("main.c", "main()"), node("main.c", "main()")),
        relationship(RelationshipType.SAME, node("main.c"), node("main.c")),
        relationship(RelationshipType.MOVE_RENAME, node("main.c", "hello()"), node("foo/greet.c", "greet()"))));
  }

  @Test
  public void shouldMatchExtractFunction() throws Exception {
    CstDiff diff = diff("extractFunction");
    assertThat(diff, containsOnly(
        relationship(RelationshipType.EXTRACT, node("main.c", "main()"), node("main.c", "hello()")),
        relationship(RelationshipType.SAME, node("main.c", "main()"), node("main.c", "main()")),
        relationship(RelationshipType.SAME, node("main.c"), node("main.c"))));
  }

  @Test
  public void shouldMatchInlineFunction() throws Exception {
    CstDiff diff = diff("inlineFunction");
    assertThat(diff, containsOnly(
        relationship(RelationshipType.SAME, node("main.c", "main()"), node("main.c", "main()")),
        relationship(RelationshipType.SAME, node("main.c"), node("main.c")),
        relationship(RelationshipType.INLINE, node("main.c", "hello()"), node("main.c", "main()"))));
  }
}
