package plugingenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

public class PluginGenerator {
  public static void main(String[] args) throws Exception {
    String pluginDir = System.getProperty("plugin.dir");
    if (pluginDir == null || pluginDir.isEmpty()) {
      throw new IllegalArgumentException("Path to the plugin directory must be provided via the 'plugin.dir' system property.");
    }

    String langArg = null;
    if (args != null && args.length > 0 && args[0] != null && !args[0].isEmpty()) {
      langArg = args[0];
    }
    if (langArg == null) {
      throw new IllegalArgumentException("Language argument is required");
    }
    Language language = Language.fromName(langArg);

    Path srcDir = Paths.get("context/" + language.getName() + "/src/");
    List<Path> sourceFiles = Files.list(srcDir)
      .filter(Files::isRegularFile)
      .filter(p -> Arrays.stream(language.getExtensions()).anyMatch(ext -> p.toString().endsWith(ext)))
      .collect(Collectors.toList());
    
    Path tagsDir = Paths.get(pluginDir + "/src/test/resources/" + language.getName() + "/tags/");
    if (!Files.exists(tagsDir)) {
      Files.createDirectories(tagsDir);
    }
    for (Path sourceFile : sourceFiles) {
      String sourceFileName = sourceFile.getFileName().toString();
      String ctagsOutput = CtagsExecutor.exec(
        srcDir.toFile(),
        language.getCtagsOption(),
        sourceFileName
      );
      Path ctagsOutputFile = tagsDir.resolve(sourceFileName + ".ndjson");
      Files.writeString(ctagsOutputFile, ctagsOutput);

      TestGenerator.generate(ctagsOutput);
    }

    Path astDir = Paths.get("context/" + language.getName() + "/ast/");
    if (!Files.exists(astDir)) {
      Files.createDirectories(astDir);
    }
    for (Path sourceFile : sourceFiles) {
      String astContent = TreeSitterParser.parse(sourceFile, language.getTSLanguage());
      String sourceFileName = sourceFile.getFileName().toString();
      String baseName = sourceFileName.substring(0, sourceFileName.lastIndexOf('.'));
      Path astOutputFile = astDir.resolve(baseName + ".ast.txt");
      Files.writeString(astOutputFile, astContent);
    }
  }
}
