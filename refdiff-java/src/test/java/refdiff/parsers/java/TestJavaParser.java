package refdiff.parsers.java;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

import org.junit.Test;

import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstNodeRelationship;
import refdiff.core.cst.CstNodeRelationshipType;
import refdiff.core.cst.CstRoot;
import refdiff.core.cst.Location;
import refdiff.core.diff.CstRootHelper;
import refdiff.core.io.SourceFolder;
import refdiff.parsers.LanguagePlugin;

public class TestJavaParser {
	
	private LanguagePlugin parser = new JavaPlugin();
	
	@Test
	public void shouldParseFiles() throws Exception {
		Path basePath = Paths.get("test-data/parser/java");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("p2/Foo.java"), Paths.get("p1/Bar.java"));
		
		CstRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(2));
		
		CstNode classFoo = root.getNodes().get(0);
		assertThat(classFoo.getType(), is(NodeTypes.CLASS_DECLARATION));
		assertThat(classFoo.getNamespace(), is("p2."));
		assertThat(classFoo.getLocalName(), is("Foo"));
		assertThat(classFoo.getSimpleName(), is("Foo"));
		assertThat(classFoo.getLocation(), is(new Location("p2/Foo.java", 13, 121, 3)));
		
		assertThat(classFoo.getNodes().size(), is(1));
		CstNode fooM1 = classFoo.getNodes().get(0);
		assertThat(fooM1.getType(), is(NodeTypes.METHOD_DECLARATION));
		assertThat(fooM1.getLocalName(), is("m1(String)"));
		assertThat(fooM1.getParameters().size(), is(1));
		assertThat(fooM1.getParameters().get(0).getName(), is("arg"));
		assertThat(fooM1.getSimpleName(), is("m1"));
		assertThat(fooM1.getLocation(), is(new Location("p2/Foo.java", 35, 117, 9, 111, 116)));
		
		CstNode classBar = root.getNodes().get(1);
		assertThat(classBar.getType(), is(NodeTypes.CLASS_DECLARATION));
		assertThat(classBar.getNamespace(), is("p1."));
		assertThat(classBar.getLocalName(), is("Bar"));
		assertThat(classBar.getSimpleName(), is("Bar"));
		
		assertThat(classBar.getNodes().size(), is(2));
		CstNode barM1 = classBar.getNodes().get(0);
		assertThat(barM1.getType(), is(NodeTypes.METHOD_DECLARATION));
		assertThat(barM1.getLocalName(), is("m1(String)"));
		assertThat(barM1.getSimpleName(), is("m1"));
		
		CstNode barM2 = classBar.getNodes().get(1);
		assertThat(barM2.getType(), is(NodeTypes.METHOD_DECLARATION));
		assertThat(barM2.getLocalName(), is("m2()"));
		assertThat(barM2.getSimpleName(), is("m2"));
		
		Set<CstNodeRelationship> relationships = root.getRelationships();
		assertThat(relationships.size(), is(2));
		assertThat(relationships, hasItem(rel(CstNodeRelationshipType.USE, barM1, barM2)));
		assertThat(relationships, hasItem(rel(CstNodeRelationshipType.SUBTYPE, classBar, classFoo)));
		
		String fooSourceCode = sources.readContent(sources.getSourceFiles().get(0));
		assertThat(
			CstRootHelper.retrieveTokens(root, fooSourceCode, fooM1, false),
			is(Arrays.asList("/**", "A", "javadoc", "comment.", "@param", "arg", "*/", "public", "void", "m1", "(", "String", "arg", ")", "{", "}"))
		);
		
		String barSourceCode = sources.readContent(sources.getSourceFiles().get(1));
		assertThat(
			CstRootHelper.retrieveTokens(root, barSourceCode, barM1, false),
			is(Arrays.asList("public", "void", "m1", "(", "String", "arg", ")", "{", "m2", "(", ")", ";", "}"))
		);
		
		assertThat(
			CstRootHelper.retrieveTokens(root, barSourceCode, barM1, true),
			is(Arrays.asList("m2", "(", ")", ";"))
		);
	}
	
	private CstNodeRelationship rel(CstNodeRelationshipType type, CstNode n1, CstNode n2) {
		return new CstNodeRelationship(type, n1.getId(), n2.getId());
	}
}
