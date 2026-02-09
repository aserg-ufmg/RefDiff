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

public class TestJavaPlugin {
    private static final LanguagePlugin parser = new JavaPlugin();
    private static final String TEST_DATA_BASE_PATH = "src/test/resources/java/syntax";

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

    // @Test
    public void shouldParseClassDeclarationsCorrectly() throws Exception {
        Path baseFolderPath = Paths.get(TEST_DATA_BASE_PATH + "/class");
        SourceFileSet sources = SourceFolder.from(baseFolderPath, ".java");
        CstRoot cstRoot = parser.parse(sources);

        List<CstNode> classNodes = new ArrayList<>();
        cstRoot.forEachNode((node, depth) -> {
            if (JavaNodeTypes.CLASS.equals(node.getType())) {
                classNodes.add(node);
            }
        });

        List<ExpectedNode> expectedNodes = Arrays.asList(
            new ExpectedNode("BasicPublicClass", JavaNodeTypes.CLASS, 23, "BasicPublicClass", "com.example.parser.test.", "BasicPublicClass.java"),
            new ExpectedNode("PackagePrivateClass", JavaNodeTypes.CLASS, 46, "PackagePrivateClass", "com.example.parser.test.", "BasicPublicClass.java"),
            new ExpectedNode("AbstractVehicle", JavaNodeTypes.CLASS, 58, "AbstractVehicle", "com.example.parser.test.", "BasicPublicClass.java"),
            new ExpectedNode("FinalImmutableData", JavaNodeTypes.CLASS, 69, "FinalImmutableData", "com.example.parser.test.", "BasicPublicClass.java"),
            new ExpectedNode("Car", JavaNodeTypes.CLASS, 88, "Car", "com.example.parser.test.", "BasicPublicClass.java"),
            new ExpectedNode("MultiImplementer", JavaNodeTypes.CLASS, 98, "MultiImplementer", "com.example.parser.test.", "BasicPublicClass.java"),
            new ExpectedNode("ComplexHierarchy", JavaNodeTypes.CLASS, 113, "ComplexHierarchy", "com.example.parser.test.", "BasicPublicClass.java"),
            new ExpectedNode("Box", JavaNodeTypes.CLASS, 131, "Box", "com.example.parser.test.", "BasicPublicClass.java"),
            new ExpectedNode("BoundedGenericCache", JavaNodeTypes.CLASS, 149, "BoundedGenericCache", "com.example.parser.test.", "BasicPublicClass.java"),
            new ExpectedNode("BoundedGenericCache", JavaNodeTypes.CLASS, 149, "BoundedGenericCache", "com.example.parser.test.", "BasicPublicClass.java"),
            new ExpectedNode("NumberProcessor", JavaNodeTypes.CLASS, 162, "NumberProcessor", "com.example.parser.test.", "BasicPublicClass.java"),
            new ExpectedNode("OuterShell", JavaNodeTypes.CLASS, 177, "OuterShell", "com.example.parser.test.", "BasicPublicClass.java"),
            new ExpectedNode("StaticNested", JavaNodeTypes.CLASS, 185, "StaticNested", null, "BasicPublicClass.java"),
            new ExpectedNode("Inner", JavaNodeTypes.CLASS, 196, "Inner", null, "BasicPublicClass.java"),
            new ExpectedNode("MethodLocalRunnable", JavaNodeTypes.CLASS, 210, "MethodLocalRunnable", null, "BasicPublicClass.java"),
            new ExpectedNode("AnnotatedClass", JavaNodeTypes.CLASS, 311, "AnnotatedClass", "com.example.parser.test.", "BasicPublicClass.java"),
            new ExpectedNode("EmptyClass", JavaNodeTypes.CLASS, 329, "EmptyClass", "com.example.parser.test.", "BasicPublicClass.java"),
            new ExpectedNode("GenericsSubclass", JavaNodeTypes.CLASS, 340, "GenericsSubclass", "com.example.parser.test.", "BasicPublicClass.java")
        );

        for (ExpectedNode expected : expectedNodes) {
            CstNode actualNode = findNode(classNodes, expected.name(), expected.line());
            assertThat(actualNode.getType(), is(equalTo(expected.type())));
            assertThat(actualNode.getSimpleName(), is(equalTo(expected.name())));
            assertThat(actualNode.getLocalName(), is(equalTo(expected.localName())));
            assertThat(actualNode.getNamespace(), is(equalTo(expected.namespace())));
            Location location = actualNode.getLocation();
            assertThat(location.getFile(), is(equalTo(expected.fileName())));
            assertThat(location.getBeginLine(), is(equalTo(expected.line())));
            assertTrue(actualNode.getParameters().isEmpty());
        }
    }

    @Test
    public void shouldParseMethodDeclarationsCorrectly() throws Exception {
        Path baseFolderPath = Paths.get(TEST_DATA_BASE_PATH + "/method");
        SourceFileSet sources = SourceFolder.from(baseFolderPath, ".java");
        CstRoot cstRoot = parser.parse(sources);

        List<CstNode> nodes = new ArrayList<>();
        cstRoot.forEachNode((node, depth) -> {
            nodes.add(node);
        });

        List<ExpectedNode> expectedNodes = Arrays.asList(
            new ExpectedNode("checkArgument", JavaNodeTypes.METHOD, 7, "checkArgument(boolean, String)",null, "Precondition.java")
        );

        for (ExpectedNode expected : expectedNodes) {
            CstNode actualNode = findNode(nodes, expected.name(), expected.line());
            assertThat(actualNode.getType(), is(equalTo(expected.type())));
            assertThat(actualNode.getSimpleName(), is(equalTo(expected.name())));
            assertThat(actualNode.getLocalName(), is(equalTo(expected.localName())));
            assertThat(actualNode.getNamespace(), is(equalTo(expected.namespace())));
            Location location = actualNode.getLocation();
            assertThat(location.getFile(), is(equalTo(expected.fileName())));
            assertThat(location.getBeginLine(), is(equalTo(expected.line())));
        }
    }

}