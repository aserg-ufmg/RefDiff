package refdiff.core.diff.similarity;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class TestMultiset {
	
	@Test
	public void shouldSubtractElementsFromCollection() {
		Multiset<String> ms = new Multiset<>();
		ms.add("x", 1);
		ms.add("y", 2);
		ms.add("z", 3);
		
		Multiset<String> r = ms.minusElements(Arrays.asList("x", "z"));
		
		assertThat(r.getMultiplicity("x"), is(0));
		assertThat(r.getMultiplicity("y"), is(2));
		assertThat(r.getMultiplicity("z"), is(0));
		assertThat(r.size(), is(2));
	}
	
}
