package validation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import refdiff.core.cst.CstNode;

public class MatchExporter {
  private static final List<String> HEADERS = Arrays.asList(
      "exactMatch",
      "notes",
      "url",
      "tagName",
      "tagKind",
      "tagPath",
      "tagLine",
      "tagPattern",
      "type",
      "simpleName",
      "localName",
      "file",
      "startLine",
      "matchedFile",
      "matchedLine",
      "matchedType",
      "matchedName"
  );
  public static String export(List<Match> matches, String repoUrl) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.join(",", HEADERS)).append('\n');

    String defaultBranch = Repository.detectDefaultBranch(repoUrl);
    for (Match match : matches) {
      String csv = toCsv(match, repoUrl, defaultBranch);
      sb.append(csv);
    }

    return sb.toString();
  }

  private static String toCsv(Match match,String repoUrl, String branch) {
    StringBuilder sb = new StringBuilder();

    String notesJoined = String.join("|", match.getNotes());

    if (match.getCstNodeCandidates().isEmpty()) {
      Map<String, String> row = new LinkedHashMap<>();
      row.put("exactMatch", String.valueOf(match.getExactMatch()));
      row.put("notes", notesJoined);
      row.put("url", Repository.generateRemoteUrl(repoUrl, branch, match.getTag().getPath(), match.getTag().getLine()));
      row.put("tagName", Optional.ofNullable(match.getTag().getName()).orElse(""));
      row.put("tagKind", Optional.ofNullable(match.getTag().getKind()).orElse(""));
      row.put("tagPath", Optional.ofNullable(match.getTag().getPath()).orElse(""));
      row.put("tagLine", String.valueOf(match.getTag().getLine()));
      row.put("tagPattern", Optional.ofNullable(match.getTag().getPattern()).orElse(""));
      sb.append(buildCsvRow(row));
      return sb.toString();
    }

    for (CstNode node : match.getCstNodeCandidates()) {
      Map<String, Boolean> fieldMatches = match.getMatchedFields().get(node.getId());
      if (fieldMatches == null) {
        fieldMatches = new HashMap<>();
        fieldMatches.put("file", false);
        fieldMatches.put("line", false);
        fieldMatches.put("type", false);
        fieldMatches.put("name", false);
      }

      Map<String, String> row = new LinkedHashMap<>();
      row.put("exactMatch", String.valueOf(match.getExactMatch()));
      row.put("notes", notesJoined);
      row.put("url", Repository.generateRemoteUrl(repoUrl, branch, match.getTag().getPath(), match.getTag().getLine()));
      row.put("tagName", Optional.ofNullable(match.getTag().getName()).orElse(""));
      row.put("tagKind", Optional.ofNullable(match.getTag().getKind()).orElse(""));
      row.put("tagPath", Optional.ofNullable(match.getTag().getPath()).orElse(""));
      row.put("tagLine", String.valueOf(match.getTag().getLine()));
      row.put("tagPattern", Optional.ofNullable(match.getTag().getPattern()).orElse(""));

      row.put("matchedFile", String.valueOf(fieldMatches.getOrDefault("file", false)));
      row.put("matchedLine", String.valueOf(fieldMatches.getOrDefault("line", false)));
      row.put("matchedType", String.valueOf(fieldMatches.getOrDefault("type", false)));
      row.put("matchedName", String.valueOf(fieldMatches.getOrDefault("name", false)));

      row.put("type", node == null ? "" : Optional.ofNullable(node.getType()).orElse(""));
      row.put("simpleName", node == null ? "" : Optional.ofNullable(node.getSimpleName()).orElse(""));
      row.put("localName", node == null ? "" : Optional.ofNullable(node.getLocalName()).orElse(""));
      row.put("file", Optional.ofNullable(node)
          .map(n -> Optional.ofNullable(n.getLocation()).map(l -> l.getFile()).orElse("")).orElse(""));
      row.put("startLine",
          node != null && node.getLocation() != null ? String.valueOf(node.getLocation().getBeginLine()) : "");
      sb.append(buildCsvRow(row));
    }

    return sb.toString();
  }

  private static String buildCsvRow(Map<String, String> rowMap) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < HEADERS.size(); i++) {
      if (i > 0) {
        sb.append(',');
      }
      String key = HEADERS.get(i);
      String v = rowMap.getOrDefault(key, "");
      sb.append(csvQuoted(v));
    }
    sb.append('\n');
    return sb.toString();
  }

  private static String csvQuoted(Object o) {
    if (o == null)
      return "";
    String s = o.toString();
    s = s.replace("\"", "\"\"").replace("\n", "\\n").replace("\r", "\\r");
    return "\"" + s + "\"";
  }
}
