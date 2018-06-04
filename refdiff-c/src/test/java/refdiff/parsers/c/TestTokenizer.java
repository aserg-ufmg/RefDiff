package refdiff.parsers.c;

import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

public class TestTokenizer {
private CParser parser = new CParser();
	
	@Test
	public void shouldNotThrowExceptionWhenTokenizing() throws Exception {
		byte[] encoded = Files.readAllBytes(Paths.get("test-data/c/tokenize/file.c"));
		String source = new String(encoded, StandardCharsets.UTF_8);
		
		try {
			this.parser.tokenize(source);
		}
		catch (RuntimeException e) {
			e.printStackTrace();
			fail("Should not have thrown exception");
		}
	}
}
