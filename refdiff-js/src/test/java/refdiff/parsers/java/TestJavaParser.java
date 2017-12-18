package refdiff.parsers.java;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import refdiff.core.io.FileSystemSourceFile;
import refdiff.core.io.SourceFile;
import refdiff.core.rast.Location;
import refdiff.core.rast.RastNode;
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
		assertThat(classFoo.getLocalName(), is("Foo"));
		assertThat(classFoo.getSimpleName(), is("Foo"));
		assertThat(classFoo.getLocation(), is(new Location("Foo.java", 0, 66)));

		assertThat(classFoo.getNodes().size(), is(1));
		RastNode m1 = classFoo.getNodes().get(0);
		assertThat(m1.getType(), is("MethodDeclaration"));
		assertThat(m1.getLocalName(), is("m1(String)"));
		assertThat(m1.getSimpleName(), is("m1"));
		assertThat(m1.getLocation(), is(new Location("Foo.java", 24, 60)));

		RastNode cuBar = root.getNodes().get(1);
		assertThat(cuBar.getType(), is("TypeDeclaration"));
		assertThat(cuBar.getLocalName(), is("p1.Bar"));
		assertThat(cuBar.getSimpleName(), is("Bar"));
		assertThat(cuBar.getLocation(), is(new Location("p1/Bar.java", 15, 42)));
	}
	
}
