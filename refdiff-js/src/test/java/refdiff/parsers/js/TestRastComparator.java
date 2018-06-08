package refdiff.parsers.js;

import static org.junit.Assert.*;
import static refdiff.test.util.RastDiffMatchers.*;

import java.nio.file.Paths;

import org.junit.Test;

import refdiff.core.diff.RastComparator;
import refdiff.core.diff.RastDiff;
import refdiff.core.diff.RelationshipType;
import refdiff.core.io.SourceFolder;
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
		SourceFolder sourcesBefore = SourceFolder.from(Paths.get(basePath, "v0"), ".js");
		SourceFolder sourcesAfter = SourceFolder.from(Paths.get(basePath, "v1"), ".js");
		RastComparator comparator = new RastComparator(parser, parser);
		return comparator.compare(sourcesBefore, sourcesAfter);
	}
	
}
