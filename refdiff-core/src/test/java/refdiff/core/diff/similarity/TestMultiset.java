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
	
	@Test
	public void shouldAdd() {
		Multiset<String> m1 = new Multiset<>();
		m1.add("x", 1);
		m1.add("y", 2);
		m1.add("z", 3);
		
		Multiset<String> m2 = new Multiset<>();
		m2.add("z", 1);
		m2.add("w", 2);
		
		Multiset<String> r = m1.plus(m2);
		
		assertThat(r.getMultiplicity("x"), is(1));
		assertThat(r.getMultiplicity("y"), is(2));
		assertThat(r.getMultiplicity("z"), is(4));
		assertThat(r.getMultiplicity("w"), is(2));
		assertThat(r, not(sameInstance(m1)));
		assertThat(r, not(sameInstance(m1)));
	}
	
}
