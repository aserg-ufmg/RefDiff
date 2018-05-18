package refdiff.parsers.c;

import static org.junit.Assert.*;
import static refdiff.test.util.RastDiffMatchers.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import refdiff.core.diff.RastComparator;
import refdiff.core.diff.RastDiff;
import refdiff.core.diff.RelationshipType;
import refdiff.core.io.FileSystemSourceFile;
import refdiff.core.io.SourceFile;

public class TestRastComparator {
	
	private CParser parser = new CParser();
	
	@Test
	public void shouldMatchWithSameNamePath() throws Exception {
		assertThat(diff("same"), containsOnly(
			relationship(RelationshipType.SAME, node("hello.c"), node("hello.c")),
			relationship(RelationshipType.SAME, node("hello.c", "f1()"), node("hello.c", "f1()")),
			relationship(RelationshipType.SAME, node("hello.c", "f2(int)"), node("hello.c", "f2(int)")),
			relationship(RelationshipType.SAME, node("hello.c", "main()"), node("hello.c", "main()"))
		));
	}
	
	@Test
	public void shouldMatchExtractMethod() throws Exception {
		assertThat(diff("extract"), containsOnly(
			relationship(RelationshipType.SAME, node("hello.c"), node("hello.c")),
			relationship(RelationshipType.SAME, node("hello.c", "f1()"), node("hello.c", "f1()")),
			relationship(RelationshipType.SAME, node("hello.c", "f3()"), node("hello.c", "f3()")),
			relationship(RelationshipType.SAME, node("hello.c", "main()"), node("hello.c", "main()")), 
			relationship(RelationshipType.EXTRACT, node("hello.c", "f1()"), node("hello.c", "f2()")),
			relationship(RelationshipType.EXTRACT, node("hello.c", "f3()"), node("hello.c", "f2()")) 
		));
	}
	
	@Test
	public void shouldMatchRenameMethod() throws Exception {
		assertThat(diff("rename"), containsOnly(
			relationship(RelationshipType.SAME, node("hello.c"), node("hello.c")),
			relationship(RelationshipType.SAME, node("hello.c", "f1()"), node("hello.c", "f1()")),
			relationship(RelationshipType.SAME, node("hello.c", "main()"), node("hello.c", "main()")), 
			relationship(RelationshipType.RENAME, node("hello.c", "f2()"), node("hello.c", "f3()"))
		));
	}
	
	@Test
	public void shouldMatchChangeMethodSignature() throws Exception {
		assertThat(diff("changeSignature"), containsOnly(
			relationship(RelationshipType.SAME, node("hello.c"), node("hello.c")),
			relationship(RelationshipType.SAME, node("hello.c", "main()"), node("hello.c", "main()")), 
			relationship(RelationshipType.CHANGE_SIGNATURE, node("hello.c", "f1(char, int)"), node("hello.c", "f1(int, char)"))
		));
	}
	
	private RastDiff diff(String folder) throws Exception {
		String basePath = "test-data/c/" + folder;
		List<SourceFile> sourceFilesBefore = getSourceFiles(Paths.get(basePath, "v0"));
		List<SourceFile> sourceFilesAfter = getSourceFiles(Paths.get(basePath, "v1"));
		RastComparator comparator = new RastComparator(parser, parser);
		return comparator.compare(sourceFilesBefore, sourceFilesAfter);
	}
	
	private List<SourceFile> getSourceFiles(Path basePath) throws IOException {
		try (Stream<Path> stream = Files.walk(basePath)) {
			return stream.filter(path -> path.getFileName().toString().endsWith(".c"))
				.map(path -> new FileSystemSourceFile(basePath, basePath.relativize(path)))
				.collect(Collectors.toList());
		}
	}
	
}
