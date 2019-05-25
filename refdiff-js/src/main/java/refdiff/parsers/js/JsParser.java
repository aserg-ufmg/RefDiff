package refdiff.parsers.js;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8Object;

import refdiff.core.io.FilePathFilter;
import refdiff.core.io.SourceFile;
import refdiff.core.io.SourceFileSet;
import refdiff.core.rast.HasChildrenNodes;
import refdiff.core.rast.Location;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.RastNodeRelationship;
import refdiff.core.rast.RastNodeRelationshipType;
import refdiff.core.rast.RastRoot;
import refdiff.core.rast.TokenPosition;
import refdiff.core.rast.TokenizedSource;
import refdiff.parsers.RastParser;

public class JsParser implements RastParser, Closeable {
	
	private NodeJS nodeJs;
	private int nodeCounter = 0;
	private File nodeModules;
	private V8Object babel;
	
	public JsParser() throws Exception {
		this.nodeJs = NodeJS.createNodeJS();
		URL nodeModulesUrl = this.getClass().getClassLoader().getResource("node_modules");
		nodeModules = new File(nodeModulesUrl.getFile());
		this.babel = this.nodeJs.require(new File(nodeModules, "@babel/parser"));
		
		this.nodeJs.getRuntime().add("babelParser", this.babel);
		
		String plugins = "['jsx', 'objectRestSpread', 'exportDefaultFrom', 'exportNamespaceFrom', 'classProperties', 'flow', 'dynamicImport', 'decorators', 'optionalCatchBinding']";
		
		this.nodeJs.getRuntime().executeVoidScript("function parse(script) {return babelParser.parse(script, {ranges: true, tokens: true, sourceType: 'unambiguous', allowImportExportEverywhere: true, allowReturnOutsideFunction: true, plugins: " + plugins + " });}");
		this.nodeJs.getRuntime().executeVoidScript("function toJson(object) {return JSON.stringify(object);}");
	}
	
	@Override
	public RastRoot parse(SourceFileSet sources) throws Exception {
		try {
			RastRoot root = new RastRoot();
			this.nodeCounter = 0;
			for (SourceFile sourceFile : sources.getSourceFiles()) {
				String content = sources.readContent(sourceFile);
				getRast(root, sourceFile, content, sources);
			}
			return root;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void getRast(RastRoot root, SourceFile sourceFile, String content, SourceFileSet sources) throws Exception {
		try {
			V8Object babelAst = (V8Object) this.nodeJs.getRuntime().executeJSFunction("parse", content);
			
			// System.out.print(String.format("Parsing %s ... ", sources.describeLocation(sourceFile)));
			// long timestamp = System.currentTimeMillis();
			try (JsValueV8 astRoot = new JsValueV8(babelAst, this::toJson)) {
				
				TokenizedSource tokenizedSource = buildTokenizedSourceFromAst(sourceFile, astRoot);
				root.addTokenizedFile(tokenizedSource);
				
				// System.out.println(String.format("Done in %d ms", System.currentTimeMillis() - timestamp));
				Map<String, Set<RastNode>> callerMap = new HashMap<>();
				getRast(0, root, sourceFile, content, astRoot, callerMap);
				
				root.forEachNode((calleeNode, depth) -> {
					if (calleeNode.getType().equals(JsNodeType.FUNCTION) && callerMap.containsKey(calleeNode.getLocalName())) {
						Set<RastNode> callerNodes = callerMap.get(calleeNode.getLocalName());
						for (RastNode callerNode : callerNodes) {
							root.getRelationships().add(new RastNodeRelationship(RastNodeRelationshipType.USE, callerNode.getId(), calleeNode.getId()));
						}
					}
				});
			}
			
		} catch (Exception e) {
			throw new RuntimeException(String.format("Error parsing %s: %s", sources.describeLocation(sourceFile), e.getMessage()), e);
		}
	}
	
	private TokenizedSource buildTokenizedSourceFromAst(SourceFile sourceFile, JsValueV8 astRoot) {
		JsValueV8 tokensArray = astRoot.get("tokens");
		
		List<TokenPosition> tokens = new ArrayList<>();
		for (int i = 0; i < tokensArray.size(); i++) {
			JsValueV8 tokenObj = tokensArray.get(i);
			int start = tokenObj.get("start").asInt();
			int end = tokenObj.get("end").asInt();
			if (end > start) {
				tokens.add(new TokenPosition(start, end));
			}
		}
		TokenizedSource tokenizedSource = new TokenizedSource(sourceFile.getPath(), tokens);
		return tokenizedSource;
	}
	
	private void getRast(int depth, HasChildrenNodes container, SourceFile sourceFile, String fileContent, JsValueV8 babelAst, Map<String, Set<RastNode>> callerMap) throws Exception {
		if (!babelAst.has("type")) {
			throw new RuntimeException("object is not an AST node");
		}
		String path = sourceFile.getPath();
		String type = babelAst.get("type").asString();
		List<JsValueV8> children = null;
		
		if (BabelNodeHandler.RAST_NODE_HANDLERS.containsKey(type)) {
			BabelNodeHandler handler = BabelNodeHandler.RAST_NODE_HANDLERS.get(type);
			
			if (handler.isRastNode(babelAst)) {
				JsValueV8 mainNode = handler.getMainNode(babelAst);
				
				int begin = mainNode.get("start").asInt();
				int end = mainNode.get("end").asInt();
				int bodyBegin = begin;
				int bodyEnd = end;
				
				RastNode rastNode = new RastNode(++nodeCounter);
				rastNode.setType(handler.getType(babelAst));
				JsValueV8 bodyNode = handler.getBodyNode(babelAst);
				if (bodyNode.isDefined()) {
					if (bodyNode.has("range")) {
						bodyBegin = bodyNode.get("start").asInt();
						bodyEnd = bodyNode.get("end").asInt();
						if (bodyNode.get("type").asString().equals("BlockStatement")) {
							bodyBegin = bodyBegin + 1;
							bodyEnd = bodyEnd - 1;
						}
					}
				}
				
				rastNode.setLocation(Location.of(path, begin, end, bodyBegin, bodyEnd, fileContent));
				rastNode.setLocalName(handler.getLocalName(rastNode, babelAst));
				rastNode.setSimpleName(handler.getSimpleName(rastNode, babelAst));
				rastNode.setNamespace(handler.getNamespace(rastNode, babelAst));
				rastNode.setStereotypes(handler.getStereotypes(rastNode, babelAst));
				rastNode.setParameters(handler.getParameters(rastNode, babelAst));
				container.addNode(rastNode);
				container = rastNode;
				children = Collections.singletonList(bodyNode);
			}
		} else if ("CallExpression".equals(type)) {
			extractCalleeNameFromCallExpression(babelAst, callerMap, (RastNode) container);
		}
		
		if (children == null) {
			children = new ArrayList<>();
			for (String key : babelAst.getOwnKeys()) {
				if (!key.equals("tokens")) {
					children.add(babelAst.get(key));
				}
			}
		}
		
		for (JsValueV8 value : children) {
			if (value.isObject()) {
				if (value.has("type")) {
					getRast(depth + 1, container, sourceFile, fileContent, value, callerMap);
				}
			}
			if (value.isArray()) {
				for (int i = 0; i < value.size(); i++) {
					JsValueV8 element = value.get(i);
					if (element.has("type")) {
						getRast(depth + 1, container, sourceFile, fileContent, element, callerMap);
					}
				}
			}
		}
	}
	
	private void extractCalleeNameFromCallExpression(JsValueV8 callExpresionNode, Map<String, Set<RastNode>> callerMap, RastNode container) {
		JsValueV8 callee = callExpresionNode.get("callee");
		if (callee.get("type").asString().equals("MemberExpression")) {
			JsValueV8 property = callee.get("property");
			if (property.get("type").asString().equals("Identifier")) {
				String calleeName = property.get("name").asString();
				callerMap.computeIfAbsent(calleeName, key -> new HashSet<>()).add(container);
			} else {
				// callee is a complex expression, not an identifier
			}
		} else if (callee.get("type").asString().equals("Identifier")) {
			String calleeName = callee.get("name").asString();
			callerMap.computeIfAbsent(calleeName, key -> new HashSet<>()).add(container);
		} else {
			// callee is a complex expression, not an identifier
		}
	}
	
	private String toJson(Object object) {
		return this.nodeJs.getRuntime().executeJSFunction("toJson", object).toString();
	}
	
	@Override
	public FilePathFilter getAllowedFilesFilter() {
		return new FilePathFilter(Arrays.asList(".js", ".jsx"), Arrays.asList(".min.js"));
	}
	
	@Override
	public void close() throws IOException {
		this.babel.release();
		//this.nodeJs.getRuntime().release(true);
		this.nodeJs.release();
	}
}
