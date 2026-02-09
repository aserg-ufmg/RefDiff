package validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class TestTag {

	@Test
	public void parsesNdjsonFileIntoTagObject() throws Exception {
		Path tempDir = Files.createTempDirectory("tags");
		try {
			InputStream is = getClass().getResourceAsStream("/foo.ndjson");
			assertNotNull(is, "test resource foo.ndjson not found on classpath");
			Path dest = tempDir.resolve("foo.ndjson");
			Files.copy(is, dest);

			List<Tag> tags = Tag.parseJson(dest);
			assertEquals(1, tags.size());
			Tag t = tags.get(0);

			assertEquals("tag", t.getType());
			assertEquals("main", t.getName());
			assertEquals("foo/bar/baz.java", t.getPath());
			assertEquals("/^    public static void main(String[] args) {$/", t.getPattern());
			assertEquals(Integer.valueOf(75), t.getLine());
			assertEquals("method", t.getKind());
			assertEquals("array", t.getScope());
			assertEquals("class", t.getScopeKind());
			assertEquals(Integer.valueOf(104), t.getEnd());
			assertEquals(Integer.valueOf(1), t.getNdjsonLine());
		} finally {
			Files.walk(tempDir)
				.sorted(Comparator.reverseOrder())
				.forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception e) { /* ignore */ } });
		}
	}

}
