package refdiff.parsers.java;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

public class TestJavaParser {
	
	private JavaParser parser = new JavaParser();
	
	@Test
	public void shouldParseFiles() throws Exception {
		Path basePath = Paths.get("test-data/parser/java");
		List<SourceFile> sourceFiles = new ArrayList<>();
		sourceFiles.add(new FileSystemSourceFile(basePath, Paths.get("Foo.java")));
		sourceFiles.add(new FileSystemSourceFile(basePath, Paths.get("p1/Bar.java")));
		
		RastRoot root = parser.parse(sourceFiles);
		
		assertThat(root.getNodes().size(), is(2));
		
		RastNode classFoo = root.getNodes().get(0);
		assertThat(classFoo.getType(), is("TypeDeclaration"));
		assertThat(classFoo.getNamespace(), is(""));
		assertThat(classFoo.getLocalName(), is("Foo"));
		assertThat(classFoo.getSimpleName(), is("Foo"));
		assertThat(classFoo.getLocation(), is(new Location("Foo.java", 0, 66)));
		
		assertThat(classFoo.getNodes().size(), is(1));
		RastNode fooM1 = classFoo.getNodes().get(0);
		assertThat(fooM1.getType(), is("MethodDeclaration"));
		assertThat(fooM1.getLocalName(), is("m1(String)"));
		assertThat(fooM1.getSimpleName(), is("m1"));
		assertThat(fooM1.getLocation(), is(new Location("Foo.java", 24, 60)));
		
		RastNode classBar = root.getNodes().get(1);
		assertThat(classBar.getType(), is("TypeDeclaration"));
		assertThat(classBar.getNamespace(), is("p1."));
		assertThat(classBar.getLocalName(), is("Bar"));
		assertThat(classBar.getSimpleName(), is("Bar"));
		assertThat(classBar.getLocation(), is(new Location("p1/Bar.java", 15, 152)));
		
		assertThat(classBar.getNodes().size(), is(2));
		RastNode barM1 = classBar.getNodes().get(0);
		assertThat(barM1.getType(), is("MethodDeclaration"));
		assertThat(barM1.getLocalName(), is("m1(String)"));
		assertThat(barM1.getSimpleName(), is("m1"));
		assertThat(barM1.getLocation(), is(new Location("p1/Bar.java", 39, 80)));
		
		RastNode barM2 = classBar.getNodes().get(1);
		assertThat(barM2.getType(), is("MethodDeclaration"));
		assertThat(barM2.getLocalName(), is("m2()"));
		assertThat(barM2.getSimpleName(), is("m2"));
		assertThat(barM2.getLocation(), is(new Location("p1/Bar.java", 86, 146)));
		
		Set<RastNodeRelationship> relationships = root.getRelationships();
		assertThat(relationships.size(), is(1));
		assertThat(relationships, hasItem(rel(RastNodeRelationshipType.USE, barM1, barM2)));
	}
	
	private RastNodeRelationship rel(RastNodeRelationshipType type, RastNode n1, RastNode n2) {
		return new RastNodeRelationship(type, n1.getId(), n2.getId());
	}
}
