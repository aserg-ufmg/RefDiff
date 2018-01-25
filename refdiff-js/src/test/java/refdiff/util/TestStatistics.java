package refdiff.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import refdiff.core.util.Statistics;

public class TestStatistics {
	
	@Test
	public void shouldComputeMedian() {
		assertThat(
			Statistics.median(Arrays.asList(1.0)),
			is(1.0));
		
		assertThat(
			Statistics.median(Arrays.asList(1.0, 2.0)),
			is(1.5));
		
		assertThat(
			Statistics.median(Arrays.asList(1.0, 2.0, 3.0)),
			is(2.0));
		
		assertThat(
			Statistics.median(Arrays.asList(1.0, 2.0, 2.2, 4.0)),
			is(2.1));
	}

	@Test
	public void shouldComputeQ1AndQ3() {
		assertThat(
			Statistics.q1(Arrays.asList(1.0, 2.0)),
			is(1.0));
		
		assertThat(
			Statistics.q3(Arrays.asList(1.0, 2.0)),
			is(2.0));
		
		assertThat(
			Statistics.q1(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0)),
			is(1.5));
		
		assertThat(
			Statistics.q3(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0)),
			is(4.5));
	}
	
	@Test
	public void shouldNotComputeMedianOfEmptyList() {
		try {
			Statistics.median(Collections.emptyList());
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
	
}
