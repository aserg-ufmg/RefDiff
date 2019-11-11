package refdiff.parsers.c;

import static org.junit.Assert.*;
import static refdiff.test.util.CstDiffMatchers.*;

import java.nio.file.Paths;

import org.junit.Test;

import refdiff.core.diff.CstComparator;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.Relationship;
import refdiff.core.diff.RelationshipType;
import refdiff.core.io.SourceFileSet;
import refdiff.core.io.SourceFolder;
import refdiff.core.cst.CstNode;

public class TestCstComparator {
	
	private CPlugin parser = new CPlugin();
	
	@Test
	public void shouldMatchSameFile() throws Exception {
		assertThat(diff("sameFile"), containsOnly(
			relationship(RelationshipType.SAME, node("file.c"), node("file.c")),
			relationship(RelationshipType.SAME, node("file.c", "f1()"), node("file.c", "f1()")),
			relationship(RelationshipType.SAME, node("file.c", "f2(int)"), node("file.c", "f2(int)")),
			relationship(RelationshipType.SAME, node("file.c", "main()"), node("file.c", "main()"))
		));
	}
	
	@Test
	public void shouldMatchSameFunction() throws Exception {
		assertThat(diff("sameFunction"), contains(
			relationship(RelationshipType.SAME, node("file.c", "main()"), node("file2.c", "main()"))
		));
		
		assertThat(diff("sameFunction"), doesntContain(
			relationship(RelationshipType.SAME, node("file.c"), node("file2.c"))
		));
	}
	
	@Test
	public void shouldMatchExtractFunction() throws Exception {
		assertThat(diff("extractFunction"), containsOnly(
			relationship(RelationshipType.SAME, node("file.c"), node("file.c")),
			relationship(RelationshipType.SAME, node("file.c", "f1()"), node("file.c", "f1()")),
			relationship(RelationshipType.SAME, node("file.c", "f3()"), node("file.c", "f3()")),
			relationship(RelationshipType.SAME, node("file.c", "main()"), node("file.c", "main()")),
			relationship(RelationshipType.EXTRACT, node("file.c", "f1()"), node("file.c", "f2()")),
			relationship(RelationshipType.EXTRACT, node("file.c", "f3()"), node("file.c", "f2()"))
		));
	}
	
	@Test
	public void shouldMatchRenameFile() throws Exception {
		assertThat(diff("renameFile"), containsOnly(
			relationship(RelationshipType.SAME, node("file.c", "f1()"), node("file2.c", "f1()")),
			relationship(RelationshipType.SAME, node("file.c", "main()"), node("file2.c", "main()")),
			relationship(RelationshipType.RENAME, node("file.c"), node("file2.c"))
		));
	}
	
	@Test
	public void shouldMatchRenameFunction() throws Exception {
		assertThat(diff("renameFunction"), containsOnly(
			relationship(RelationshipType.SAME, node("file.c"), node("file.c")),
			relationship(RelationshipType.SAME, node("file.c", "f1()"), node("file.c", "f1()")),
			relationship(RelationshipType.SAME, node("file.c", "main()"), node("file.c", "main()")),
			relationship(RelationshipType.RENAME, node("file.c", "f2()"), node("file.c", "f3()"))
		));
	}
	
	@Test
	public void shouldMatchChangeSignatureFunction() throws Exception {
		assertThat(diff("changeSignatureFunction"), containsOnly(
			relationship(RelationshipType.SAME, node("file.c"), node("file.c")),
			relationship(RelationshipType.SAME, node("file.c", "main()"), node("file.c", "main()")),
			relationship(RelationshipType.CHANGE_SIGNATURE, node("file.c", "f1(char, int)"), node("file.c", "f1(int, char)"))
		));
	}
	
	@Test
	public void shouldMatchInlineFunction() throws Exception {
		assertThat(diff("inlineFunction"), containsOnly(
			relationship(RelationshipType.SAME, node("file.c"), node("file.c")),
			relationship(RelationshipType.SAME, node("file.c", "main()"), node("file.c", "main()")),
			relationship(RelationshipType.INLINE, node("file.c", "f1()"), node("file.c", "main()"))
		));
	}
	
	@Test
	// Indirection
	public void shouldMatchInlineFunction2() throws Exception {
		assertThat(diff("inlineFunction2"), containsOnly(
			relationship(RelationshipType.SAME, node("file.c"), node("file.c")),
			relationship(RelationshipType.SAME, node("file.c", "main()"), node("file.c", "main()")),
			relationship(RelationshipType.SAME, node("file.c", "f1()"), node("file.c", "f1()")),
			relationship(RelationshipType.INLINE, node("file.c", "f2()"), node("file.c", "main()"))
		));
	}
	
	@Test
	public void shouldMatchMoveFunction() throws Exception {
		assertThat(diff("moveFunction"), containsOnly(
			relationship(RelationshipType.SAME, node("file.c"), node("file.c")),
			relationship(RelationshipType.SAME, node("file.c", "main()"), node("file.c", "main()")),
			relationship(RelationshipType.MOVE, node("file.c", "f1(int, int)"), node("function.h", "f1(int, int)"))
		));
	}
	
	@Test
	public void shouldMatchMoveFile() throws Exception {
		assertThat(diff("moveFile"), containsOnly(
			relationship(RelationshipType.SAME, node("folder1/file.c", "main()"), node("folder2/file.c", "main()")),
			relationship(RelationshipType.MOVE, node("folder1/file.c"), node("folder2/file.c"))
		));
	}
	
	@Test
	public void shouldMatchMoveRenameFunction() throws Exception {
		CstDiff diff = diff("moveRenameFunction");
		
		assertThat(diff, containsOnly(
			relationship(RelationshipType.SAME, node("file.c"), node("file.c")),
			relationship(RelationshipType.SAME, node("file.c", "main()"), node("file.c", "main()")),
			relationship(RelationshipType.MOVE_RENAME, node("file.c", "f1(int, int)"), node("function.h", "f2(int, int)"))
		));
		
		Relationship relationship = diff.getRelationships().stream()
				.filter(r -> r.getType().equals(RelationshipType.MOVE_RENAME))
				.findFirst()
				.get();
		
		CstNode nodeBefore = relationship.getNodeBefore();
		CstNode nodeAfter = relationship.getNodeAfter();
		
		assertEquals(nodeBefore.getType(), "FunctionDeclaration");
		assertEquals(nodeAfter.getType(), "FunctionDeclaration");
	}
	
	@Test
	public void shouldMatchMoveRenameFile() throws Exception {
		CstDiff diff = diff("moveRenameFile");
		
		assertThat(diff, containsOnly(
			relationship(RelationshipType.SAME, node("folder1/file1.c", "main()"), node("folder2/file2.c", "main()")),
			relationship(RelationshipType.MOVE_RENAME, node("folder1/file1.c"), node("folder2/file2.c"))
		));
		
		Relationship relationship = diff.getRelationships().stream()
				.filter(r -> r.getType().equals(RelationshipType.MOVE_RENAME))
				.findFirst()
				.get();
		
		CstNode nodeBefore = relationship.getNodeBefore();
		CstNode nodeAfter = relationship.getNodeAfter();
		
		assertEquals(nodeBefore.getType(), "Program");
		assertEquals(nodeAfter.getType(), "Program");
	}
	
	private CstDiff diff(String folder) throws Exception {
		String basePath = "test-data/c/" + folder;
		SourceFileSet sourcesBefore = SourceFolder.from(Paths.get(basePath, "v0"), ".c", ".h");
		SourceFileSet sourcesAfter = SourceFolder.from(Paths.get(basePath, "v1"), ".c", ".h");
		CstComparator comparator = new CstComparator(parser);
		return comparator.compare(sourcesBefore, sourcesAfter);
	}
	
}
