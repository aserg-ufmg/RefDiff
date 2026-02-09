package validation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Repository {

	private static final String OUTDIR = "./repo";
	private static final String DEPTH = "1";

  /**
   * Get the local path of a repository, cloning it if necessary.
   */
  public static Path get(String repoUrl) {
    String name = extractName(repoUrl);

    Path path = Paths.get(OUTDIR).resolve(name);
    if (!Files.exists(path)) {
      try {
        cloneShallow(repoUrl);
      } catch (Exception e) {
        throw new RuntimeException("Failed to clone repository: " + repoUrl, e);
      }
    }
    return path;
  }

	private static void cloneShallow(String url) throws IOException, InterruptedException {
		Path outPath = Paths.get(OUTDIR);
		Files.createDirectories(outPath);

    String name = extractName(url);
    Path target = outPath.resolve(name);
    runGit("clone", "--depth", DEPTH, url, target.toString());
	}

  public static String generateRemoteUrl(String repoUrl, String branch, String file, int lineNumber) {
    String baseUrl = repoUrl;
    if (baseUrl.endsWith(".git")) {
      baseUrl = baseUrl.substring(0, baseUrl.length() - 4);
    }
    return String.format("%s/blob/%s/%s#L%d", baseUrl, branch, file, lineNumber);
  }

	public static String detectDefaultBranch(String url) {
		try {
			ProcessBuilder pb = new ProcessBuilder("git", "ls-remote", "--symref", url, "HEAD");
			pb.redirectErrorStream(true);
			Process p = pb.start();
			String out;
			try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				out = r.lines().collect(Collectors.joining("\n"));
			}
			p.waitFor();
			for (String line : out.split("\n")) {
				line = line.trim();
				if (line.startsWith("ref:")) {
					String[] parts = line.split("\\s+");
					if (parts.length >= 2) {
						String ref = parts[1];
						if (ref.startsWith("refs/heads/")) {
							return ref.substring("refs/heads/".length());
						}
					}
				}
			}
		} catch (Exception e) {
			// ignore and fall through
		}
		return null;
	}

	private static void runGit(String... args) throws IOException, InterruptedException {
		List<String> cmd = new ArrayList<>();
		cmd.add("git");
		cmd.addAll(Arrays.asList(args));
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.inheritIO();
		Process p = pb.start();
		int rc = p.waitFor();
		if (rc != 0) {
			throw new IOException("git command failed with exit code " + rc);
		}
	}

  private static String extractName(String repoUrl) {
    String name = repoUrl.endsWith(".git") ? repoUrl.substring(0, repoUrl.length() - 4) : repoUrl;
    int idx = name.lastIndexOf('/');
    if (idx >= 0 && idx < name.length() - 1) {
      name = name.substring(idx + 1);
    }
    return name;
  }
}
