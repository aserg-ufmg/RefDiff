package refdiff.parsers.universal.common;

import org.junit.jupiter.api.Test;

public class TestFilePathUtils {
  @Test
  public void testExtractDirectoryFromFilePath() {
    String filePath1 = "dir1/dir2/file.c";
    String filePath2 = "dir1\\dir2\\file.c";
    String filePath3 = "file.c";

    assert FilePathUtils.extractDirectoryFromFilePath(filePath1).equals("dir1/dir2/");
    assert FilePathUtils.extractDirectoryFromFilePath(filePath2).equals("dir1\\dir2\\");
    assert FilePathUtils.extractDirectoryFromFilePath(filePath3).equals("");
  }

  @Test
  public void testExtractFileNameFromFilePath() {
    String filePath1 = "dir1/dir2/file.c";
    String filePath2 = "dir1\\dir2\\file.c";
    String filePath3 = "file.c";

    assert FilePathUtils.extractFileNameFromFilePath(filePath1).equals("file.c");
    assert FilePathUtils.extractFileNameFromFilePath(filePath2).equals("file.c");
    assert FilePathUtils.extractFileNameFromFilePath(filePath3).equals("file.c");
  }
}
