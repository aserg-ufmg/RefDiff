package evaluation;

import java.io.File;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import refdiff.core.RefDiff;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.Relationship;
import refdiff.parsers.LanguagePlugin;
import refdiff.parsers.universal.java.JavaPlugin;
import refdiff.parsers.universal.c.CPlugin;
import refdiff.parsers.universal.js.JsPlugin;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;

public class Executor {
    static final int COMMIT_DEPTH = 500;
    private int commitProcessingCount = 0;

    private void incrementCommitProcessingCount() {
        commitProcessingCount++;
    }

    private void initCommitProcessingCount() {
        commitProcessingCount = 0;
    }

    enum Option {
        PRECISION,
        RECALL,
        HEAD;

        public static Option build(String metricStr) {
            if (metricStr == null) {
                return null;
            }
            switch (metricStr.toLowerCase()) {
                case "precision":
                    return PRECISION;
                case "recall":
                    return RECALL;
                case "head":
                    return HEAD;
                default:
                    throw new IllegalArgumentException("Unknown metric: " + metricStr);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Please provide the programming language and optionally the metric as arguments.");
            return;
        }
        Language lang = mapLanguage(args[0]);
        Option option = null;
        if (args.length >= 2) {
            option = Option.build(args[1]);
        }
        String repoFilter = null; // e.g. react, linux
        if (args.length >= 3) {
            repoFilter = args[2];
        }

        Executor executor = new Executor();
        String fileName;
        if (option != null && repoFilter != null && !repoFilter.isEmpty()) {
            fileName = lang.toString().toLowerCase() + "-" + option.toString().toLowerCase() + "-" + repoFilter + "-" + executor.getNowDateTime() + ".csv";
        } else if (option != null) {
            fileName = lang.toString().toLowerCase() + "-" + option.toString().toLowerCase() + "-"
                    + executor.getNowDateTime() + ".csv";
        } else {
            fileName = lang.toString().toLowerCase() + "-" + executor.getNowDateTime() + ".csv";
        }

        String resultDir;
        if (option != null) {
            resultDir = "detection-result/" + lang.toString().toLowerCase() + "/" + option.toString().toLowerCase() + "/";
        } else {
            resultDir = "detection-result/" + lang.toString().toLowerCase() + "/";
        }
        Files.createDirectories(Paths.get(resultDir));
        String outputFilePath = resultDir + fileName;
        Files.write(Paths.get(outputFilePath), new byte[0], StandardOpenOption.CREATE_NEW);

        LanguagePlugin plugin = mapPlugin(lang);
        RefDiff refDiff = new RefDiff(plugin);
        File clonedRepositoryBaseDir = new File("repository");
        if (option == Executor.Option.HEAD) {
            String[] repoUrls = RepoUrl.getRepoUrls(lang);
            // If a repo filter is provided, keep only matching URLs by repo name 
            if (repoFilter != null && !repoFilter.isEmpty()) {
                java.util.List<String> filtered = new java.util.ArrayList<>();
                for (String url : repoUrls) {
                    try {
                        String repoName = RepoUrl.extractRepoName(url);
                        if (repoName.equals(repoFilter)) {
                            filtered.add(url);
                        }
                    } catch (Exception e) {
                        // ignore malformed URL entries
                    }
                }
                repoUrls = filtered.toArray(new String[0]);
            }
            if (repoUrls.length == 0) {
                System.out.println("No repositories found matching filter: " + repoFilter);
                return;
            }
            Map<String, File> repoMap = RepoUrl.cloneRepos(repoUrls, clonedRepositoryBaseDir);
            executor.runFromHead(refDiff, repoMap, outputFilePath);
        } else {
            String[] commitUrls = CommitUrl.getCommitUrls(lang, option);
            Map<String, File> repoMap = CommitUrl.cloneRepos(commitUrls, clonedRepositoryBaseDir);
            executor.runForEachCommit(refDiff, repoMap, commitUrls, outputFilePath);
        }        
    }

    private void runForEachCommit(RefDiff refDiff, Map<String, File> repoMap, String[] commitUrls, String outputFilePath) throws Exception {
        StringBuilder result = new StringBuilder();
        result.append("url,repository,commit,type,before,after\n"); // header
        for (int i = 0; i < commitUrls.length; i++) {
            String commitUrl = commitUrls[i];
            String owner = CommitUrl.extractOwner(commitUrl);
            String repoName = CommitUrl.extractRepoName(commitUrl);
            String sha1 = CommitUrl.extractSha1(commitUrl);
            String repoMapKey = owner + "/" + repoName;

            System.out.println("\nProcessing commit " + (i + 1) + "/" + commitUrls.length + " " + sha1 + " in repository " + repoName);

            File repo = repoMap.get(repoMapKey);
            // Detect refactorings
            try {
                CstDiff diff = refDiff.computeDiffForCommit(repo, sha1);
                for (Relationship rel : diff.getRefactoringRelationships()) {
                    result.append(String.format("\"%s\",\"%s\",\"%s\",%s\n", commitUrl, repoName, sha1, rel.getStandardDescriptionForCsv()));
                }
            } catch (Exception e) {
                String errorMsg = String.format("Error processing commit %s in repository %s: %s\n", sha1, repoName, e.getMessage());
                Files.write(Paths.get(outputFilePath + ".error.txt"),
                        String.format("%s\n", errorMsg).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
            }
        }
        writeResultToFile(outputFilePath, result.toString());
    }

    private void runFromHead(RefDiff refDiff, Map<String, File> repoMap, String outputFilePath) throws Exception {
        StringBuilder result = new StringBuilder();
        result.append("url,repository,commit,type,before,after\n"); // header
        for (String repoMapKey : repoMap.keySet()) {
            initCommitProcessingCount();
            File repo = repoMap.get(repoMapKey);
            refDiff.computeDiffForCommitHistory(repo, COMMIT_DEPTH, (commit, diff) -> {
                System.out.println("Processing commit " + (commitProcessingCount) + " / " + COMMIT_DEPTH + " " + commit.getName() + " in repository " + repoMapKey);
                incrementCommitProcessingCount();
                for (Relationship rel : diff.getRefactoringRelationships()) {
                    String owner = repoMapKey.split("/")[0];
                    String repoName = repoMapKey.split("/")[1];
                    String commitSha = commit.getName();
                    String url = CommitUrl.generateCommitDiffUrl(owner, repoName, commitSha, rel.getNodeBefore().getLocation().getFile());
                    result.append(String.format("\"%s\",\"%s\",\"%s\",%s\n", url, repoMapKey, commitSha, rel.getStandardDescriptionForCsv()));
                }
            });
            writeResultToFile(outputFilePath, result.toString());
            result.setLength(0); // Clear the result after writing to file
        }
    }

    private void writeResultToFile(String outputFilePath, String result) throws Exception {
        try {
            System.out.println("\nWriting results to " + outputFilePath);
            Files.write(Paths.get(outputFilePath), result.toString().getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            String errorMsg = String.format("Error during execution: %s", e.getMessage());
            System.err.println(errorMsg);
        }
    }

    private String getNowDateTime() {
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
            case "js":
                return Language.JAVASCRIPT;
            default:
                throw new IllegalArgumentException("Unsupported language: " + lang);
        }
    }

    private static LanguagePlugin mapPlugin(Language lang) {
        switch (lang) {
            case JAVA:
                return new JavaPlugin();
            case C:
                return new CPlugin();
            case JAVASCRIPT:
                return new JsPlugin();
            default:
                throw new IllegalArgumentException("Unsupported language: " + lang);
        }
    }
}
