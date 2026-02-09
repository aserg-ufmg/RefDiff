package refdiff.parsers.universal.js;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import static org.junit.jupiter.api.Assertions.assertAll;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;


import org.junit.jupiter.api.Test;
import refdiff.core.cst.Location;
import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstRoot;
import refdiff.core.io.SourceFileSet;
import refdiff.core.cst.Parameter;
import refdiff.core.io.SourceFolder;
import refdiff.parsers.LanguagePlugin;

public class TestJsPlugin {
  private static final LanguagePlugin parser = new JsPlugin();
  private static final String TEST_DATA_BASE_PATH = "src/test/resources/js/syntax";

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
      String localName,
      String namespace,
      String fileName,
      List<String> params
  ) {
      ExpectedNode(String name, String type, int line, String localName, String namespace, String fileName) {
          this(name, type, line, localName, namespace, fileName, List.of());
      }
  }
  
  @Test
  public void shouldParseFileNodeCorrectly() throws Exception {
    Path fileTestPath = Paths.get(TEST_DATA_BASE_PATH, "file");
    SourceFileSet sources = SourceFolder.from(fileTestPath, ".js");
    CstRoot cstRoot = parser.parse(sources);

    List<CstNode> fileNodes = cstRoot.getNodes().stream()
        .filter(node -> JsNodeTypes.FILE.equals(node.getType()))
        .collect(Collectors.toList());

    assertThat("Should find 1 file node", fileNodes.size(), is(equalTo(1)));

    List<ExpectedNode> expectedNodes = Arrays.asList(
        new ExpectedNode("file.js", JsNodeTypes.FILE, 1, "file.js", "dir/", "dir/file.js")
    );
    assertAll(expectedNodes.stream().map(expected -> () -> {
      CstNode actualNode = findNode(fileNodes, expected.name(), expected.line());
      assertThat(actualNode.getType(), is(equalTo(expected.type())));
      assertThat(actualNode.getSimpleName(), is(equalTo(expected.name())));
      assertThat(actualNode.getLocalName(), is(equalTo(expected.localName())));
      assertThat(actualNode.getNamespace(), is(equalTo(expected.namespace())));
      Location location = actualNode.getLocation();
      assertThat(location.getFile(), is(equalTo(expected.fileName())));
      assertThat(location.getBeginLine(), is(equalTo(expected.line())));
    }));
  }

  @Test
  public void shouldParseClassDeclarations() throws Exception {
    Path baseFolderPath = Paths.get(TEST_DATA_BASE_PATH, "class");
    SourceFileSet sources = SourceFolder.from(baseFolderPath, ".js");
    CstRoot cstRoot = parser.parse(sources);

    List<CstNode> classNodes = new ArrayList<>();
    cstRoot.forEachNode((node, depth) -> {
      if (JsNodeTypes.CLASS.equals(node.getType())) {
        classNodes.add(node);
      }
    });

    List<ExpectedNode> expectedNodes = Arrays.asList(
        new ExpectedNode("Animal", JsNodeTypes.CLASS, 2, "Animal", null, "dir/class.js"),
        new ExpectedNode("Dog", JsNodeTypes.CLASS, 65, "Dog", null, "dir/class.js")
    );

    assertAll(expectedNodes.stream().map(expected -> () -> {
      CstNode actualNode = findNode(classNodes, expected.name(), expected.line());
      assertThat(actualNode.getType(), is(equalTo(expected.type())));
      assertThat(actualNode.getSimpleName(), is(equalTo(expected.name())));
      assertThat(actualNode.getLocalName(), is(equalTo(expected.localName())));
      assertThat(actualNode.getNamespace(), is(equalTo(expected.namespace())));
      Location location = actualNode.getLocation();
      assertThat(location.getFile(), is(equalTo(expected.fileName())));
      assertThat(location.getBeginLine(), is(equalTo(expected.line())));
    }));
  }

  @Test
  public void shouldParseFunctionDeclarations() throws Exception {
    Path baseFolderPath = Paths.get(TEST_DATA_BASE_PATH, "function");
    SourceFileSet sources = SourceFolder.from(baseFolderPath, ".js");
    CstRoot cstRoot = parser.parse(sources);

    List<CstNode> actualFunctionNodes = new ArrayList<>();
    cstRoot.forEachNode((node, depth) -> {
      if (JsNodeTypes.FUNCTION.equals(node.getType())) {
        actualFunctionNodes.add(node);
      }
    });

    List<ExpectedNode> expectedNodes = Arrays.asList(
        new ExpectedNode("classicFunction", JsNodeTypes.FUNCTION, 2, "classicFunction(param1, param2)", null, "function.js", List.of("param1", "param2")),
        new ExpectedNode("anonymousFunction", JsNodeTypes.FUNCTION, 10, "anonymousFunction(a, b)", null, "function.js", List.of("a", "b")),
        new ExpectedNode("arrowFunctionSimple", JsNodeTypes.FUNCTION, 16, "arrowFunctionSimple(x, y)", null, "function.js", List.of("x", "y")),
        new ExpectedNode("arrowFunctionSingleParam", JsNodeTypes.FUNCTION, 19, "arrowFunctionSingleParam(param)", null, "function.js", List.of("param")),
        new ExpectedNode("arrowFunctionNoParam", JsNodeTypes.FUNCTION, 22, "arrowFunctionNoParam()", null, "function.js", List.of()),
        new ExpectedNode("arrowFunctionBlockBody", JsNodeTypes.FUNCTION, 25, "arrowFunctionBlockBody(val1, val2)", null, "function.js", List.of("val1", "val2")),
        new ExpectedNode("higherOrderFunction", JsNodeTypes.FUNCTION, 32, "higherOrderFunction(callback)", null, "function.js", List.of("callback")),
        new ExpectedNode("outerFunction", JsNodeTypes.FUNCTION, 39, "outerFunction(outerVar)", null, "function.js", List.of("outerVar")),
        new ExpectedNode("innerFunction", JsNodeTypes.FUNCTION, 41, "innerFunction(innerParam)", null, "function.js", List.of("innerParam")),
        new ExpectedNode("processArguments", JsNodeTypes.FUNCTION, 50, "processArguments(firstArg, ...restArgs)", null, "function.js", List.of("firstArg", "...restArgs")),
        new ExpectedNode("greet", JsNodeTypes.FUNCTION, 58, "greet(name)", null, "function.js", List.of("name")),
        new ExpectedNode("performAsyncOperation", JsNodeTypes.FUNCTION, 65, "performAsyncOperation(success)", null, "function.js", List.of("success")),
        new ExpectedNode("idGenerator", JsNodeTypes.FUNCTION, 91, "idGenerator()", null, "function.js", List.of()),
        new ExpectedNode("fibonacciSequence", JsNodeTypes.FUNCTION, 101, "fibonacciSequence()", null, "function.js", List.of()),
        new ExpectedNode("functionWithErrorHandling", JsNodeTypes.FUNCTION, 114, "functionWithErrorHandling(num)", null, "function.js", List.of("num")),
        new ExpectedNode("functionWithIIFE", JsNodeTypes.FUNCTION, 132, "functionWithIIFE()", null, "function.js", List.of()),
        new ExpectedNode("labeledLoopFunction", JsNodeTypes.FUNCTION, 145, "labeledLoopFunction()", null, "function.js", List.of())
    );

    assertAll(expectedNodes.stream().map(expected -> () -> {
      CstNode actualNode = findNode(actualFunctionNodes, expected.name(), expected.line());
      assertThat(actualNode.getType(), is(equalTo(expected.type())));
      assertThat(actualNode.getSimpleName(), is(equalTo(expected.name())));
      assertThat(actualNode.getLocalName(), is(equalTo(expected.localName())));
      if (expected.namespace() != null) {
          assertThat(actualNode.getNamespace(), is(equalTo(expected.namespace())));
      }
      Location location = actualNode.getLocation();
      assertThat(location.getFile(), is(equalTo(expected.fileName())));
      assertThat(location.getBeginLine(), is(equalTo(expected.line())));
      List<String> actualParamNames = actualNode.getParameters().stream()
          .map(Parameter::getName)
          .collect(Collectors.toList());
      assertThat(actualParamNames, is(equalTo(expected.params())));
    }));
  }
}
