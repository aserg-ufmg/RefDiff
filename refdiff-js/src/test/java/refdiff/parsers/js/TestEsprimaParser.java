package refdiff.parsers.js;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import refdiff.core.io.FileSystemSourceFile;
import refdiff.core.io.SourceFile;
import refdiff.core.rast.Location;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastRoot;

public class TestEsprimaParser {
	
	@Test
	public void shouldParseSimpleFile() throws Exception {
		EsprimaParser parser = new EsprimaParser();
		Path basePath = Paths.get("src/test/resources/parser/");
		Set<SourceFile> sourceFiles = Collections.singleton(new FileSystemSourceFile(basePath, Paths.get("ex1.js")));
		RastRoot root = parser.parse(sourceFiles);
		
		assertThat(root.getNodes().size(), is(1));
		
		RastNode nodeScriptEx1 = root.getNodes().get(0);
		assertThat(nodeScriptEx1.getType(), is("Program"));
		assertThat(nodeScriptEx1.getLocation(), is(new Location("ex1.js", 0, 77)));
		
		assertThat(nodeScriptEx1.getNodes().size(), is(2));
		RastNode nodeArrowFn = nodeScriptEx1.getNodes().get(0);
		RastNode nodeFnHello = nodeScriptEx1.getNodes().get(1);
		
		assertThat(nodeArrowFn.getType(), is("ArrowFunctionExpression"));
		assertThat(nodeArrowFn.getLocation(), is(new Location("ex1.js", 17, 24)));
		assertThat(nodeArrowFn.getLocalName(), is(""));
		
		assertThat(nodeFnHello.getType(), is("FunctionDeclaration"));
		assertThat(nodeFnHello.getLocation(), is(new Location("ex1.js", 32, 77)));
		assertThat(nodeFnHello.getLocalName(), is("hello"));
	}
}
