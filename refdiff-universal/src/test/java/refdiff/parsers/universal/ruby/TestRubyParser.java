package refdiff.parsers.universal.ruby;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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

public class TestRubyParser {
    private static final LanguagePlugin parser = new RubyParser();
    private static final String TEST_DATA_BASE_PATH = "src/test/resources/ruby/syntax";

    private CstNode findMethod(List<CstNode> nodes, String name, int line) {
        return nodes.stream()
            .filter(node -> name.equals(node.getSimpleName()) && node.getLocation().getBeginLine() == line)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Method with name '" + name + "' at line " + line + " not found."));
    }

    private record ExpectedMethod(
        String name,
        String type,
        int line,
        String localName,
        String namespace,
        String fileName,
        List<String> params
    ) {}

    @Test
    public void shouldParseMethodDefinitionsCorrectly() throws Exception {
        Path baseFolderPath = Paths.get(TEST_DATA_BASE_PATH);
        SourceFileSet sources = SourceFolder.from(baseFolderPath, ".rb");
        CstRoot cstRoot = parser.parse(sources);

        List<CstNode> methodNodes = cstRoot.getNodes().stream()
            .filter(node -> RubyNodeTypes.METHOD.equals(node.getType()))
            .collect(Collectors.toList());

        // Expected methods based on the query for `def` and `singleton_method`
        // Dynamic methods (define_method) and accessors (attr_*) are not included.
        assertThat("Should find 27 method nodes", methodNodes.size(), is(equalTo(27)));

        List<ExpectedMethod> expectedMethodsData = Arrays.asList(
            new ExpectedMethod("instance_method_example", RubyNodeTypes.METHOD, 7, "instance_method_example()", null, "method.rb", List.of()),
            new ExpectedMethod("class_method_example", RubyNodeTypes.METHOD, 12, "class_method_example()", null, "method.rb", List.of()),
            new ExpectedMethod("another_class_method_example", RubyNodeTypes.METHOD, 21, "another_class_method_example()", null, "method.rb", List.of()),
            new ExpectedMethod("greet", RubyNodeTypes.METHOD, 40, "greet()", null, "method.rb", List.of()),
            new ExpectedMethod("class_specific_method", RubyNodeTypes.METHOD, 52, "class_specific_method()", null, "method.rb", List.of()),
            new ExpectedMethod("initialize", RubyNodeTypes.METHOD, 96, "initialize(name, email, age)", null, "method.rb", List.of("name", "email", "age")), // User's initialize
            new ExpectedMethod("display_age", RubyNodeTypes.METHOD, 102, "display_age()", null, "method.rb",  List.of()),
            new ExpectedMethod("hello", RubyNodeTypes.METHOD, 125, "hello()", null, "method.rb",  List.of()),
            new ExpectedMethod("method_missing", RubyNodeTypes.METHOD, 141, "method_missing(method_name, *args, &block)", null, "method.rb", List.of("method_name", "*args", "&block")),
            new ExpectedMethod("respond_to_missing?", RubyNodeTypes.METHOD, 151, "respond_to_missing?(method_name, include_private)", null, "method.rb", List.of("method_name", "include_private")),
            new ExpectedMethod("initialize",  RubyNodeTypes.METHOD,173, "initialize()", null, "method.rb",  List.of()), // ProcRunner's initialize
            new ExpectedMethod("run_proc",  RubyNodeTypes.METHOD,177, "run_proc(val)", null, "method.rb",  List.of("val")),
            new ExpectedMethod("run_lambda",  RubyNodeTypes.METHOD,181, "run_lambda(val)", null, "method.rb",  List.of("val")),
            new ExpectedMethod("run_instance_lambda",  RubyNodeTypes.METHOD,185, "run_instance_lambda(val)", null, "method.rb",  List.of("val")),
            new ExpectedMethod("run_class_proc",  RubyNodeTypes.METHOD,189, "run_class_proc(val)", null, "method.rb",  List.of("val")),
            new ExpectedMethod("public_method",  RubyNodeTypes.METHOD,205, "public_method()", null,  "method.rb",  List.of()),
            new ExpectedMethod("protected_method",  RubyNodeTypes.METHOD,213, "protected_method()", null, "method.rb",  List.of()),
            new ExpectedMethod("protected_method_called_from_public",  RubyNodeTypes.METHOD,217, "protected_method_called_from_public()", null, "method.rb",  List.of()),
            new ExpectedMethod("private_method",  RubyNodeTypes.METHOD,223, "private_method()", null, "method.rb",List.of()),
            new ExpectedMethod("private_method_called_from_public",  RubyNodeTypes.METHOD,227, "private_method_called_from_public()", null, "method.rb", List.of()),
            new ExpectedMethod("call_protected_from_subclass",  RubyNodeTypes.METHOD,233, "call_protected_from_subclass(other)", null, "method.rb",List.of("other")),
            new ExpectedMethod("call_own_private",  RubyNodeTypes.METHOD,237, "call_own_private()", null, "method.rb", List.of()),
            new ExpectedMethod("top_level_method_example",  RubyNodeTypes.METHOD,260, "top_level_method_example(name)", null, "method.rb", List.of("name")),
            new ExpectedMethod("optional_param_method",  RubyNodeTypes.METHOD,283, "optional_param_method(a)", null, "method.rb", List.of("a")),
            new ExpectedMethod("keyword_param_method",  RubyNodeTypes.METHOD,287, "keyword_param_method(a)", null, "method.rb", List.of("a")),
            new ExpectedMethod("hash_splat_param_method",  RubyNodeTypes.METHOD,291, "hash_splat_param_method(**options)", null, "method.rb", List.of("**options")),
            new ExpectedMethod("all_param_types",  RubyNodeTypes.METHOD,295, "all_param_types(required, optional, *splat, keyword_req, keyword_opt, **hash_splat, &block)", null, "method.rb", List.of("required", "optional", "*splat", "keyword_req", "keyword_opt", "**hash_splat", "&block"))
        );

        for (ExpectedMethod expected : expectedMethodsData) {
            CstNode actualNode = findMethod(methodNodes, expected.name(), expected.line());

            assertThat("Type for " + expected.name(), actualNode.getType(), is(equalTo(expected.type())));
            assertThat("SimpleName for " + expected.name(), actualNode.getSimpleName(), is(equalTo(expected.name())));
            assertThat("LocalName for " + expected.name(), actualNode.getLocalName(), is(equalTo(expected.localName())));

            Location actualLocation = actualNode.getLocation();
            assertThat("Location file for " + expected.name(), actualLocation.getFile(), is(equalTo("method.rb")));
            assertThat("Location line for " + expected.name(), actualLocation.getBeginLine(), is(equalTo(expected.line())));

            List<String> actualParamNames = actualNode.getParameters().stream()
                .map(Parameter::getName)
                .collect(Collectors.toList());
            assertThat("Parameters for " + expected.name(), actualParamNames, is(equalTo(expected.params())));
        }
    }
}
