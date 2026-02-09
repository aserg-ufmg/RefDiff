package validation;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import plugingenerator.CtagsExecutor;
import plugingenerator.Language;
import refdiff.core.io.FilePathFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Data holder for a ctags NDJSON entry and helpers to parse NDJSON files.
 */
public class Tag {
	@SerializedName("_type")
	private String type;
	private String name;
	private String path;
	private String pattern;
	private Integer line;
	private String kind;
	private String scope;
	private String scopeKind;
	private Integer end;

	/* The line number within the NDJSON file where this tag entry appears (1-based). */
	private Integer ndjsonLine;

	public String getType() { return type; }
	public String getName() { return name; }
	public String getPath() { return path; }
	public String getPattern() { return pattern; }
	public Integer getLine() { return line; }
	public String getKind() { return kind; }
	public String getScope() { return scope; }
	public String getScopeKind() { return scopeKind; }
	public Integer getEnd() { return end; }
	public Integer getNdjsonLine() { return ndjsonLine; }

	@Override
	public String toString() {
		return "Tag[name=" + name + ", kind=" + kind + ", path=" + path + ", line=" + line + ", ndjsonLine=" + ndjsonLine + "]";
	}

	public static List<Tag> parseJson(Path tagsFilePath) throws IOException {
		Gson gson = new Gson();
		List<Tag> tags = new ArrayList<>();
		AtomicInteger counter = new AtomicInteger(1);
		try (Stream<String> lines = Files.lines(tagsFilePath)) {
			lines.map(String::trim)
				.filter(s -> !s.isEmpty())
				.forEach(line -> {
					Tag t = gson.fromJson(line, Tag.class);
					t.ndjsonLine = counter.getAndIncrement(); // record the NDJSON line number (1-based)
					tags.add(t);
				});
		}
		return tags;
	}

	public static String execCtags(Path repoPath, Language language, FilePathFilter fileFilter) {
    StringBuilder ctagsOutput = new StringBuilder();
    try {
      Files.walk(repoPath)
      .filter(Files::isRegularFile)
      .filter(p -> fileFilter.isAllowed(p.toString()))
      .forEach(p -> {
        try {
          String relative = repoPath.relativize(p).toString();
          String ctagsOutputPart = CtagsExecutor.exec(repoPath.toFile(), language.getCtagsOption(), relative);
          ctagsOutput.append(ctagsOutputPart).append(System.lineSeparator());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    } catch (IOException e) {
      System.out.println("Error walking repository files: " + e.getMessage());
      e.printStackTrace();
      return "";
    }
    return ctagsOutput.toString();
  }
}
