// package refdiff.parsers.universal.php;

// import static org.junit.Assert.assertThat;

// import static refdiff.test.util.CstDiffMatchers.containsOnly;
// import static refdiff.test.util.CstDiffMatchers.node;
// import static refdiff.test.util.CstDiffMatchers.relationship;

// import java.nio.file.Path;
// import java.nio.file.Paths;

// import org.junit.jupiter.api.Test;

// import refdiff.core.diff.CstComparator;
// import refdiff.core.diff.CstDiff;
// import refdiff.core.io.SourceFolder;
// import refdiff.parsers.LanguagePlugin;
// import refdiff.parsers.universal.UniversalPlugin;
// import refdiff.core.diff.Relationship;
// import refdiff.core.diff.RelationshipType;


// public class TestPhpCstComparator {
//   private static final LanguagePlugin parser = new UniversalPlugin();
//     private static final String TEST_DATA_BASE_PATH = "src/test/resources/php/refactor";

//     private CstDiff diff(String folderName) throws Exception {
//         Path baseFolderPath = Paths.get(TEST_DATA_BASE_PATH, folderName);
//         SourceFolder sourcesBefore = SourceFolder.from(baseFolderPath.resolve("v0"), ".php");
//         SourceFolder sourcesAfter = SourceFolder.from(baseFolderPath.resolve("v1"), ".php");
//         CstComparator comparator = new CstComparator(parser);
//         return comparator.compare(sourcesBefore, sourcesAfter);
//     }

//     @Test
//     public void shouldMatchChangeSignature() throws Exception {
//         CstDiff diff = diff("changeSignature");
//         assertThat(diff, containsOnly(
//             relationship(RelationshipType.CHANGE_SIGNATURE, node("main.php/foo($a, $b)"), node("main.php/foo($c, $d)")),
//             relationship(RelationshipType.SAME, node("main.php/bar()"), node("main.php/bar()"))
//         ));
//     }

//     @Test
//     public void shouldMatchRename() throws Exception {
//         CstDiff diff = diff("rename");
//         assertThat(diff, containsOnly(
//             relationship(RelationshipType.RENAME, node("main.php/foo()"), node("main.php/bar()"))
//         ));
//     }

//     @Test
//     public void shouldMatchMove() throws Exception {
//         CstDiff diff = diff("move");
//         assertThat(diff, containsOnly(
//             relationship(RelationshipType.MOVE, node("main.php/bar()"), node("bar.php/bar()")),
//             relationship(RelationshipType.SAME, node("main.php/foo()"), node("main.php/foo()"))
//         ));
//     }

//     // @Test // TODO クラスを追加して検出できるようにする
//     // public void shouldMatchInternalMove() throws Exception {
//     //     CstDiff diff = diff("internalMove");
//     //     for (Relationship r : diff.getRelationships()) {
//     //         System.out.println(r);
//     //     }
//     //     assertThat(diff, containsOnly(
//     //             relationship(RelationshipType.INTERNAL_MOVE, node("main.php/bar(self)"), node("main.php/bar(self)")),
//     //             relationship(RelationshipType.SAME, node("main.php/foo(self)"), node("main.php/foo(self)"))));
//     // }
    
//     @Test
//     public void shouldMatchMoveRename() throws Exception {
//         CstDiff diff = diff("moveRename");
//         assertThat(diff, containsOnly(
//             relationship(RelationshipType.MOVE_RENAME, node("main.php/bar()"), node("baz.php/baz()")),
//             relationship(RelationshipType.SAME, node("main.php/foo()"), node("main.php/foo()"))
//         ));
//     }

//     @Test
//     public void shouldMatchExtract() throws Exception {
//         CstDiff diff = diff("extract");
//         assertThat(diff, containsOnly(
//             relationship(RelationshipType.EXTRACT, node("main.php/foo()"), node("main.php/extracted_method()")),
//             relationship(RelationshipType.SAME, node("main.php/foo()"), node("main.php/foo()")),
//             relationship(RelationshipType.SAME, node("main.php/bar()"), node("main.php/bar()"))
//         ));
//     }

//     @Test
//     public void shouldMatchInline() throws Exception {
//         CstDiff diff = diff("inline");
//         assertThat(diff, containsOnly(
//             relationship(RelationshipType.INLINE, node("main.php/calculate_sum($a, $b)"), node("main.php/foo()")),
//             relationship(RelationshipType.SAME, node("main.php/foo()"), node("main.php/foo()")),
//             relationship(RelationshipType.SAME, node("main.php/bar()"), node("main.php/bar()"))
//         ));
//     }
// }
