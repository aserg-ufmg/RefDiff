package evaluation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import refdiff.core.io.GitHelper;

public class RepoUrl {
  private static final String JAVA_PROJECTS_FILE = "src/main/resources/java-projects.txt";
  private static final String C_PROJECTS_FILE = "src/main/resources/c-projects.txt";
  private static final String JS_PROJECTS_FILE = "src/main/resources/js-projects.txt";

  public static String[] getRepoUrls(Language lang) {
    if (lang == Language.JAVA) {
      return loadRepoUrlsFromFile(JAVA_PROJECTS_FILE);
    } else if (lang == Language.C) {
      return loadRepoUrlsFromFile(C_PROJECTS_FILE);
    } else if (lang == Language.JAVASCRIPT) {
      return loadRepoUrlsFromFile(JS_PROJECTS_FILE);
    } else {
      throw new IllegalArgumentException("Unsupported language for repo URLs.");
    }
  }

  private static String[] loadRepoUrlsFromFile(String filePath) {
    try {
      List<String> lines = Files.readAllLines(
          Paths.get(filePath),
          StandardCharsets.UTF_8
      );
      return lines.stream()
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .toArray(String[]::new);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read repo URLs from " + filePath, e);
    }
  }

  public static Map<String, File> cloneRepos(String[] repoUrls, File clonedRepositoryBaseDir) throws Exception {
    Map<String, File> clonedRepos = new HashMap<>();
    for (String repoUrl : repoUrls) {
        String owner = extractOwner(repoUrl);
        String repoName = extractRepoName(repoUrl);
        String repoMapKey = owner + "/" + repoName;
        if (!clonedRepos.containsKey(repoMapKey)) {
            File clonedRepo = clone(clonedRepositoryBaseDir, repoUrl, repoName);
            clonedRepos.put(repoMapKey, clonedRepo);
        }
    }
    return clonedRepos;
  }

  public static String extractOwner(String repoUrl) {
    String[] parts = repoUrl.split("/");
    return parts[3];
  }

  public static String extractRepoName(String repoUrl) {
    String[] parts = repoUrl.split("/");
    String repoNameWithGit = parts[4];
    if (repoNameWithGit.endsWith(".git")) {
        return repoNameWithGit.substring(0, repoNameWithGit.length() - 4);
    }
    return repoNameWithGit;
  }

  private static File clone(File destBaseDir, String url, String repoName) throws Exception {
    File repoDir = new File(destBaseDir, repoName);
    File clonedRepo = GitHelper.cloneBareRepository(repoDir, url);
    return clonedRepo;
  }
}