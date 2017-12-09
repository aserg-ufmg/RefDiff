package refdiff.parsers.js;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import refdiff.core.io.FileSystemSourceFile;
import refdiff.core.io.SourceFile;
import refdiff.core.rast.Location;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastNodeRelationship;
import refdiff.core.rast.RastNodeRelationshipType;
import refdiff.core.rast.RastRoot;
import refdiff.test.util.EsprimaParserSingleton;

public class TestEsprimaParser {
	
	private EsprimaParser parser = EsprimaParserSingleton.get();
	
	@Test
	public void shouldParseSimpleFile() throws Exception {
		Path basePath = Paths.get("src/test/resources/parser/");
		List<SourceFile> sourceFiles = Collections.singletonList(new FileSystemSourceFile(basePath, Paths.get("ex1.js")));
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
	
	@Test
	public void shouldParseFunctionCall() throws Exception {
		Path basePath = Paths.get("src/test/resources/parser/");
		List<SourceFile> sourceFiles = Collections.singletonList(new FileSystemSourceFile(basePath, Paths.get("ex2.js")));
		RastRoot root = parser.parse(sourceFiles);
		
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
		Path basePath = Paths.get("src/test/resources/parser/");
		List<SourceFile> sourceFiles = Collections.singletonList(new FileSystemSourceFile(basePath, Paths.get("ex3.js")));
		RastRoot root = parser.parse(sourceFiles);
		
		assertThat(root.getNodes().size(), is(1));
		
		RastNode nodeScriptEx3 = root.getNodes().get(0);
		assertThat(nodeScriptEx3.getType(), is("Program"));
		
		assertThat(nodeScriptEx3.getNodes().size(), is(1));
		RastNode nodeRectangle = nodeScriptEx3.getNodes().get(0);
		
		assertThat(nodeRectangle.getType(), is("ClassDeclaration"));
		assertThat(nodeRectangle.getLocalName(), is("Rectangle"));
		
		assertThat(nodeRectangle.getNodes().size(), is(3));
		
		RastNode contructor = nodeRectangle.getNodes().get(0);
		assertThat(contructor.getType(), is("MethodDefinition"));
		assertThat(contructor.getLocalName(), is("constructor"));
		
		RastNode methodGetArea = nodeRectangle.getNodes().get(1);
		assertThat(methodGetArea.getType(), is("MethodDefinition"));
		assertThat(methodGetArea.getLocalName(), is("area"));
		
		RastNode methodCalcArea = nodeRectangle.getNodes().get(2);
		assertThat(methodCalcArea.getType(), is("MethodDefinition"));
		assertThat(methodCalcArea.getLocalName(), is("calcArea"));
	}
	
	private RastNodeRelationship rel(RastNodeRelationshipType type, RastNode n1, RastNode n2) {
		return new RastNodeRelationship(type, n1.getId(), n2.getId());
	}
}
