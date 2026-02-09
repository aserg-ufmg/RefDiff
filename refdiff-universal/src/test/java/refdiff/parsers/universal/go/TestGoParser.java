package refdiff.parsers.universal.go;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstRoot;
import refdiff.core.cst.Location;
import refdiff.core.cst.Parameter;
import refdiff.core.io.SourceFileSet;
import refdiff.core.io.SourceFolder;
import refdiff.parsers.LanguagePlugin;

public class TestGoParser {
    private static final LanguagePlugin parser = new GoParser();
    private static final String TEST_DATA_BASE_PATH = "src/test/resources/go/syntax";

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
    public void shouldParseFunctionsCorrectly() throws Exception {
        Path baseFolderPath = Paths.get(TEST_DATA_BASE_PATH);
        SourceFileSet sources = SourceFolder.from(baseFolderPath, ".go");
        CstRoot cstRoot = parser.parse(sources);

        List<CstNode> functionNodes = cstRoot.getNodes().stream()
            .filter(node -> node.getType().equals(GoNodeTypes.FUNCTION) || node.getType().equals(GoNodeTypes.METHOD))
            .collect(Collectors.toList());

        List<ExpectedNode> expectedNodes = Arrays.asList(
            new ExpectedNode("MethodWithValueReceiver", GoNodeTypes.METHOD, 17, "MethodWithValueReceiver()", "main.", "sample.go", List.of()),
            new ExpectedNode("CustomIntMethod", GoNodeTypes.METHOD, 22, "CustomIntMethod()", "main.", "sample.go", List.of()),
            new ExpectedNode("MethodWithPointerReceiver", GoNodeTypes.METHOD, 29, "MethodWithPointerReceiver()", "main.", "sample.go", List.of()),
            new ExpectedNode("MethodWithVariadicArgs", GoNodeTypes.METHOD, 38, "MethodWithVariadicArgs(numbers ...int)", "main.", "sample.go", List.of("numbers")),
            new ExpectedNode("MethodWithMultipleArguments", GoNodeTypes.METHOD, 50, "MethodWithMultipleArguments(a, b int)", "main.", "sample.go", List.of("a", "b")),
            new ExpectedNode("MethodWithReturnValue", GoNodeTypes.METHOD, 55, "MethodWithReturnValue()", "main.", "sample.go", List.of()),
            new ExpectedNode("MethodWithNakedReturns", GoNodeTypes.METHOD, 60, "MethodWithNakedReturns(s string)", "main.", "sample.go", List.of("s")),
            new ExpectedNode("main", GoNodeTypes.FUNCTION, 65, "main()", "main.", "sample.go", List.of()),
            new ExpectedNode("simpleFunction", GoNodeTypes.FUNCTION, 89, "simpleFunction()", "main.", "sample.go", List.of()),
            new ExpectedNode("functionWithParameters", GoNodeTypes.FUNCTION, 94, "functionWithParameters(x int, y string)", "main.", "sample.go", List.of("x", "y")),
            new ExpectedNode("functionWithMultipleSameTypeParameters", GoNodeTypes.FUNCTION, 99, "functionWithMultipleSameTypeParameters(a, b int, c, d float64)", "main.", "sample.go", List.of("a", "b", "c", "d")),
            new ExpectedNode("functionWithReturnValue", GoNodeTypes.FUNCTION, 104, "functionWithReturnValue()", "main.", "sample.go", List.of()),
            new ExpectedNode("functionWithBoth", GoNodeTypes.FUNCTION, 109, "functionWithBoth(x, y int)", "main.", "sample.go", List.of("x", "y")),
            new ExpectedNode("functionWithMultipleReturnValues", GoNodeTypes.FUNCTION, 114, "functionWithMultipleReturnValues(a, b int)", "main.", "sample.go", List.of("a", "b")),
            new ExpectedNode("functionWithNamedReturnValues", GoNodeTypes.FUNCTION, 122, "functionWithNamedReturnValues(a int, b int)", "main.", "sample.go", List.of("a", "b")),
            new ExpectedNode("functionWithVariadicParameters", GoNodeTypes.FUNCTION, 129, "functionWithVariadicParameters(s string, nums ...int)", "main.", "sample.go", List.of("s", "nums"))
        );

        for (ExpectedNode expected : expectedNodes) {
            CstNode actualNode = findNode(functionNodes, expected.name(), expected.line());
            assertThat(actualNode.getType(), is(equalTo(expected.type())));
            assertThat(actualNode.getSimpleName(), is(equalTo(expected.name())));
            assertThat(actualNode.getLocalName(), is(equalTo(expected.localName())));
            assertThat(actualNode.getNamespace(), is(equalTo(expected.namespace())));
            Location location = actualNode.getLocation();
            assertThat(location.getFile(), is(equalTo(expected.fileName())));
            assertThat(location.getBeginLine(), is(equalTo(expected.line())));
            List<String> actualParamNames = actualNode.getParameters().stream()
                .map(Parameter::getName)
                .collect(Collectors.toList());
            assertThat(actualParamNames, is(equalTo(expected.params())));
        }
    }
}
