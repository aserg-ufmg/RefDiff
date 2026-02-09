package plugingenerator;

import java.nio.file.Files;
import java.nio.file.Path;

import org.treesitter.*;

public class TreeSitterParser {
  /**
   * Parses the specified source file using TreeSitter and returns the AST as a string.
   *
   * @param sourceFile The path to the source file to be parsed
   * @param tsLang The TreeSitter language definition
   * @return The AST content as a string
   * @throws Exception If an error occurs during parsing
   */
  public static String parse(Path sourceFile, TSLanguage tsLang) throws Exception {
    TSParser parser = new TSParser();
    parser.setLanguage(tsLang);

    String sourceCode = Files.readString(sourceFile);
    TSTree tree = parser.parseString(null, sourceCode);
    String query = "_ @node";
    TSQuery tsQuery = new TSQuery(tsLang, query);
    TSNode rootNode = tree.getRootNode();
    TSQueryCursor cursor = new TSQueryCursor();
    cursor.exec(tsQuery, rootNode);
    TSQueryMatch match = new TSQueryMatch();
    StringBuilder astContent = new StringBuilder();
    while (cursor.nextMatch(match)) {
      TSQueryCapture[] captures = match.getCaptures();
      for (TSQueryCapture capture : captures) {
        TSNode node = capture.getNode();
        astContent.append(node.toString()).append(" L:").append(node.getStartPoint().getRow() + 1).append("\n");
      }
    }
    return astContent.toString();
  }
}
