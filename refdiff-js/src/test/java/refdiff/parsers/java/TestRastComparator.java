package refdiff.parsers.java;

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
import refdiff.core.diff.RastComparatorThresholds;
import refdiff.core.diff.RastDiff;
import refdiff.core.diff.RelationshipType;
import refdiff.core.diff.similarity.TfIdfSourceRepresentation;
import refdiff.core.diff.similarity.TfIdfSourceRepresentationBuilder;
import refdiff.core.io.FileSystemSourceFile;
import refdiff.core.io.SourceFile;

public class TestRastComparator {
	
	private static JavaParser parser = new JavaParser();
	private static JavaSourceTokenizer tokenizer = new JavaSourceTokenizer();
	
	@Test
	public void shouldMatchExtractMethod() throws Exception {
		assertThat(diff("java"), containsOnly(
			relationship(RelationshipType.SAME, node("Foo"), node("Foo")),
			relationship(RelationshipType.SAME, node("Foo", "m1(String)"), node("Foo", "m1(String)")),
			relationship(RelationshipType.SAME, node("Foo", "m3(String)"), node("Foo", "m3(String)")),
			relationship(RelationshipType.EXTRACT, node("Foo", "m1(String)"), node("Foo", "m2()")),
			relationship(RelationshipType.EXTRACT, node("Foo", "m3(String)"), node("Foo", "m2()"))
		));
	}
	
	private RastDiff diff(String folder) throws Exception {
		String basePath = "test-data/diff/" + folder;
		List<SourceFile> sourceFilesBefore = getSourceFiles(Paths.get(basePath, "v0"));
		List<SourceFile> sourceFilesAfter = getSourceFiles(Paths.get(basePath, "v1"));
		RastComparator<TfIdfSourceRepresentation> comparator = new RastComparator<>(parser, tokenizer, new TfIdfSourceRepresentationBuilder(), RastComparatorThresholds.DEFAULT);
		return comparator.compare(sourceFilesBefore, sourceFilesAfter);
	}
	
	private List<SourceFile> getSourceFiles(Path basePath) throws IOException {
		try (Stream<Path> stream = Files.walk(basePath)) {
			return stream.filter(path -> path.getFileName().toString().endsWith(".java"))
				.map(path -> new FileSystemSourceFile(basePath, basePath.relativize(path)))
				.collect(Collectors.toList());
		}
	}
	
}
