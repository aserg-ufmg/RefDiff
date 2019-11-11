package refdiff.parsers.java;

import static org.junit.Assert.*;
import static refdiff.test.util.CstDiffMatchers.*;

import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import refdiff.core.diff.CstComparator;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.RelationshipType;
import refdiff.core.io.SourceFolder;
import refdiff.parsers.LanguagePlugin;

public class TestCstComparator {
	
	private static LanguagePlugin parser = new JavaPlugin();
	
	@Test
	public void shouldMatchExtractMethod() throws Exception {
		assertThat(diff("java"), containsOnly(
			relationship(RelationshipType.SAME, node("Foo"), node("Foo")),
			relationship(RelationshipType.SAME, node("Foo", "m1(String)"), node("Foo", "m1(String)")),
			relationship(RelationshipType.SAME, node("Foo", "m3(String)"), node("Foo", "m3(String)")),
			relationship(RelationshipType.EXTRACT, node("Foo", "m1(String)"), node("Foo", "m2()")),
			relationship(RelationshipType.EXTRACT, node("Foo", "m3(String)"), node("Foo", "m2()"))
		));
	}
	
	@Test
	public void shouldMatchPullUpAndPushDown() throws Exception {
		assertThat(diff("pullUpAndPushDown"), containsOnly(
			relationship(RelationshipType.SAME, node("p1.A"), node("p1.A")),
			relationship(RelationshipType.SAME, node("p1.A1"), node("p1.A1")),
			relationship(RelationshipType.SAME, node("p1.A2"), node("p1.A2")),
			relationship(RelationshipType.PUSH_DOWN, node("p1.A", "m1()"), node("p1.A1", "m1()")),
			relationship(RelationshipType.PUSH_DOWN, node("p1.A", "m1()"), node("p1.A2", "m1()")),
			relationship(RelationshipType.PULL_UP, node("p1.A1", "m2(String)"), node("p1.A", "m2(String)")),
			relationship(RelationshipType.PULL_UP, node("p1.A2", "m2(String)"), node("p1.A", "m2(String)"))
		));
	}
	
	@Test
	public void shouldMatchPullUpToAdded() throws Exception {
		assertThat(diff("pullUpToAdded"), containsOnly(
			relationship(RelationshipType.SAME, node("p1.A1"), node("p1.A1")),
			relationship(RelationshipType.SAME, node("p1.A1", "m1()"), node("p1.A1", "m1()")),
			relationship(RelationshipType.PULL_UP, node("p1.A1", "m2(String)"), node("p1.A", "m2(String)")),
			relationship(RelationshipType.EXTRACT_SUPER, node("p1.A1"), node("p1.A"))
		));
	}
	
	@Ignore
	@Test
	public void shouldMatchPullUpFromRemoved() throws Exception {
		assertThat(diff("pullUpFromRemoved"), containsOnly(
			relationship(RelationshipType.SAME, node("p1.A"), node("p1.A")),
			relationship(RelationshipType.PULL_UP, node("p1.A1", "m1()"), node("p1.A", "m1()")),
			relationship(RelationshipType.PULL_UP, node("p1.A1", "m2(String)"), node("p1.A", "m2(String)"))
		));
	}
	
	@Test
	public void shouldMatchPushDownToAdded() throws Exception {
		assertThat(diff("pushDownToAdded"), containsOnly(
			relationship(RelationshipType.SAME, node("p1.A"), node("p1.A")),
			relationship(RelationshipType.PUSH_DOWN, node("p1.A", "m2(String)"), node("p1.A1", "m2(String)"))
		));
	}
	
	@Ignore
	@Test
	public void shouldMatchPushDownFromRemoved() throws Exception {
		assertThat(diff("pushDownFromRemoved"), containsOnly(
			relationship(RelationshipType.SAME, node("p1.A1"), node("p1.A1")),
			relationship(RelationshipType.PUSH_DOWN, node("p1.A", "m2(String)"), node("p1.A1", "m2(String)"))
		));
	}
	
	@Test
	public void shouldMatchPullUpSignature() throws Exception {
		assertThat(diff("pullUpSignature"), containsOnly(
			relationship(RelationshipType.SAME, node("p1.A"), node("p1.A")),
			relationship(RelationshipType.SAME, node("p1.A1"), node("p1.A1")),
			relationship(RelationshipType.SAME, node("p1.A1", "m1()"), node("p1.A1", "m1()")),
			relationship(RelationshipType.PULL_UP_SIGNATURE, node("p1.A1", "m1()"), node("p1.A", "m1()"))
		));
	}
	
	@Test
	public void shouldNotFindFalsePullUp() throws Exception {
		assertThat(diff("falsePullUp"), containsOnly(
			relationship(RelationshipType.SAME, node("p1.A"), node("p1.A")),
			relationship(RelationshipType.SAME, node("p1.A1"), node("p1.A1")),
			relationship(RelationshipType.SAME, node("p1.A", "m1()"), node("p1.A", "m1()"))
		));
	}
	
	@Test
	public void shouldMatchPushDownImpl() throws Exception {
		assertThat(diff("pushDownImpl"), containsOnly(
			relationship(RelationshipType.SAME, node("p1.A"), node("p1.A")),
			relationship(RelationshipType.SAME, node("p1.A1"), node("p1.A1")),
			relationship(RelationshipType.SAME, node("p1.A", "m1()"), node("p1.A", "m1()")),
			relationship(RelationshipType.PUSH_DOWN_IMPL, node("p1.A", "m1()"), node("p1.A1", "m1()"))
		));
	}
	
	@Test
	public void shouldNotMatchFalsePushDownImpl() throws Exception {
		assertThat(diff("falsePushDownImpl"), containsOnly(
			relationship(RelationshipType.SAME, node("p1.A"), node("p1.A")),
			relationship(RelationshipType.SAME, node("p1.A", "m1()"), node("p1.A", "m1()"))
		));
	}
	
	@Test
	public void shouldMatchChangeSignature() throws Exception {
		assertThat(diff("java3"), containsOnly(
			relationship(RelationshipType.SAME, node("p1.A"), node("p1.A")),
			relationship(RelationshipType.CHANGE_SIGNATURE, node("p1.A", "m1(Integer)"), node("p1.A", "m1(Integer, boolean)")),
			relationship(RelationshipType.CHANGE_SIGNATURE, node("p1.A", "m2(Long)"), node("p1.A", "m2(Long, boolean)")),
			relationship(RelationshipType.CHANGE_SIGNATURE, node("p1.A", "m3(Double)"), node("p1.A", "m3(Double, boolean)"))
		));
	}
	
	@Test
	public void shouldMatchExtractOverloadedMethod() throws Exception {
		assertThat(diff("java4"), containsOnly(
			relationship(RelationshipType.SAME, node("p1.A"), node("p1.A")),
			relationship(RelationshipType.SAME, node("p1.A", "fetch(Feed)"), node("p1.A", "fetch(Feed)")),
			relationship(RelationshipType.EXTRACT, node("p1.A", "fetch(Feed)"), node("p1.A", "fetch(String)"))
		));
	}
	
	@Test
	public void shouldMatchExtractSuperclass() throws Exception {
		assertThat(diff("java5"), containsOnly(
			relationship(RelationshipType.SAME, node("p1.A"), node("p1.A")),
			relationship(RelationshipType.SAME, node("p1.A", "m1()"), node("p1.A", "m1()")),
			relationship(RelationshipType.PULL_UP, node("p1.A", "m2(int)"), node("p1.B", "m2(int)")),
			relationship(RelationshipType.EXTRACT_SUPER, node("p1.A"), node("p1.B"))
		));
	}
	
	@Test
	public void shouldNotMatchPullUpWhenSuperclassIsRenamed() throws Exception {
		assertThat(diff("java6"), containsOnly(
			relationship(RelationshipType.SAME, node("p1.A"), node("p1.A")),
			relationship(RelationshipType.SAME, node("p1.A", "m1()"), node("p1.A", "m1()")),
			relationship(RelationshipType.SAME, node("p1.A", "m2(int)"), node("p1.A", "m2(int)")),
			relationship(RelationshipType.RENAME, node("p1.B"), node("p1.C")),
			relationship(RelationshipType.SAME, node("p1.B", "m2(int)"), node("p1.C", "m2(int)"))
		));
	}
	
	@Test
	@Ignore
	public void shouldNotMatchExtractGetterAndSetter() throws Exception {
		assertThat(diff("java7"), containsOnly(
			relationship(RelationshipType.SAME, node("p1.A"), node("p1.A")),
			relationship(RelationshipType.SAME, node("p1.A", "m1()"), node("p1.A", "m1()")),
			relationship(RelationshipType.SAME, node("p1.A", "m2()"), node("p1.A", "m2()")),
			relationship(RelationshipType.SAME, node("p1.A", "m3()"), node("p1.A", "m3()")),
			relationship(RelationshipType.SAME, node("p1.A", "m4()"), node("p1.A", "m4()"))
		));
	}
	
	@Test
	public void shouldMatchNestedRename() throws Exception {
		assertThat(diff("nestedRename"), containsOnly(
			relationship(RelationshipType.RENAME, node("p1.A"), node("p1.B")),
			relationship(RelationshipType.SAME, node("p1.A", "m1(String)"), node("p1.B", "m1(String)")),
			relationship(RelationshipType.SAME, node("p1.A", "m2(String)"), node("p1.B", "m2(String)")),
			relationship(RelationshipType.RENAME, node("p1.A", "m3(String)"), node("p1.B", "m3x(String)"))
		));
	}
	
	@Test
	public void shouldMatchMoveByMatchingChildren() throws Exception {
		assertThat(diff("moveByMatchingChildren"), containsOnly(
			relationship(RelationshipType.MOVE, node("p1.A"), node("p2.A")),
			relationship(RelationshipType.SAME, node("p1.A", "m1()"), node("p2.A", "m1()"))
		));
	}
	
	@Ignore
	@Test
	public void shouldMatchRenameByMatchingChildren() throws Exception {
		assertThat(diff("renameByMatchingChildren"), containsOnly(
			relationship(RelationshipType.RENAME, node("p1.A"), node("p1.B")),
			relationship(RelationshipType.SAME, node("p1.A", "m1()"), node("p1.B", "m1()"))
		));
	}
	
	private CstDiff diff(String folder) throws Exception {
		String basePath = "test-data/diff/" + folder;
		SourceFolder sourcesBefore = SourceFolder.from(Paths.get(basePath, "v0"), ".java");
		SourceFolder sourcesAfter = SourceFolder.from(Paths.get(basePath, "v1"), ".java");
		CstComparator comparator = new CstComparator(parser);
		return comparator.compare(sourcesBefore, sourcesAfter);
	}
	
}
