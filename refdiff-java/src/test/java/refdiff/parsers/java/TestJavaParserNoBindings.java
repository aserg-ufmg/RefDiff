package refdiff.parsers.java;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.Test;

import refdiff.core.io.SourceFolder;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastNodeRelationship;
import refdiff.core.rast.RastNodeRelationshipType;
import refdiff.core.rast.RastRoot;
import refdiff.parsers.CstParser;

public class TestJavaParserNoBindings {
	
	private CstParser parser = new JavaParserNoBindings();
	
	@Test
	public void shouldParseFiles() throws Exception {
		Path basePath = Paths.get("test-data/parser/methodInvocations");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("p2/Foo.java"), Paths.get("p1/Bar.java"), Paths.get("p1/Bar2.java"));
		
		RastRoot root = parser.parse(sources);
		
		RastNode classBar = root.getNodes().get(1);
		assertThat(classBar.getType(), is(NodeTypes.CLASS_DECLARATION));
		assertThat(classBar.getSimpleName(), is("Bar"));
		
		assertThat(classBar.getNodes().size(), is(3));
		RastNode barM1 = classBar.getNodes().get(0);
		assertThat(barM1.getLocalName(), is("m1(String)"));
		
		RastNode barM2 = classBar.getNodes().get(1);
		assertThat(barM2.getLocalName(), is("m2()"));
		
		RastNode barM2Int = classBar.getNodes().get(2);
		assertThat(barM2Int.getLocalName(), is("m2(int)"));
		
		Set<RastNodeRelationship> relationships = root.getRelationships();
		
		assertThat(relationships, hasItem(rel(RastNodeRelationshipType.USE, barM1, barM2Int)));
		assertThat(relationships, not(hasItem(rel(RastNodeRelationshipType.USE, barM1, barM2))));
		
		
		RastNode classFoo = root.getNodes().get(0);
		assertThat(classFoo.getType(), is(NodeTypes.CLASS_DECLARATION));
		assertThat(classFoo.getSimpleName(), is("Foo"));
		
		assertThat(classFoo.getNodes().size(), is(1));
		RastNode fooM2 = classFoo.getNodes().get(0);
		assertThat(fooM2.getLocalName(), is("m2(int)"));
		
		assertThat(relationships, not(hasItem(rel(RastNodeRelationshipType.USE, barM1, fooM2))));
	}
	
	private RastNodeRelationship rel(RastNodeRelationshipType type, RastNode n1, RastNode n2) {
		return new RastNodeRelationship(type, n1.getId(), n2.getId());
	}
}
