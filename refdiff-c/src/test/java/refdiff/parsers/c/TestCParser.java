package refdiff.parsers.c;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import refdiff.core.io.SourceFolder;
import refdiff.core.cst.Parameter;
import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstNodeRelationship;
import refdiff.core.cst.CstNodeRelationshipType;
import refdiff.core.cst.CstRoot;

public class TestCParser {

	private CPlugin parser = new CPlugin();

	@Test
	public void shouldParseSimpleFile() throws Exception {
		Path basePath = Paths.get("test-data/c/parser");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("dir1/hello.c"));
		CstRoot root = parser.parse(sources);

		assertThat(root.getNodes().size(), is(1));

		CstNode nodeScriptEx2 = root.getNodes().get(0);
		assertThat(nodeScriptEx2.getType(), is("Program"));
		assertThat(nodeScriptEx2.getLocalName(), is("hello.c"));
		assertThat(nodeScriptEx2.getSimpleName(), is("hello.c"));
		assertThat(nodeScriptEx2.getNamespace(), is("dir1/"));

		assertThat(nodeScriptEx2.getNodes().size(), is(3));
		CstNode nodeF1 = nodeScriptEx2.getNodes().get(0);
		CstNode nodeF2 = nodeScriptEx2.getNodes().get(1);
		CstNode nodeMain = nodeScriptEx2.getNodes().get(2);

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

		Set<CstNodeRelationship> relationships = root.getRelationships();
		assertThat(relationships.size(), is(2));
		assertThat(relationships, hasItem(rel(CstNodeRelationshipType.USE, nodeF2, nodeF1)));
		assertThat(relationships, hasItem(rel(CstNodeRelationshipType.USE, nodeMain, nodeF2)));
	}

	private CstNodeRelationship rel(CstNodeRelationshipType type, CstNode n1, CstNode n2) {
		return new CstNodeRelationship(type, n1.getId(), n2.getId());
	}

	@Test
	public void shouldParseParameters() throws Exception {
		Path basePath = Paths.get("test-data/c/parser");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("file1.c"));
		
		CstRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(1));

		CstNode programNode = root.getNodes().get(0);
		assertThat(programNode.getType(), is("Program"));
		assertThat(programNode.getLocalName(), is("file1.c"));
		assertThat(programNode.getSimpleName(), is("file1.c"));
		assertThat(programNode.getNamespace(), is(""));

		assertThat(programNode.getNodes().size(), is(2));

		CstNode nodeAddItem = programNode.getNodes().get(0);

		assertThat(nodeAddItem.getType(), is("FunctionDeclaration"));
		assertThat(nodeAddItem.getLocalName(), is("add_item(HTree, HTree, HTree)"));
		assertThat(nodeAddItem.getSimpleName(), is("add_item"));
		assertThat(nodeAddItem.getNamespace(), is((String) null));

		List<Parameter> nodeAddItemParams = nodeAddItem.getParameters();

		assertThat(nodeAddItemParams.size(), is(3));
		assertThat(nodeAddItemParams.get(0).getName(), is("param1"));
		assertThat(nodeAddItemParams.get(1).getName(), is("param2[]"));
		assertThat(nodeAddItemParams.get(2).getName(), is("*param3"));

		CstNode nodeRemoveItem = programNode.getNodes().get(1);

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
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("locationIssue.c"));
		
		CstRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(1));

		CstNode program = root.getNodes().get(0);

		assertThat(program.getLocation().getBegin(), is(0));
		assertThat(program.getLocation().getEnd(), is(631));

		CstNode nodeF1 = program.getNodes().get(0);

		assertThat(nodeF1.getLocation().getBegin(), is(479));
		assertThat(nodeF1.getLocation().getEnd(), is(528));
		assertThat(nodeF1.getLocation().getBodyBegin(), is(489));
		assertThat(nodeF1.getLocation().getBodyEnd(), is(527));

		CstNode nodeF2 = program.getNodes().get(1);

		assertThat(nodeF2.getLocation().getBegin(), is(530));
		assertThat(nodeF2.getLocation().getEnd(), is(579));
		assertThat(nodeF2.getLocation().getBodyBegin(), is(540));
		assertThat(nodeF2.getLocation().getBodyEnd(), is(578));

		CstNode nodeF3 = program.getNodes().get(2);

		assertThat(nodeF3.getLocation().getBegin(), is(581));
		assertThat(nodeF3.getLocation().getEnd(), is(630));
		assertThat(nodeF3.getLocation().getBodyBegin(), is(591));
		assertThat(nodeF3.getLocation().getBodyEnd(), is(629));
	}

	@Test
	public void shouldParseLocation2() throws Exception {
		Path basePath = Paths.get("test-data/c/parser");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("locationIssue2.c"));
		
		CstRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(1));

		CstNode program = root.getNodes().get(0);

		assertThat(program.getLocation().getBegin(), is(0));
		assertThat(program.getLocation().getEnd(), is(1979));
	}

	@Test
	public void shouldParseStructParams() throws Exception {
		Path basePath = Paths.get("test-data/c/parser");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("structParams.c"));
		
		CstRoot root = parser.parse(sources);
		
		assertThat(root.getNodes().size(), is(1));

		CstNode program = root.getNodes().get(0);

		CstNode functionNode = program.getNodes().get(0);

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
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("arrayModifier.c"));
		
		try {
			parser.parse(sources);	
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("Should have not thrown exception");
		}
	}

	@Test
	public void shouldParseFunctionWithNoName() throws Exception {
		Path basePath = Paths.get("test-data/c/parser");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("functionWithNoName.c"));
		
		try {
			parser.parse(sources);	
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("Should have not thrown exception");
		}
	}

	@Test
	public void shouldParseFunctionWithStructReturn() throws Exception {
		Path basePath = Paths.get("test-data/c/parser");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("functionWithStructReturn.c"));

		CstRoot root = parser.parse(sources);

		assertThat(root.getNodes().size(), is(1));
		
		CstNode program = root.getNodes().get(0);

		CstNode functionNode = program.getNodes().get(0);
		assertThat(functionNode.getLocalName(), is("Curl_pgrsLimitWaitTime(curl_off_t, curl_off_t, curl_off_t, curltime, curltime)"));
	}
	
	@Test
	public void shouldParseFunctionWithStructAndPointerReturn() throws Exception {
		Path basePath = Paths.get("test-data/c/parser");
		SourceFolder sources = SourceFolder.from(basePath, Paths.get("functionWithStructAndPointerReturn.c"));

		CstRoot root = parser.parse(sources);

		assertThat(root.getNodes().size(), is(1));
		
		CstNode program = root.getNodes().get(0);

		CstNode functionNode = program.getNodes().get(0);
		assertThat(functionNode.getLocalName(), is("Curl_cookie_add(Curl_easy, CookieInfo, bool, bool, char, char, char)"));
	}
}
