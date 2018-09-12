package refdiff.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static refdiff.core.util.IdentifierSplitter.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import refdiff.core.util.IdentifierSplitter;

public class TestIdentifierSplitter {
	
	@Test
	public void shouldSplitEmptyString() {
		assertThat(
			split(""),
			is(tokens()));
	}
	
	@Test
	public void shouldSplitSimpleIdentifier() {
		assertThat(
			split("foo"),
			is(tokens("foo")));
		
		assertThat(
			split("FOO"),
			is(tokens("FOO")));
		
		assertThat(
			split("foo37"),
			is(tokens("foo37")));
	}
	
	@Test
	public void shouldSplitCamelCase() {
		assertThat(
			split("fooBar"),
			is(tokens("foo", "Bar")));
		
		assertThat(
			split("FooBarBaz"),
			is(tokens("Foo", "Bar", "Baz")));
		
		assertThat(
			split("HTMLBuilder"),
			is(tokens("HTML", "Builder")));
		
		assertThat(
			split("SuperHTMLBuilder"),
			is(tokens("Super", "HTML", "Builder")));
	}
	
	@Test
	public void shouldSplitUnderline() {
		assertThat(
			split("foo_bar"),
			is(tokens("foo", "bar")));
		
		assertThat(
			split("__foo_bar"),
			is(tokens("foo", "bar")));
	}
	
	@Test
	public void shouldSplitPath() {
		assertThat(
			IdentifierSplitter.split("foo/bar"),
			is(tokens("foo", "bar")));
		
		assertThat(
			IdentifierSplitter.split("foo/bar/"),
			is(tokens("foo", "bar")));
	}
	
	@Test
	public void shouldSplitDot() {
		assertThat(
			IdentifierSplitter.split("foo.bar"),
			is(tokens("foo", "bar")));
	}
	
	@Test
	public void shouldSplitEverything() {
		assertThat(
			IdentifierSplitter.split("src/folder_x/baz.MyFile.java"),
			is(tokens("src", "folder", "x", "baz", "My", "File", "java")));
	}
	
	private List<String> tokens(String... tokens) {
		return Arrays.asList(tokens);
	}
}
