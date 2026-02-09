package refdiff.parsers.universal.java;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import refdiff.core.cst.CstRoot;
import refdiff.core.io.SourceFileSet;
import refdiff.core.io.SourceFolder;
import refdiff.parsers.LanguagePlugin;

public class TestParseError {
  private static final LanguagePlugin parser = new JavaPlugin();
  private static final String TEST_DATA_BASE_PATH = "src/test/resources/java/";

  @Test
  public void shouldDetectParseError() throws Exception {
    Path baseFolderPath = Paths.get(TEST_DATA_BASE_PATH + "fail");
    SourceFileSet sources = SourceFolder.from(baseFolderPath, ".java");
    CstRoot cstRoot = parser.parse(sources);

    assert(cstRoot.isFileParseFailed("foo.java"));
  }
}
