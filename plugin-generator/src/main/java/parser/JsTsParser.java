package parser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import plugingenerator.Language;
import plugingenerator.TreeSitterParser;

public class JsTsParser {
  private static final String[] REPO_URLS = new String[] {
    // "https://github.com/facebook/react/blob/main/scripts/bench/benchmarks/pe-class-components/benchmark.js#L454",
    // "https://github.com/facebook/react/blob/main/scripts/bench/benchmarks/hacker-news/benchmark.js#L307",
    // "https://github.com/facebook/react/blob/main/scripts/bench/benchmarks/pe-functional-components/benchmark.js#L4",
    // "https://github.com/facebook/react/blob/main/scripts/flags/flags.js#L150",
    // "https://github.com/facebook/react/blob/main/scripts/release/shared-commands/print-prerelease-summary.js#L9",
    // "https://github.com/facebook/react/blob/main/scripts/release/shared-commands/parse-params.js#L44,"
    // "https://github.com/facebook/react/blob/main/scripts/release/prepare-release-from-npm-commands/check-out-packages.js#L55",
    // "https://github.com/facebook/react/blob/main/scripts/rollup/build-all-release-channels.js#L469",
    // "https://github.com/facebook/react/blob/main/scripts/rollup/forks.js#L55",
    // "https://github.com/facebook/react/blob/main/scripts/jest/patchMessageChannel.js#L4",
    // "https://github.com/facebook/react/blob/main/scripts/flow/react-native-host-hooks.js#L12",
    // "https://github.com/facebook/react/blob/main/packages/react-devtools-inline/src/frontend.js#L37",
    // "https://github.com/facebook/react/blob/main/compiler/packages/babel-plugin-react-compiler/src/__tests__/fixtures/compiler/rules-of-hooks/todo.bail.rules-of-hooks-fadd52c1e460.js#L38",
    // "https://github.com/facebook/react/blob/main/compiler/packages/babel-plugin-react-compiler/src/__tests__/fixtures/compiler/rules-of-hooks/todo.invalid.invalid-rules-of-hooks-9c79feec4b9b.js#L5",
    // "https://github.com/facebook/react/blob/main/fixtures/stacks/BabelClasses-compiled.js#L39",
    // "https://github.com/facebook/react/blob/main/fixtures/legacy-jsx-runtimes/react-14/react-14.test.js#L279",
    // "https://github.com/facebook/react/blob/main/packages/react-server-dom-turbopack/src/server/ReactFlightDOMServerEdge.js#L175",
    // "https://github.com/facebook/react/blob/main/packages/react-server-dom-turbopack/src/server/ReactFlightDOMServerNode.js#L654",
    // "https://github.com/facebook/react/blob/main/packages/react-server-dom-turbopack/src/__tests__/ReactFlightTurbopackDOM-test.js#L68",
    // "https://github.com/facebook/react/blob/main/packages/react-dom/src/__tests__/ReactDOM-test.js#L348",
    // "https://github.com/facebook/react/blob/main/packages/react-dom/src/__tests__/ReactDOM-test.js#L382",
    // "https://github.com/facebook/react/blob/main/fixtures/legacy-jsx-runtimes/react-16/react-16.test.js#L33",
    // "https://github.com/facebook/react/blob/main/packages/react-refresh/src/__tests__/ReactFresh-test.js#L2092",
    "https://github.com/facebook/react/blob/main/packages/react/src/__tests__/ReactChildren-test.js#L416"
  };
 public static void main(String[] args) throws Exception {
    for (String repoUrl : REPO_URLS) {
      System.out.println("Validating repo: " + repoUrl);
      Path repoPath = getRepoPath(repoUrl);
      String ast = TreeSitterParser.parse(repoPath, Language.JS.getTSLanguage());
      Path outPath = Path.of("./output/ast/%s/%s/".formatted(Language.JS.getName(), extractRepoName(repoUrl)));
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
    // https://github.com/{owner}/{repo}/blob/{branch}/path/to/File.ext#L454
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
    // https://github.com/{owner}/{repo}/blob/{branch}/path/to/File.ext#L15
    Pattern p = Pattern.compile("^https?://(?:www\\.)?github\\.com/([^/]+)/([^/]+)/(?:blob)/[^/]+/(.*)$");
    Matcher m = p.matcher(repoUrl);
    if (!m.find())
      return null;
    String repo = m.group(2);
    return repo;
  }
}
