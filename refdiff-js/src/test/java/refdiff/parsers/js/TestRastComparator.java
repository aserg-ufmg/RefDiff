package refdiff.parsers.js;

import static org.junit.Assert.assertThat;
import static refdiff.test.util.RastDiffMatchers.containsOnly;
import static refdiff.test.util.RastDiffMatchers.node;
import static refdiff.test.util.RastDiffMatchers.relationship;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import refdiff.core.diff.RastComparator;
import refdiff.core.diff.ThresholdsProvider;
import refdiff.core.diff.RastDiff;
import refdiff.core.diff.RelationshipType;
import refdiff.core.diff.similarity.TfIdfSourceRepresentation;
import refdiff.core.diff.similarity.TfIdfSourceRepresentationBuilder;
import refdiff.core.io.FileSystemSourceFile;
import refdiff.core.io.SourceFile;
import refdiff.test.util.EsprimaParserSingleton;

public class TestRastComparator {
	
	private EsprimaParser parser = EsprimaParserSingleton.get();
	
	@Test
	public void shouldMatchWithSameNamePath() throws Exception {
		assertThat(diff("diff1"), containsOnly(
			relationship(RelationshipType.SAME, node("hello.js"), node("hello.js")),
			relationship(RelationshipType.SAME, node("hello.js", "foo"), node("hello.js", "foo")),
			relationship(RelationshipType.SAME, node("hello.js", "bar"), node("hello.js", "bar"))
		));
	}
	
	@Test
	public void shouldMatchRenameFile() throws Exception {
		assertThat(diff("diff2"), containsOnly(
			relationship(RelationshipType.RENAME, node("hello.js"), node("hello2.js")),
			relationship(RelationshipType.SAME, node("hello.js", "foo"), node("hello2.js", "foo")),
			relationship(RelationshipType.SAME, node("hello.js", "bar"), node("hello2.js", "bar"))
		));
	}
	
	@Test
	public void shouldMatchExtractFunction() throws Exception {
		assertThat(diff("diff3"), containsOnly(
			relationship(RelationshipType.SAME, node("hello.js"), node("hello.js")),
			relationship(RelationshipType.SAME, node("hello.js", "bar"), node("hello.js", "bar")),
			relationship(RelationshipType.EXTRACT, node("hello.js", "bar"), node("hello.js", "foo"))
		));
	}
	
	@Test
	public void shouldMatchInlineFunction() throws Exception {
		assertThat(diff("diff4"), containsOnly(
			relationship(RelationshipType.SAME, node("hello.js"), node("hello.js")),
			relationship(RelationshipType.SAME, node("hello.js", "bar"), node("hello.js", "bar")),
			relationship(RelationshipType.INLINE, node("hello.js", "foo"), node("hello.js", "bar"))
		));
	}
	
	private RastDiff diff(String folder) throws Exception {
		String basePath = "src/test/resources/" + folder;
		List<SourceFile> sourceFilesBefore = getSourceFiles(Paths.get(basePath, "v0"));
		List<SourceFile> sourceFilesAfter = getSourceFiles(Paths.get(basePath, "v1"));
		RastComparator<TfIdfSourceRepresentation> comparator = new RastComparator<>(parser, parser, new TfIdfSourceRepresentationBuilder());
		return comparator.compare(sourceFilesBefore, sourceFilesAfter);
	}
	
	private List<SourceFile> getSourceFiles(Path basePath) throws IOException {
		try (Stream<Path> stream = Files.walk(basePath)) {
			return stream.filter(path -> path.getFileName().toString().endsWith(".js"))
				.map(path -> new FileSystemSourceFile(basePath, basePath.relativize(path)))
				.collect(Collectors.toList());
		}
	}
	
}
