package refdiff.parsers.java;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

public class TestJavaSourceTokenizer {
	
	@Test
	public void shouldTokenize() throws Exception {
		JavaSourceTokenizer tokenizer = new JavaSourceTokenizer();
		
		String source = "int foo() {return 3 + 4;}";
		
		assertThat(
			tokenizer.tokenize(source),
			is(Arrays.asList("int", "foo", "(", ")", "{", "return", "3", "+", "4", ";", "}")));
	}
	
}
