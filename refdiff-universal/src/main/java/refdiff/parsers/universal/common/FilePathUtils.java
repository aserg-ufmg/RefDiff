package refdiff.parsers.universal.common;

public class FilePathUtils {
  public static String extractDirectoryFromFilePath(String filePath) {
    int lastSlash = filePath.lastIndexOf('/');
    int lastBackslash = filePath.lastIndexOf('\\');
    int lastSeparator = Math.max(lastSlash, lastBackslash);

    if (lastSeparator != -1) {
      return filePath.substring(0, lastSeparator + 1);
    }
    return "";
  }

  public static String extractFileNameFromFilePath(String filePath) {
    int lastSlash = filePath.lastIndexOf('/');
    int lastBackslash = filePath.lastIndexOf('\\');
    int lastSeparator = Math.max(lastSlash, lastBackslash);

    if (lastSeparator != -1) {
      return filePath.substring(lastSeparator + 1);
    }
    return filePath;
  }
}
