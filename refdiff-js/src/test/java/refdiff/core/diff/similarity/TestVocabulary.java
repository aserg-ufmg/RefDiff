package refdiff.core.diff.similarity;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class TestVocabulary {
	
	@Test
	public void shouldCountTerms() throws Exception {
		Vocabulary v = new Vocabulary();
		
		v.count(true, Arrays.asList("a", "b"));
		v.count(true, Arrays.asList("a", "c"));
		
		v.count(false, Arrays.asList("a", "d"));
		
		assertThat(v.getDc(), is(2));
		assertThat(v.getDf("a"), is(2));
		assertThat(v.getDf("b"), is(1));
		assertThat(v.getDf("c"), is(1));
		assertThat(v.getDf("d"), is(1));
	}
	
}
