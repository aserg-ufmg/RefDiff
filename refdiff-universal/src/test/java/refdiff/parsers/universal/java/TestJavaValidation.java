package refdiff.parsers.universal.java;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstRoot;
import refdiff.core.cst.Location;
import refdiff.core.cst.Parameter;
import refdiff.core.io.SourceFileSet;
import refdiff.core.io.SourceFolder;
import refdiff.parsers.LanguagePlugin;

public class TestJavaValidation {
  private static final LanguagePlugin parser = new JavaPlugin();
  private static final String TEST_DATA_BASE_PATH = "src/test/resources/java/syntax/";

  private CstNode findNode(List<CstNode> nodes, String name, int line) {
    return nodes.stream()
        .filter(node -> name.equals(node.getSimpleName()) && node.getLocation().getBeginLine() == line)
        .findFirst()
        .orElseThrow(() -> new AssertionError("Node with name '" + name + "' at line " + line + " not found."));
  }

  private record ExpectedNode(
      String name,
      String type,
      int line,
      String fileName,
      List<String> params) {
    ExpectedNode(String name, String type, int line, String fileName) {
      this(name, type, line, fileName, List.of());
    }
  }

  public void shouldParseClassDeclarationsCorrectly() throws Exception {
    Path baseFolderPath = Paths.get(TEST_DATA_BASE_PATH + "/validation");
    SourceFileSet sources = SourceFolder.from(baseFolderPath, ".java");
    CstRoot cstRoot = parser.parse(sources);

    List<CstNode> classNodes = new ArrayList<>();
    cstRoot.forEachNode((node, depth) -> {
      if (JavaNodeTypes.CLASS.equals(node.getType())) {
        classNodes.add(node);
      }
    });

    List<ExpectedNode> expectedNodes = Arrays.asList(
        new ExpectedNode("BasicPublicClass", JavaNodeTypes.CLASS, 23, "BasicPublicClass.java"),
        new ExpectedNode("value", JavaNodeTypes.METHOD, 16, "BasicPublicClass.java"),
        new ExpectedNode("message", JavaNodeTypes.METHOD, 18, "BasicPublicClass.java"),
        new ExpectedNode("groups", JavaNodeTypes.METHOD, 20, "BasicPublicClass.java"),
        new ExpectedNode("payload", JavaNodeTypes.METHOD, 22, "BasicPublicClass.java")
    );

    for (ExpectedNode expected : expectedNodes) {
      CstNode actualNode = findNode(classNodes, expected.name(), expected.line());
      assertThat(actualNode.getType(), is(equalTo(expected.type())));
      assertThat(actualNode.getSimpleName(), is(equalTo(expected.name())));
      Location location = actualNode.getLocation();
      assertThat(location.getFile(), is(equalTo(expected.fileName())));
      assertThat(location.getBeginLine(), is(equalTo(expected.line())));
    }
  }
}