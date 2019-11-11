package refdiff.parsers.js;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import refdiff.core.diff.CstRootHelper;
import refdiff.core.io.SourceFolder;
import refdiff.core.cst.Location;
import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstNodeRelationship;
import refdiff.core.cst.CstNodeRelationshipType;
import refdiff.core.cst.CstRoot;
import refdiff.core.cst.Stereotype;
import refdiff.test.util.JsParserSingleton;

public class TestJsParser {
	
	private JsPlugin parser = JsParserSingleton.get();
	
	@Test
	public void shouldParseSimpleFile() throws Exception {
		Path basePath = Paths.get("test-data/parser/js/");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("ex1.js"));
		CstRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(1));
		
		CstNode nodeScriptEx1 = root.getNodes().get(0);
		assertThat(nodeScriptEx1.getType(), is("File"));
		assertThat(nodeScriptEx1.getNamespace(), is(""));
		assertThat(nodeScriptEx1.getLocation(), is(new Location("ex1.js", 0, 83, 1)));
		
		assertThat(nodeScriptEx1.getNodes().size(), is(2));
		CstNode nodeArrowFn = nodeScriptEx1.getNodes().get(0);
		CstNode nodeFnHello = nodeScriptEx1.getNodes().get(1);
		
		assertThat(nodeArrowFn.getType(), is("Function"));
		assertThat(nodeArrowFn.getLocation(), is(new Location("ex1.js", 16, 23, 2, 22, 23)));
		assertThat(nodeArrowFn.getLocalName(), is("fn"));
		
		assertThat(nodeFnHello.getType(), is("Function"));
		assertThat(nodeFnHello.getLocation(), is(new Location("ex1.js", 28, 83, 6, 50, 82)));
		assertThat(nodeFnHello.getLocalName(), is("hello"));
		assertThat(nodeFnHello.getParameters().size(), is(1));
		assertThat(nodeFnHello.getParameters().get(0).getName(), is("name"));
	}
	
	@Test
	public void shouldParseFunctionCall() throws Exception {
		Path basePath = Paths.get("test-data/parser/js/");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("ex2.js"));
		CstRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(1));
		
		CstNode nodeScriptEx2 = root.getNodes().get(0);
		assertThat(nodeScriptEx2.getType(), is("File"));
		
		assertThat(nodeScriptEx2.getNodes().size(), is(2));
		CstNode nodeF1 = nodeScriptEx2.getNodes().get(0);
		CstNode nodeF2 = nodeScriptEx2.getNodes().get(1);
		
		assertThat(nodeF1.getType(), is("Function"));
		assertThat(nodeF1.getLocalName(), is("f1"));
		
		assertThat(nodeF2.getType(), is("Function"));
		assertThat(nodeF2.getLocalName(), is("f2"));
		
		Set<CstNodeRelationship> relationships = root.getRelationships();
		assertThat(relationships.size(), is(1));
		assertThat(relationships, hasItem(rel(CstNodeRelationshipType.USE, nodeF1, nodeF2)));
	}
	
	@Test
	public void shouldParseClassDeclaration() throws Exception {
		Path basePath = Paths.get("test-data/parser/js/");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("ex3.js"));
		CstRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(1));
		
		CstNode nodeScriptEx3 = root.getNodes().get(0);
		assertThat(nodeScriptEx3.getType(), is("File"));
		
		assertThat(nodeScriptEx3.getNodes().size(), is(1));
		CstNode nodeRectangle = nodeScriptEx3.getNodes().get(0);
		
		assertThat(nodeRectangle.getType(), is("Class"));
		assertThat(nodeRectangle.getLocalName(), is("Rectangle"));
		
		assertThat(nodeRectangle.getNodes().size(), is(3));
		
		CstNode contructor = nodeRectangle.getNodes().get(0);
		assertThat(contructor.getType(), is("Function"));
		assertThat(contructor.getLocalName(), is("constructor"));
		assertThat(contructor.getParameters().size(), is(2));
		assertThat(contructor.getParameters().get(0).getName(), is("height"));
		assertThat(contructor.getParameters().get(1).getName(), is("width"));
		assertTrue(contructor.hasStereotype(Stereotype.TYPE_CONSTRUCTOR));
		
		CstNode methodGetArea = nodeRectangle.getNodes().get(1);
		assertThat(methodGetArea.getType(), is("Function"));
		assertThat(methodGetArea.getLocalName(), is("area"));
		
		CstNode methodCalcArea = nodeRectangle.getNodes().get(2);
		assertThat(methodCalcArea.getType(), is("Function"));
		assertThat(methodCalcArea.getLocalName(), is("calcArea"));
	}
	
	@Test
	public void shouldParseFunctionVar() throws Exception {
		Path basePath = Paths.get("test-data/parser/js/");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("ex4.js"));
		CstRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(1));
		CstNode script = root.getNodes().get(0);
		assertThat(script.getType(), is("File"));
		
		CstNode f1 = script.getNodes().get(0);
		assertThat(f1.getLocalName(), is("f1"));
		assertThat(f1.getType(), is("Function"));
		assertThat(f1.getParameters().size(), is(1));
		assertThat(f1.getParameters().get(0).getName(), is("x"));
		
		CstNode f2 = script.getNodes().get(1);
		assertThat(f2.getLocalName(), is("f2"));
		assertThat(f2.getType(), is("Function"));
		assertThat(f2.getParameters().size(), is(1));
		assertThat(f2.getParameters().get(0).getName(), is("x"));
		
		CstNode f3 = script.getNodes().get(2);
		assertThat(f3.getLocalName(), is("f3"));
		assertThat(f3.getType(), is("Function"));
		assertThat(f3.getParameters().size(), is(1));
		assertThat(f3.getParameters().get(0).getName(), is("x"));
		
		CstNode f4 = script.getNodes().get(3);
		assertThat(f4.getLocalName(), is("f4"));
		assertThat(f4.getType(), is("Function"));
		assertThat(f4.getParameters().size(), is(1));
		assertThat(f4.getParameters().get(0).getName(), is("x"));
	}
	
	@Test
	public void shouldParseObjectLiteralFunctionProperty() throws Exception {
		Path basePath = Paths.get("test-data/parser/js/");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("ex5.js"));
		CstRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(1));
		CstNode script = root.getNodes().get(0);
		assertThat(script.getType(), is("File"));
		
		CstNode f1 = script.getNodes().get(0);
		assertThat(f1.getLocalName(), is("f1"));
		assertThat(f1.getType(), is("Function"));
		assertThat(f1.getParameters().size(), is(1));
		assertThat(f1.getParameters().get(0).getName(), is("x"));
		
		CstNode f2 = script.getNodes().get(1);
		assertThat(f2.getLocalName(), is("f2"));
		assertThat(f2.getType(), is("Function"));
		assertThat(f2.getParameters().size(), is(1));
		assertThat(f2.getParameters().get(0).getName(), is("y"));
		
		String sourceCode = sources.readContent(sources.getSourceFiles().get(0));
		assertThat(
			CstRootHelper.retrieveTokens(root, sourceCode, f2, false),
			is(Arrays.asList("(", "y", ")", "=>", "y", "+", "2")));
	}
	
	@Test
	public void shouldParseAssignmentOfFunctionExpression() throws Exception {
		Path basePath = Paths.get("test-data/parser/js/");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("ex6.js"));
		CstRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(1));
		CstNode script = root.getNodes().get(0);
		assertThat(script.getType(), is("File"));
		
		CstNode f1 = script.getNodes().get(0);
		assertThat(f1.getLocalName(), is("f1"));
		assertThat(f1.getType(), is("Function"));
		assertThat(f1.getParameters().size(), is(1));
		assertThat(f1.getParameters().get(0).getName(), is("x"));
		
		CstNode f2 = script.getNodes().get(1);
		assertThat(f2.getLocalName(), is("f2"));
		assertThat(f2.getType(), is("Function"));
		assertThat(f2.getParameters().size(), is(1));
		assertThat(f2.getParameters().get(0).getName(), is("y"));
		
		String sourceCode = sources.readContent(sources.getSourceFiles().get(0));
		assertThat(
			CstRootHelper.retrieveTokens(root, sourceCode, f2, false),
			is(Arrays.asList("(", "y", ")", "=>", "y", "+", "2")));
	}
	
	@Test
	public void shouldNotHandleAnonymousFunctionsAsCstNodes() throws Exception {
		Path basePath = Paths.get("test-data/parser/js/");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("ex7.js"));
		CstRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(1));
		CstNode script = root.getNodes().get(0);
		assertThat(script.getType(), is("File"));
		
		CstNode bar = script.getNodes().get(0);
		assertThat(bar.getLocalName(), is("bar"));
		assertThat(bar.getType(), is("Function"));
		assertThat(bar.getParameters().size(), is(2));
		assertThat(bar.getParameters().get(0).getName(), is("x"));
		assertThat(bar.getParameters().get(1).getName(), is("y"));
	}
	
	@Test
	public void shouldParseSubfolder() throws Exception {
		Path basePath = Paths.get("test-data/parser/js/");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("dir1/ex5.js"));
		CstRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(1));
		CstNode script = root.getNodes().get(0);
		assertThat(script.getType(), is("File"));
		assertThat(script.getNamespace(), is("dir1/"));
		assertThat(script.getLocalName(), is("ex5.js"));
		assertThat(script.getSimpleName(), is("ex5.js"));
		
	}
	
	@Test
	public void shouldParseLargeFile() throws Exception {
		Path basePath = Paths.get("test-data/parser/js/");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("input.js"));
		CstRoot root = parser.parse(sources);
		
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
		
		CstRoot cstRoot = parser.parse(sources);
		CstNode fileNode = cstRoot.getNodes().get(0);
		String sourceCode = sources.readContent(sources.getSourceFiles().get(0));
		
		List<String> actual = CstRootHelper.retrieveTokens(cstRoot, sourceCode, fileNode, false);
		List<String> expected = Arrays.asList("var", "x", "=", "{", "fn", ":", "(", ")", "=>", "1", "}", ";", "function", "hello", "(", "name", ")", "{", "console", ".", "log", "(", "'hello '", "+", "name", ")", ";", "}");
		
		assertThat(actual, is(expected));
	}
	
	private CstNodeRelationship rel(CstNodeRelationshipType type, CstNode n1, CstNode n2) {
		return new CstNodeRelationship(type, n1.getId(), n2.getId());
	}
}
