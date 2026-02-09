package refdiff.parsers.go;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstRoot;
import refdiff.core.diff.CstRootHelper;
import refdiff.core.io.SourceFolder;
import refdiff.test.util.GoParserSingleton;

public class TestCstGoParser {
	
	private GoPlugin parser = GoParserSingleton.get();
	
	@Test
	public void shouldMatchNodes() throws Exception {
		Path basePath = Paths.get("test-data/parser/go/");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("example.go"));
		CstRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(6));

		assertThat(root.getNodes().get(0).getType(), is(NodeType.FILE));
		assertThat(root.getNodes().get(0).getSimpleName(), is("example.go"));

		assertThat(root.getNodes().get(1).getType(), is(NodeType.INTERFACE));
		assertThat(root.getNodes().get(1).getSimpleName(), is("Handler"));

		// interface function
		assertThat(root.getNodes().get(2).getType(), is(NodeType.FUNCTION));
		assertThat(root.getNodes().get(2).getSimpleName(), is("Handle"));

		assertThat(root.getNodes().get(3).getType(), is(NodeType.STRUCT));
		assertThat(root.getNodes().get(3).getSimpleName(), is("MyHandle"));

		// struct function
		assertThat(root.getNodes().get(4).getType(), is(NodeType.FUNCTION));
		assertThat(root.getNodes().get(4).getSimpleName(), is("Handle"));

		assertThat(root.getNodes().get(5).getType(), is(NodeType.FUNCTION));
		assertThat(root.getNodes().get(5).getSimpleName(), is("main"));
	}

	@Test
	public void shouldTokenizeSimpleFile() throws Exception {
		Path basePath = Paths.get("test-data/parser/go/");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("small.go"));

		CstRoot cstRoot = parser.parse(sources);
		CstNode fileNode = cstRoot.getNodes().get(0);
		String sourceCode = sources.readContent(sources.getSourceFiles().get(0));

		
		List<String> actual = CstRootHelper.retrieveTokens(cstRoot, sourceCode, fileNode, false);
		List<String> expected = Arrays.asList("package", "main", "\n", "// comment with UTF-8 chars: áçãûm test",
				"func", "Test", "(", "a", "string", ")", "string", "{", "return", "a", "\n", "}");

		assertArrayEquals(expected.toArray(), actual.toArray());
	}
	
}
