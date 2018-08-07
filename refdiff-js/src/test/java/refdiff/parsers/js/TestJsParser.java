package refdiff.parsers.js;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import refdiff.core.diff.RastRootHelper;
import refdiff.core.io.SourceFolder;
import refdiff.core.rast.Location;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastNodeRelationship;
import refdiff.core.rast.RastNodeRelationshipType;
import refdiff.core.rast.RastRoot;
import refdiff.core.rast.Stereotype;
import refdiff.test.util.JsParserSingleton;

public class TestJsParser {
	
	private JsParser parser = JsParserSingleton.get();
	
	@Test
	public void shouldParseSimpleFile() throws Exception {
		Path basePath = Paths.get("test-data/parser/js/");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("ex1.js"));
		RastRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(1));
		
		RastNode nodeScriptEx1 = root.getNodes().get(0);
		assertThat(nodeScriptEx1.getType(), is("Program"));
		assertThat(nodeScriptEx1.getNamespace(), is(""));
		assertThat(nodeScriptEx1.getLocation(), is(new Location("ex1.js", 0, 83)));
		
		assertThat(nodeScriptEx1.getNodes().size(), is(2));
		RastNode nodeArrowFn = nodeScriptEx1.getNodes().get(0);
		RastNode nodeFnHello = nodeScriptEx1.getNodes().get(1);
		
		assertThat(nodeArrowFn.getType(), is("ArrowFunctionExpression"));
		assertThat(nodeArrowFn.getLocation(), is(new Location("ex1.js", 16, 23, 22, 23)));
		assertThat(nodeArrowFn.getLocalName(), is(""));
		
		assertThat(nodeFnHello.getType(), is("FunctionDeclaration"));
		assertThat(nodeFnHello.getLocation(), is(new Location("ex1.js", 28, 83, 50, 82)));
		assertThat(nodeFnHello.getLocalName(), is("hello"));
		assertThat(nodeFnHello.getParameters().size(), is(1));
		assertThat(nodeFnHello.getParameters().get(0).getName(), is("name"));
	}
	
	@Test
	public void shouldParseFunctionCall() throws Exception {
		Path basePath = Paths.get("test-data/parser/js/");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("ex2.js"));
		RastRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(1));
		
		RastNode nodeScriptEx2 = root.getNodes().get(0);
		assertThat(nodeScriptEx2.getType(), is("Program"));
		
		assertThat(nodeScriptEx2.getNodes().size(), is(2));
		RastNode nodeF1 = nodeScriptEx2.getNodes().get(0);
		RastNode nodeF2 = nodeScriptEx2.getNodes().get(1);
		
		assertThat(nodeF1.getType(), is("FunctionDeclaration"));
		assertThat(nodeF1.getLocalName(), is("f1"));
		
		assertThat(nodeF2.getType(), is("FunctionDeclaration"));
		assertThat(nodeF2.getLocalName(), is("f2"));
		
		Set<RastNodeRelationship> relationships = root.getRelationships();
		assertThat(relationships.size(), is(1));
		assertThat(relationships, hasItem(rel(RastNodeRelationshipType.USE, nodeF1, nodeF2)));
	}
	
	@Test
	public void shouldParseClassDeclaration() throws Exception {
		Path basePath = Paths.get("test-data/parser/js/");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("ex3.js"));
		RastRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(1));
		
		RastNode nodeScriptEx3 = root.getNodes().get(0);
		assertThat(nodeScriptEx3.getType(), is("Program"));
		
		assertThat(nodeScriptEx3.getNodes().size(), is(1));
		RastNode nodeRectangle = nodeScriptEx3.getNodes().get(0);
		
		assertThat(nodeRectangle.getType(), is("ClassDeclaration"));
		assertThat(nodeRectangle.getLocalName(), is("Rectangle"));
		
		assertThat(nodeRectangle.getNodes().size(), is(3));
		
		RastNode contructor = nodeRectangle.getNodes().get(0);
		assertThat(contructor.getType(), is("ClassMethod"));
		assertThat(contructor.getLocalName(), is("constructor"));
		assertThat(contructor.getParameters().size(), is(2));
		assertThat(contructor.getParameters().get(0).getName(), is("height"));
		assertThat(contructor.getParameters().get(1).getName(), is("width"));
		assertTrue(contructor.hasStereotype(Stereotype.TYPE_CONSTRUCTOR));
		
		RastNode methodGetArea = nodeRectangle.getNodes().get(1);
		assertThat(methodGetArea.getType(), is("ClassMethod"));
		assertThat(methodGetArea.getLocalName(), is("area"));
		
		RastNode methodCalcArea = nodeRectangle.getNodes().get(2);
		assertThat(methodCalcArea.getType(), is("ClassMethod"));
		assertThat(methodCalcArea.getLocalName(), is("calcArea"));
	}
	
	@Test
	public void shouldParseFunctionVar() throws Exception {
		Path basePath = Paths.get("test-data/parser/js/");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("ex4.js"));
		RastRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(1));
		RastNode script = root.getNodes().get(0);
		assertThat(script.getType(), is("Program"));
		
		RastNode f1 = script.getNodes().get(0);
		assertThat(f1.getLocalName(), is("f1"));
		assertThat(f1.getType(), is("Function"));
		assertThat(f1.getParameters().size(), is(1));
		assertThat(f1.getParameters().get(0).getName(), is("x"));
		
		RastNode f2 = script.getNodes().get(1);
		assertThat(f2.getLocalName(), is("f2"));
		assertThat(f2.getType(), is("Function"));
		assertThat(f2.getParameters().size(), is(1));
		assertThat(f2.getParameters().get(0).getName(), is("x"));
		
		RastNode f3 = script.getNodes().get(2);
		assertThat(f3.getLocalName(), is("f3"));
		assertThat(f3.getType(), is("Function"));
		assertThat(f3.getParameters().size(), is(1));
		assertThat(f3.getParameters().get(0).getName(), is("x"));
		
		RastNode f4 = script.getNodes().get(3);
		assertThat(f4.getLocalName(), is("f4"));
		assertThat(f4.getType(), is("Function"));
		assertThat(f4.getParameters().size(), is(1));
		assertThat(f4.getParameters().get(0).getName(), is("x"));
	}
	
	@Test
	public void shouldParseLargeFile() throws Exception {
		Path basePath = Paths.get("test-data/parser/js/");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("input.js"));
		RastRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(1));
	}
	
	@Test
	public void shouldTokenizeLargeFile() throws Exception {
		Path basePath = Paths.get("test-data/parser/js/");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("input.js"));
		parser.parse(sources);
	}
	
	@Test
	public void shouldTokenizeSimpleFile() throws Exception {
		Path basePath = Paths.get("test-data/parser/js/");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("ex1.js"));
		
		RastRoot rastRoot = parser.parse(sources);
		RastNode fileNode = rastRoot.getNodes().get(0);
		String sourceCode = sources.readContent(sources.getSourceFiles().get(0));
		
		List<String> actual = RastRootHelper.retrieveTokens(rastRoot, sourceCode, fileNode, false);
		List<String> expected = Arrays.asList("var", "x", "=", "{", "fn", ":", "(", ")", "=>", "1", "}", ";", "function", "hello", "(", "name", ")", "{", "console", ".", "log", "(", "'hello '", "+", "name", ")", ";", "}");
		
		assertThat(actual, is(expected));
	}
	
	private RastNodeRelationship rel(RastNodeRelationshipType type, RastNode n1, RastNode n2) {
		return new RastNodeRelationship(type, n1.getId(), n2.getId());
	}
}
