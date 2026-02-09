// package refdiff.parsers.universal.ts;

// import static org.hamcrest.CoreMatchers.equalTo;
// import static org.hamcrest.CoreMatchers.is;
// import static org.junit.Assert.assertThat;

// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.util.List;
// import java.util.Arrays;
// import java.util.stream.Collectors;

// import org.junit.jupiter.api.Test;

// import refdiff.core.cst.CstNode;
// import refdiff.core.cst.CstRoot;
// import refdiff.core.cst.Location;
// import refdiff.core.cst.Parameter;
// import refdiff.core.io.SourceFileSet;
// import refdiff.core.io.SourceFolder;
// import refdiff.parsers.LanguagePlugin;

// public class TestTsParser {
// 	private static final LanguagePlugin parser = new TsParser();
// 	private static final String TEST_DATA_BASE_PATH = "src/test/resources/ts/syntax";

// 	private CstNode findMethod(List<CstNode> nodes, String name, int line) {
// 		return nodes.stream()
// 				.filter(node -> name.equals(node.getSimpleName()) && node.getLocation().getBeginLine() == line)
// 				.findFirst()
// 				.orElseThrow(() -> new AssertionError("Method with name '" + name + "' at line " + line + " not found."));
// 	}

// 	private record ExpectedMethod(
// 			String simpleName,
// 			String type,
// 			int line,
// 			String localName,
// 			String namespace,
// 			String fileName,
// 			List<String> params
// 		) {
// 	}

// 	@Test
// 	public void shouldParseMethodDefinitionsCorrectly() throws Exception {
// 		Path baseFolderPath = Paths.get(TEST_DATA_BASE_PATH);
// 		SourceFileSet sources = SourceFolder.from(baseFolderPath, ".ts");
// 		CstRoot cstRoot = parser.parse(sources);

// 		List<CstNode> methodNodes = cstRoot.getNodes().stream()
// 				.filter(node -> TsNodeTypes.FUNCTION.equals(node.getType()))
// 				.collect(Collectors.toList());

// 		List<ExpectedMethod> expectedMethodsData = Arrays.asList(
// 			new ExpectedMethod("basicDeclaration", TsNodeTypes.FUNCTION, 15, null, null, "func.ts", List.of()),
// 			new ExpectedMethod("logMessage", TsNodeTypes.FUNCTION, 23, null, null, "func.ts", List.of()),
// 			new ExpectedMethod("throwError", TsNodeTypes.FUNCTION, 31, null, null, "func.ts", List.of()),
// 			new ExpectedMethod("greet", TsNodeTypes.FUNCTION, 91, null, null, "func.ts", List.of()),
// 			new ExpectedMethod("createUser", TsNodeTypes.FUNCTION, 99, null, null, "func.ts", List.of()),
// 			new ExpectedMethod("sumAll", TsNodeTypes.FUNCTION, 107, null, null, "func.ts", List.of()),
// 			new ExpectedMethod("renderConfig", TsNodeTypes.FUNCTION, 115, null, null, "func.ts", List.of()),
// 			new ExpectedMethod("identity", TsNodeTypes.FUNCTION, 127, null, null, "func.ts", List.of()),
// 			new ExpectedMethod("logLength", TsNodeTypes.FUNCTION, 141, null, null, "func.ts", List.of()),
// 			// new ExpectedMethod("getTimestamp", TsNodeTypes.FUNCTION, 155, null, null, "func.ts", List.of()),
// 			// new ExpectedMethod("fetchData", TsNodeTypes.FUNCTION, 172, null, null, "func.ts", List.of()),
// 			new ExpectedMethod("isFish", TsNodeTypes.FUNCTION, 205, null, null, "func.ts", List.of()),
// 			new ExpectedMethod("assertIsString", TsNodeTypes.FUNCTION, 213, null, null, "func.ts", List.of()),
// 			new ExpectedMethod("handleEvent", TsNodeTypes.FUNCTION, 288, null, null, "func.ts", List.of())
// 		);

// 		for (ExpectedMethod expected : expectedMethodsData) {
// 			CstNode actualNode = findMethod(methodNodes, expected.simpleName(), expected.line());

// 			assertThat("Type for " + expected.simpleName(), actualNode.getType(), is(equalTo(expected.type())));
// 			assertThat("SimpleName for " + expected.simpleName(), actualNode.getSimpleName(), is(equalTo(expected.simpleName())));
// 			// assertThat("LocalName for " + expected.name(), actualNode.getLocalName(), is(equalTo(expected.localName())));

// 			Location actualLocation = actualNode.getLocation();
// 			assertThat("Location file for " + expected.simpleName(), actualLocation.getFile(), is(equalTo("func.ts")));
// 			assertThat("Location line for " + expected.simpleName(), actualLocation.getBeginLine(), is(equalTo(expected.line())));

// 			// List<String> actualParamNames = actualNode.getParameters().stream()
// 			// 		.map(Parameter::getName)
// 			// 		.collect(Collectors.toList());
// 			// assertThat("Parameters for " + expected.name(), actualParamNames, is(equalTo(expected.params())));
// 		}
// 	}
// }
