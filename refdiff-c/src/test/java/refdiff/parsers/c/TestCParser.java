package refdiff.parsers.c;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import refdiff.core.io.FileSystemSourceFile;
import refdiff.core.io.SourceFile;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastNodeRelationship;
import refdiff.core.rast.RastNodeRelationshipType;
import refdiff.core.rast.RastRoot;

public class TestCParser {
	
	private CParser parser = new CParser();
	
	@Test
	public void shouldParseSimpleFile() throws Exception {
		Path basePath = Paths.get("test-data/c/parser");
		List<SourceFile> sourceFiles = Collections.singletonList(new FileSystemSourceFile(basePath, Paths.get("hello.c")));
		RastRoot root = parser.parse(sourceFiles);
		
		assertThat(root.getNodes().size(), is(1));
		
		RastNode nodeScriptEx2 = root.getNodes().get(0);
		assertThat(nodeScriptEx2.getType(), is("Program"));
		
		assertThat(nodeScriptEx2.getNodes().size(), is(3));
		RastNode nodeF1 = nodeScriptEx2.getNodes().get(0);
		RastNode nodeF2 = nodeScriptEx2.getNodes().get(1);
		RastNode nodeMain = nodeScriptEx2.getNodes().get(2);
		
		assertThat(nodeF1.getType(), is("FunctionDeclaration"));
		assertThat(nodeF1.getLocalName(), is("f1()"));
		assertThat(nodeF1.getSimpleName(), is("f1"));
		
		assertThat(nodeF2.getType(), is("FunctionDeclaration"));
		assertThat(nodeF2.getLocalName(), is("f2(int)"));
		assertThat(nodeF2.getSimpleName(), is("f2"));
		
		assertThat(nodeMain.getType(), is("FunctionDeclaration"));
		assertThat(nodeMain.getLocalName(), is("main()"));
		assertThat(nodeMain.getSimpleName(), is("main"));
		
		Set<RastNodeRelationship> relationships = root.getRelationships();
		assertThat(relationships.size(), is(2));
		assertThat(relationships, hasItem(rel(RastNodeRelationshipType.USE, nodeF2, nodeF1)));
		assertThat(relationships, hasItem(rel(RastNodeRelationshipType.USE, nodeMain, nodeF2)));
	}
	
	private RastNodeRelationship rel(RastNodeRelationshipType type, RastNode n1, RastNode n2) {
		return new RastNodeRelationship(type, n1.getId(), n2.getId());
	}
}
