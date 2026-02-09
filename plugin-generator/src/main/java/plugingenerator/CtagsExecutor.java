package plugingenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class CtagsExecutor {
  
  /**
   * Executes Ctags and returns the output as a string.
   *
   * @param workingDir The directory where the command will be executed
   * @param languageOption The Ctags option for the specific language
   * @param sourceFileName The name of the source file to process
   * @return The output of the Ctags command as a string that is NDJSON formatted
   * @throws IOException If the process fails to start
   * @throws InterruptedException If the process is interrupted
   * @throws RuntimeException If the command exits with a non-zero code
   */
  public static String exec(File workingDir, String languageOption, String sourceFileName)
      throws IOException, InterruptedException {
    ProcessBuilder processBuilder = new ProcessBuilder(
        "ctags",
        "--output-format=json",
        "--pseudo-tags",
        "--sort=no",
        "--fields=+neb",
        languageOption,
        sourceFileName
    );
    processBuilder.directory(workingDir);
    Process process = processBuilder.start();
    StringBuilder output = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }
    }

    int exitCode = process.waitFor();
    if (exitCode != 0) {
      StringBuilder errorOutput = new StringBuilder();
      try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
        String line;
        while ((line = errorReader.readLine()) != null) {
          errorOutput.append(line).append("\n");
        }
      }
      throw new RuntimeException("Ctags execution failed with exit code " + exitCode + ". Error: " + errorOutput.toString());
    }
    return output.toString();
  }
}
