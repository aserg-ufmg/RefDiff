package refdiff.parsers.c;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import refdiff.core.io.FileSystemSourceFile;
import refdiff.core.io.SourceFile;
import refdiff.core.rast.Parameter;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastNodeRelationship;
import refdiff.core.rast.RastNodeRelationshipType;
import refdiff.core.rast.RastRoot;

public class TestCParser {

	private CParser parser = new CParser();

	@Test
	public void shouldParseSimpleFile() throws Exception {
		Path basePath = Paths.get("test-data/c/parser");
		List<SourceFile> sourceFiles = Collections.singletonList(new FileSystemSourceFile(basePath, Paths.get("dir1/hello.c")));
		RastRoot root = parser.parse(sourceFiles);

		assertThat(root.getNodes().size(), is(1));

		RastNode nodeScriptEx2 = root.getNodes().get(0);
		assertThat(nodeScriptEx2.getType(), is("Program"));
		assertThat(nodeScriptEx2.getLocalName(), is("hello.c"));
		assertThat(nodeScriptEx2.getSimpleName(), is("hello.c"));
		assertThat(nodeScriptEx2.getNamespace(), is("dir1/"));

		assertThat(nodeScriptEx2.getNodes().size(), is(3));
		RastNode nodeF1 = nodeScriptEx2.getNodes().get(0);
		RastNode nodeF2 = nodeScriptEx2.getNodes().get(1);
		RastNode nodeMain = nodeScriptEx2.getNodes().get(2);

		assertThat(nodeF1.getType(), is("FunctionDeclaration"));
		assertThat(nodeF1.getLocalName(), is("f1()"));
		assertThat(nodeF1.getSimpleName(), is("f1"));
		assertThat(nodeF1.getNamespace(), is(nullValue()));

		assertThat(nodeF2.getType(), is("FunctionDeclaration"));
		assertThat(nodeF2.getLocalName(), is("f2(int)"));
		assertThat(nodeF2.getSimpleName(), is("f2"));

		assertThat(nodeMain.getType(), is("FunctionDeclaration"));
		assertThat(nodeMain.getLocalName(), is("main()"));
		assertThat(nodeMain.getSimpleName(), is("main"));

		Set<RastNodeRelationship> relationships = root.getRelationships();
		assertThat(relationships.size(), is(2));
		assertThat(relationships, hasItem(rel(RastNodeRelationshipType.USE, nodeF2, nodeF1)));
		assertThat(relationships, hasItem(rel(RastNodeRelationshipType.USE, nodeMain, nodeF2)));
	}

	private RastNodeRelationship rel(RastNodeRelationshipType type, RastNode n1, RastNode n2) {
		return new RastNodeRelationship(type, n1.getId(), n2.getId());
	}

	@Test
	public void shouldParseParameters() throws Exception {
		Path basePath = Paths.get("test-data/c/parser");
		List<SourceFile> sourceFiles = Collections.singletonList(new FileSystemSourceFile(basePath, Paths.get("file1.c")));

		RastRoot root = parser.parse(sourceFiles);

		assertThat(root.getNodes().size(), is(1));

		RastNode programNode = root.getNodes().get(0);
		assertThat(programNode.getType(), is("Program"));
		assertThat(programNode.getLocalName(), is("file1.c"));
		assertThat(programNode.getSimpleName(), is("file1.c"));
		assertThat(programNode.getNamespace(), is(""));

		assertThat(programNode.getNodes().size(), is(2));

		RastNode nodeAddItem = programNode.getNodes().get(0);

		assertThat(nodeAddItem.getType(), is("FunctionDeclaration"));
		assertThat(nodeAddItem.getLocalName(), is("add_item(HTree, HTree, HTree)"));
		assertThat(nodeAddItem.getSimpleName(), is("add_item"));
		assertThat(nodeAddItem.getNamespace(), is((String) null));

		List<Parameter> nodeAddItemParams = nodeAddItem.getParameters();

		assertThat(nodeAddItemParams.size(), is(3));
		assertThat(nodeAddItemParams.get(0).getName(), is("param1"));
		assertThat(nodeAddItemParams.get(1).getName(), is("param2[]"));
		assertThat(nodeAddItemParams.get(2).getName(), is("*param3"));

		RastNode nodeRemoveItem = programNode.getNodes().get(1);

		assertThat(nodeRemoveItem.getType(), is("FunctionDeclaration"));
		assertThat(nodeRemoveItem.getLocalName(), is("remove_item(HTree)"));
		assertThat(nodeRemoveItem.getSimpleName(), is("remove_item"));
		assertThat(nodeRemoveItem.getNamespace(), is((String) null));

		List<Parameter> nodeRemoveItemParams = nodeRemoveItem.getParameters();

		assertThat(nodeRemoveItemParams.size(), is(1));
		assertThat(nodeRemoveItemParams.get(0).getName(), is("param4"));
	}

	@Test
	public void shouldParseLocation() throws Exception {
		Path basePath = Paths.get("test-data/c/parser");
		List<SourceFile> sourceFiles = Collections.singletonList(new FileSystemSourceFile(basePath, Paths.get("locationIssue.c")));

		RastRoot root = parser.parse(sourceFiles);

		assertThat(root.getNodes().size(), is(1));

		RastNode program = root.getNodes().get(0);

		assertThat(program.getLocation().getBegin(), is(0));
		assertThat(program.getLocation().getEnd(), is(631));

		RastNode nodeF1 = program.getNodes().get(0);

		assertThat(nodeF1.getLocation().getBegin(), is(479));
		assertThat(nodeF1.getLocation().getEnd(), is(528));
		assertThat(nodeF1.getLocation().getBodyBegin(), is(488));
		assertThat(nodeF1.getLocation().getBodyEnd(), is(528));

		RastNode nodeF2 = program.getNodes().get(1);

		assertThat(nodeF2.getLocation().getBegin(), is(530));
		assertThat(nodeF2.getLocation().getEnd(), is(579));
		assertThat(nodeF2.getLocation().getBodyBegin(), is(539));
		assertThat(nodeF2.getLocation().getBodyEnd(), is(579));

		RastNode nodeF3 = program.getNodes().get(2);

		assertThat(nodeF3.getLocation().getBegin(), is(581));
		assertThat(nodeF3.getLocation().getEnd(), is(630));
		assertThat(nodeF3.getLocation().getBodyBegin(), is(590));
		assertThat(nodeF3.getLocation().getBodyEnd(), is(630));
	}

	@Test
	public void shouldParseLocation2() throws Exception {
		Path basePath = Paths.get("test-data/c/parser");
		List<SourceFile> sourceFiles = Collections.singletonList(new FileSystemSourceFile(basePath, Paths.get("locationIssue2.c")));

		RastRoot root = parser.parse(sourceFiles);

		assertThat(root.getNodes().size(), is(1));

		RastNode program = root.getNodes().get(0);

		assertThat(program.getLocation().getBegin(), is(0));
		assertThat(program.getLocation().getEnd(), is(1979));
	}

	@Test
	public void shouldParseStructParams() throws Exception {
		Path basePath = Paths.get("test-data/c/parser");
		List<SourceFile> sourceFiles = Collections.singletonList(new FileSystemSourceFile(basePath, Paths.get("structParams.c")));

		RastRoot root = parser.parse(sourceFiles);

		assertThat(root.getNodes().size(), is(1));

		RastNode program = root.getNodes().get(0);

		RastNode functionNode = program.getNodes().get(0);

		assertThat(functionNode.getSimpleName(), is("__init_swait_queue_head"));
		assertThat(functionNode.getLocalName(), is("__init_swait_queue_head(swait_queue_head, char, lock_class_key)"));

		List<Parameter> parameters = functionNode.getParameters();

		assertThat(parameters.size(), is(3));
		assertThat(parameters.get(0).getName(), is("*q"));
		assertThat(parameters.get(1).getName(), is("*name"));
		assertThat(parameters.get(2).getName(), is("*key"));
	}

	@Test
	public void shouldParseArrayModifier() throws Exception {
		Path basePath = Paths.get("test-data/c/parser");
		List<SourceFile> sourceFiles = Collections.singletonList(new FileSystemSourceFile(basePath, Paths.get("arrayModifier.c")));

		try {
			parser.parse(sourceFiles);	
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("Should have not thrown exception");
		}
	}

	@Test
	public void shouldParseFunctionWithNoName() throws Exception {
		Path basePath = Paths.get("test-data/c/parser");
		List<SourceFile> sourceFiles = Collections.singletonList(new FileSystemSourceFile(basePath, Paths.get("functionWithNoName.c")));

		try {
			parser.parse(sourceFiles);	
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("Should have not thrown exception");
		}
	}

	@Test
	public void shouldParseFunctionWithStructReturn() throws Exception {
		Path basePath = Paths.get("test-data/c/parser");
		List<SourceFile> sourceFiles = Collections.singletonList(new FileSystemSourceFile(basePath, Paths.get("functionWithStructReturn.c")));

		RastRoot root = parser.parse(sourceFiles);

		assertThat(root.getNodes().size(), is(1));
		
		RastNode program = root.getNodes().get(0);

		RastNode functionNode = program.getNodes().get(0);
		assertThat(functionNode.getLocalName(), is("Curl_pgrsLimitWaitTime(curl_off_t, curl_off_t, curl_off_t, curltime, curltime)"));
	}
	
	@Test
	public void shouldParseFunctionWithStructAndPointerReturn() throws Exception {
		Path basePath = Paths.get("test-data/c/parser");
		List<SourceFile> sourceFiles = Collections.singletonList(
				new FileSystemSourceFile(basePath, Paths.get("functionWithStructAndPointerReturn.c")));

		RastRoot root = parser.parse(sourceFiles);

		assertThat(root.getNodes().size(), is(1));
		
		RastNode program = root.getNodes().get(0);

		RastNode functionNode = program.getNodes().get(0);
		assertThat(functionNode.getLocalName(), is("Curl_cookie_add(Curl_easy, CookieInfo, bool, bool, char, char, char)"));
	}
}
