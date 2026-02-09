package parser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import plugingenerator.Language;
import plugingenerator.TreeSitterParser;

public class JavaTsParser {
  private static final String[] REPO_URLS = new String[] {
    "https://github.com/macrozheng/mall/blob/master/mall-admin/src/main/java/com/macro/mall/validator/FlagValidator.java#L15",
  };
  
  public static void main(String[] args) throws Exception {
    for (String repoUrl : REPO_URLS) {
      System.out.println("Validating repo: " + repoUrl);
      Path repoPath = getRepoPath(repoUrl);
      String ast = TreeSitterParser.parse(repoPath, Language.JAVA.getTSLanguage());
      Path outPath = Path.of("./output/ast/%s/%s/".formatted(Language.JAVA.getName(), extractRepoName(repoUrl)));
      try {
        Files.createDirectories(outPath);
      } catch (Exception e) {
        throw new RuntimeException("Failed to create output directory: " + outPath, e);
      }
      Files.writeString(outPath.resolve(repoPath.getFileName().toString() + ".txt"), ast);
      System.out.println("AST written to: " + outPath);
    }
  }

  private static Path getRepoPath(String repoUrl) {
    // https://github.com/{owner}/{repo}/blob/{branch}/path/to/File.java#L15
    Pattern p = Pattern.compile("^https?://(?:www\\.)?github\\.com/([^/]+)/([^/]+)/(?:blob)/[^/]+/(.*)$");
    Matcher m = p.matcher(repoUrl);
    if (!m.find()) return null;
    String repo = m.group(2);
    String rest = m.group(3);
    if (rest != null) {
      int hash = rest.indexOf('#');
      if (hash != -1) rest = rest.substring(0, hash);
      int q = rest.indexOf('?');
      if (q != -1) rest = rest.substring(0, q);
    }
    return Path.of("./repo/" + repo + (rest == null || rest.isEmpty() ? "" : "/" + rest));
  }

  private static String extractRepoName(String repoUrl) {
    // https://github.com/{owner}/{repo}/blob/{branch}/path/to/File.java#L15
    Pattern p = Pattern.compile("^https?://(?:www\\.)?github\\.com/([^/]+)/([^/]+)/(?:blob)/[^/]+/(.*)$");
    Matcher m = p.matcher(repoUrl);
    if (!m.find())
      return null;
    String repo = m.group(2);
    return repo;
  }
}
