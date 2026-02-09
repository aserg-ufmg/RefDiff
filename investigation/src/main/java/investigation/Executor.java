package investigation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import refdiff.core.RefDiff;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.Relationship;
import refdiff.core.io.GitHelper;
import refdiff.parsers.LanguagePlugin;
import refdiff.parsers.universal.java.JavaPlugin;
import refdiff.parsers.universal.c.CPlugin;
import refdiff.parsers.universal.js.JsPlugin;
import refdiff.parsers.universal.go.GoParser;
import refdiff.parsers.universal.python.PythonParser;
import refdiff.parsers.universal.ruby.RubyParser;
import refdiff.parsers.universal.php.PhpPlugin;

public class Executor {
    private static int COMMIT_DEPTH = 2000000;
    private static final int BATCH_SIZE = 1000;

    private int commitCount = 0;
    private String currentCommitSha = "";

    public static void main(String[] args) throws Exception {
        // CLI option: --resume and --language <lang> or -l <lang>
        boolean resume = false;
        String languageArg = null;
        String repoArg = null;
        String resultDirArg = null;
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if ("--resume".equals(a)) {
                resume = true;
            } else if ("--language".equals(a) || "-l".equals(a)) {
                if (i + 1 < args.length) {
                    languageArg = args[i + 1];
                    i++;
                }
            } else if (a.startsWith("--language=")) {
                languageArg = a.substring("--language=".length());
            } else if ("--repo".equals(a) || "-r".equals(a)) {
                if (i + 1 < args.length) {
                    repoArg = args[i + 1];
                    i++;
                }
            } else if (a.startsWith("--repo=")) {
                repoArg = a.substring("--repo=".length());
            } else if (a.startsWith("--result-dir=")) {
                resultDirArg = a.substring("--result-dir=".length());
            } else if ("--result-dir".equals(a)) {
                if (i + 1 < args.length) {
                    resultDirArg = args[i + 1];
                    i++;
                }
            }
        }

        // Validation: when --repo is specified, --language must also be provided
        if (repoArg != null && (languageArg == null || languageArg.trim().isEmpty())) {
            System.err.println("Error: --language is required when --repo is specified.");
            System.exit(1);
        }

        new Executor().execute(resume, languageArg, repoArg, resultDirArg);
    }

    private void incrementCommitCount() {
        commitCount++;
    }

    private void setCurrentCommitSha(String sha) {
        currentCommitSha = sha;
    }

    private String resultBase = "result";

    private void execute(boolean resume, String selectedLanguage, String selectedRepoUrl, String resultBaseDir) throws Exception {
        if (resultBaseDir != null && !resultBaseDir.trim().isEmpty()) {
            this.resultBase = resultBaseDir;
        }
        Map<String, Map<String, File>> clonedReposByLang;
        if (selectedRepoUrl == null || selectedRepoUrl.trim().isEmpty()) {
            clonedReposByLang = getRepos(selectedLanguage);
        } else {
            clonedReposByLang = getRepos(selectedLanguage, selectedRepoUrl);
        }

        System.out.println("\n\n----- Detect refactorings -----");
        for (Map.Entry<String, Map<String, File>> langEntry : clonedReposByLang.entrySet()) {
            String lang = langEntry.getKey();

            // If user specified a language, skip other languages
            if (selectedLanguage != null && !selectedLanguage.trim().isEmpty()) {
                if (!lang.equalsIgnoreCase(selectedLanguage.trim())) {
                    continue;
                }
            }

            Map<String, File> repos = langEntry.getValue();
            for (Map.Entry<String, File> entry : repos.entrySet()) {
                String repoName = entry.getKey();
                File repoDir = entry.getValue();

                String startCommitSha = null;
                if (resume) {
                    startCommitSha = getLatestCommitSha(lang, repoName);
                    if (startCommitSha == null) {
                        System.out.println("No latest commit found for " + repoName);
                        continue;
                    }
                }

                Path outDir = Paths.get(this.resultBase, lang);
                Files.createDirectories(outDir);

                commitCount = 0;

                StringBuilder sb = new StringBuilder();
                setCurrentCommitSha(startCommitSha != null ? startCommitSha : "HEAD");
                Language language = mapLanguage(lang);
                LanguagePlugin plugin = mapPlugin(language);
                RefDiff refDiffUniversal = new RefDiff(plugin);
                final String startCommitShaFinal = startCommitSha;
                for (int attempt = 1; attempt <= 3; attempt++) {
                    try {
                        refDiffUniversal.computeDiffForCommitHistory(repoDir, currentCommitSha, COMMIT_DEPTH,
                                (commit, diff) -> {
                                    String commitSha = commit.getName();
                                    // Skip the last processed commit when resuming to avoid duplication
                                    if (startCommitShaFinal != null && startCommitShaFinal.equals(commitSha)) {
                                        System.out.println("Skipping already processed commit " + commitSha + " for " + repoName);
                                        return;
                                    }
                                    if (diff.getRefactoringRelationships().isEmpty()) {
                                        sb.append(String.format("\"%s\"\n", commitSha));
                                    } else {
                                        for (Relationship rel : diff.getRefactoringRelationships()) {
                                            sb.append(String.format("\"%s\",%s\n", commitSha,
                                                    rel.getDescriptionWithLocInCsv()));
                                        }
                                    }
                                    if ((commitCount + 1) % BATCH_SIZE == 0 || commitCount + 1 == COMMIT_DEPTH) {
                                        int processed = commitCount + 1;
                                        writeBatch(outDir, repoName, sb, processed);
                                    }
                                    incrementCommitCount();
                                    setCurrentCommitSha(commitSha);
                                });
                    } catch (Exception e) {
                        System.err.println("Error processing repository " + repoName + " (attempt " + attempt + "): "
                                + e.getMessage());
                        writeBatch(outDir, repoName, sb, attempt);
                        sb.setLength(0); // clear the StringBuilder
                        if (attempt == 3) {
                            System.err.println("Max attempts reached for repository " + repoName + ". Skipping.");
                        } else {
                            System.out.println("Retrying...");
                            Thread.sleep(2000); // wait before retrying
                            continue;
                        }
                    }
                    break; // exit the retry loop if successful
                }

                // Write any remaining results (final partial batch)
                if (sb.length() > 0) {
                    int processed = commitCount; // total processed so far
                    if (processed <= 0) {
                        processed = 0;
                    }
                    writeBatch(outDir, repoName, sb, processed);
                }
            }
        }
    }

    private String getLatestCommitSha(String lang, String repoName) {
        Path resultDir = Paths.get(this.resultBase, lang);
        if (!Files.exists(resultDir)) {
            return null;
        }

        try (Stream<Path> stream = Files.list(resultDir)) {
            Optional<Path> latestCsv = stream
                    .filter(p -> p.getFileName().toString().startsWith(repoName + "-")
                            && p.getFileName().toString().endsWith(".csv"))
                    .max(Comparator.comparingLong(p -> {
                        try {
                            return Files.getLastModifiedTime(p).toMillis();
                        } catch (IOException e) {
                            return 0L;
                        }
                    }));

            if (latestCsv.isPresent()) {
                Path csvPath = latestCsv.get();
                List<String> lines = Files.readAllLines(csvPath);
                if (!lines.isEmpty()) {
                    for (int i = lines.size() - 1; i >= 0; i--) {
                        String lastLine = lines.get(i);
                        if (lastLine != null && !lastLine.trim().isEmpty()) {
                            String[] columns = lastLine.split(",");
                            if (columns.length > 0) {
                                String commitSha = columns[0].replace("\"", "");
                                if (!commitSha.trim().isEmpty()) {
                                    if (commitSha.length() != 40) {
                                        continue;
                                    }
                                    System.out.println("Latest commit " + commitSha + " for " + repoName
                                            + " file " + csvPath.getFileName());
                                    return commitSha;
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error finding latest commit for " + repoName + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Map: language -> (repoName -> repoDir)
     */
    private Map<String, Map<String, File>> getRepos(String selectedLanguage) {
        Map<String, Map<String, File>> clonedReposByLang = new HashMap<>(); // TODO: Make a hash map which has just a single language
        for (Map.Entry<String, String[]> langEntry : RepoConfig.REPOS_BY_LANGUAGE.entrySet()) {
            String lang = langEntry.getKey();
            // If a specific language is requested, skip other languages
            if (selectedLanguage != null && !selectedLanguage.trim().isEmpty()) {
                if (!lang.equalsIgnoreCase(selectedLanguage.trim())) {
                    continue;
                }
            }
            String[] repoUrls = langEntry.getValue();
            if (repoUrls == null) {
                continue;
            }
            for (String repoUrl : repoUrls) {
                try {
                    if (repoUrl == null || repoUrl.trim().isEmpty()) {
                        continue;
                    }
                    String repoName = extractRepoNameFromUrl(repoUrl);

                    File tempFolder = new File("repo");
                    File repoDir = new File(tempFolder, repoName);
                    File clonedRepo = GitHelper.cloneBareRepository(repoDir, repoUrl);
                    clonedReposByLang.computeIfAbsent(lang, ignored -> new HashMap<>()).put(repoName, clonedRepo);
                    System.out.println("Cloned " + repoName + " (" + lang + ") to " + clonedRepo.getAbsolutePath());
                } catch (Exception e) {
                    System.err.println("Failed to clone " + repoUrl + ": " + e.getMessage());
                }
            }
        }
        return clonedReposByLang;
    }

    private Map<String, Map<String, File>> getRepos(String selectedLanguage, String selectedRepoUrl) {
        // If no specific repo URL is provided, delegate to the original single-arg method
        if (selectedRepoUrl == null || selectedRepoUrl.trim().isEmpty()) {
            return getRepos(selectedLanguage);
        }

        Map<String, Map<String, File>> clonedReposByLang = new HashMap<>();
        String repoUrl = selectedRepoUrl.trim();
        String repoName = extractRepoNameFromUrl(repoUrl);
        try {
            File tempFolder = new File("repo");
            File repoDir = new File(tempFolder, repoName);
            File clonedRepo = GitHelper.cloneBareRepository(repoDir, repoUrl);
            clonedReposByLang.computeIfAbsent(selectedLanguage, ignored -> new HashMap<>()).put(repoName, clonedRepo);
            System.out.println("Cloned " + repoName + " (" + selectedLanguage + ") to " + clonedRepo.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to clone " + repoUrl + ": " + e.getMessage());
        }
        return clonedReposByLang;
    }

    private static String extractRepoNameFromUrl(String repoUrl) {
        if (repoUrl == null) {
            return null;
        }
        String[] parts = repoUrl.split("/");
        if (parts.length == 0) {
            return repoUrl;
        }
        String repoNameWithGit = parts[parts.length - 1];
        if (repoNameWithGit.endsWith(".git")) {
            return repoNameWithGit.substring(0, repoNameWithGit.length() - 4);
        }
        return repoNameWithGit;
    }

    private static String getNowDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMdd-HHmm");
        return now.format(formatter);
    }

    private static Language mapLanguage(String lang) {
        switch (lang.toLowerCase()) {
            case "java":
                return Language.JAVA;
            case "c":
                return Language.C;
            case "javascript":
                return Language.JAVASCRIPT;
            case "go":
                return Language.GO;
            case "python":
                return Language.PYTHON;
            case "ruby":
                return Language.RUBY;
            case "php":
                return Language.PHP;
            default:
                throw new IllegalArgumentException("Unsupported language: " + lang);
        }
    }

    private static LanguagePlugin mapPlugin(Language language) {
        switch (language) {
            case JAVA:
                return new JavaPlugin();
            case C:
                return new CPlugin();
            case JAVASCRIPT:
                return new JsPlugin();
            case GO:
                return new GoParser();
            case PYTHON:
                return new PythonParser();
            case RUBY:
                return new RubyParser();
            case PHP:
                return new PhpPlugin();
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
    }

    private void writeBatch(Path outDir, String repoName, StringBuilder sb, int processed) {
        String fileName = repoName + "-" + getNowDateTime() + "-" + processed + ".csv";
        Path batchPath = outDir.resolve(fileName);
        System.out.println(getNowDateTime() + " Processed " + processed + " commits for " + repoName
                + ". Writing results to file " + batchPath.getFileName() + "...");
        try {
            if (Files.exists(batchPath)) {
                // If the file already exists, always append the new data (do not rewrite header)
                Files.write(batchPath, sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            } else {
                // File doesn't exist yet: write header followed by content
                String header = "\"Commit\",\"RefactoringType\",\"Before\",\"After\",\"BeforeLOC\",\"AfterLOC\"\n";
                String content = header + sb.toString();
                Files.write(batchPath, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE);
            }
            
            sb.setLength(0);
        } catch (IOException e) {
            System.err.println("Failed to write results for " + repoName + ": " + e.getMessage());
        }
    }
}
