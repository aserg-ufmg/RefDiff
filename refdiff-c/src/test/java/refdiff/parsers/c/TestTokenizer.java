package refdiff.parsers.c;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Test;

import refdiff.core.diff.RastRootHelper;
import refdiff.core.io.SourceFolder;
import refdiff.core.rast.RastRoot;

public class TestTokenizer {
	private CParser parser = new CParser();
	
	@Test
	public void shouldTokenize() throws Exception {
		SourceFolder sources = SourceFolder.from(Paths.get("test-data/c/tokenize"), Paths.get("file.c"));
		String sourceCode = sources.readContent(sources.getSourceFiles().get(0));
		
		RastRoot rastRoot = this.parser.parse(sources);
		
		assertThat(
			RastRootHelper.retrieveTokens(rastRoot, sourceCode, rastRoot.getNodes().get(0), false),
			is(Arrays.asList("#", "define", "MAX_BUCKETS_FILE_SIZE", "(", "256", "*", "32", ")", "int", "load_buckets", "(", "const", "char", "*", "base", ",", "int64_t", "*", "buckets", ",", "int", "*", "last", ")", "{", "char", "buf", "[", "MAX_BUCKETS_FILE_SIZE", "]", ";", "}"))
		);
	}
}
