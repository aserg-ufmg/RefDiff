package refdiff.parsers.java;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.Test;

import refdiff.core.io.SourceFolder;
import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstNodeRelationship;
import refdiff.core.cst.CstNodeRelationshipType;
import refdiff.core.cst.CstRoot;
import refdiff.parsers.CstParser;

public class TestJavaParserNoBindings {
	
	private CstParser parser = new JavaParserNoBindings();
	
	@Test
	public void shouldParseFiles() throws Exception {
		Path basePath = Paths.get("test-data/parser/methodInvocations");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("p2/Foo.java"), Paths.get("p1/Bar.java"), Paths.get("p1/Bar2.java"));
		
		CstRoot root = parser.parse(sources);
		
		CstNode classBar = root.getNodes().get(1);
		assertThat(classBar.getType(), is(NodeTypes.CLASS_DECLARATION));
		assertThat(classBar.getSimpleName(), is("Bar"));
		
		assertThat(classBar.getNodes().size(), is(3));
		CstNode barM1 = classBar.getNodes().get(0);
		assertThat(barM1.getLocalName(), is("m1(String)"));
		
		CstNode barM2 = classBar.getNodes().get(1);
		assertThat(barM2.getLocalName(), is("m2()"));
		
		CstNode barM2Int = classBar.getNodes().get(2);
		assertThat(barM2Int.getLocalName(), is("m2(int)"));
		
		Set<CstNodeRelationship> relationships = root.getRelationships();
		
		assertThat(relationships, hasItem(rel(CstNodeRelationshipType.USE, barM1, barM2Int)));
		assertThat(relationships, not(hasItem(rel(CstNodeRelationshipType.USE, barM1, barM2))));
		
		
		CstNode classFoo = root.getNodes().get(0);
		assertThat(classFoo.getType(), is(NodeTypes.CLASS_DECLARATION));
		assertThat(classFoo.getSimpleName(), is("Foo"));
		
		assertThat(classFoo.getNodes().size(), is(1));
		CstNode fooM2 = classFoo.getNodes().get(0);
		assertThat(fooM2.getLocalName(), is("m2(int)"));
		
		assertThat(relationships, not(hasItem(rel(CstNodeRelationshipType.USE, barM1, fooM2))));
	}
	
	private CstNodeRelationship rel(CstNodeRelationshipType type, CstNode n1, CstNode n2) {
		return new CstNodeRelationship(type, n1.getId(), n2.getId());
	}
}
