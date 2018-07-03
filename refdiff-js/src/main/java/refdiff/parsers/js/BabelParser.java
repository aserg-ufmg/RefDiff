package refdiff.parsers.js;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import refdiff.parsers.RastParser;
import refdiff.parsers.SourceTokenizer;

public class BabelParser implements RastParser, SourceTokenizer, Closeable {
	
	private NodeJS nodeJs;
	private int nodeCounter = 0;
	private File nodeModules = new File("node_modules");
	private V8Object babel;
	
	public BabelParser() throws Exception {
		this.nodeJs = NodeJS.createNodeJS();
		this.babel = this.nodeJs.require(new File(nodeModules, "@babel/parser"));
		
		this.nodeJs.getRuntime().add("babelParser", this.babel);
		
		this.nodeJs.getRuntime().executeVoidScript("function parse(script) {return babelParser.parse(script, {ranges: true, sourceType: 'unambiguous'});}"
			+ "function tokenize(source) {return babelParser.parse(source, {tokens: true}).tokens;}"
			+ "function toJson(object) {return JSON.stringify(object);}");
	}
	
	@Override
	public RastRoot parse(SourceFileSet sources) throws Exception {
		RastRoot root = new RastRoot();
		this.nodeCounter = 0;
		for (SourceFile sourceFile : sources.getSourceFiles()) {
			String content = sources.readContent(sourceFile);
			getRast(root, sourceFile, content, sources);
		}
		return root;
	}
	
	@Override
	public List<String> tokenize(String source) {
		try {
			
			JsValueV8 babelAst = new JsValueV8(this.nodeJs.getRuntime().executeJSFunction("tokenize", source), this::toJson);
			
			//ScriptObjectMirror array = (ScriptObjectMirror) 
			List<String> tokens = new ArrayList<>();
//			for (int i = 0; i < array.size(); i++) {
//				String token = ((ScriptObjectMirror) array.getSlot(i)).getMember("value").toString();
//				tokens.add(token);
//			}
			return tokens;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void getRast(RastRoot root, SourceFile sourceFile, String content, SourceFileSet sources) throws Exception {
		try {
//			System.out.print(String.format("Parsing %s ... ", sources.describeLocation(sourceFile)));
//			long timestamp = System.currentTimeMillis();
			V8Object babelAst = (V8Object) this.nodeJs.getRuntime().executeJSFunction("parse", content);
//			System.out.println(String.format("Done in %d ms", System.currentTimeMillis() - timestamp));
			JsValueV8 astRoot = new JsValueV8(babelAst, this::toJson);
			Map<String, Object> callerMap = new HashMap<>();
			getRast(0, root, sourceFile, astRoot, callerMap);
			root.forEachNode((calleeNode, depth) -> {
				if (calleeNode.getType().equals("FunctionDeclaration") && callerMap.containsKey(calleeNode.getLocalName())) {
					RastNode callerNode = (RastNode) callerMap.get(calleeNode.getLocalName());
					root.getRelationships().add(new RastNodeRelationship(RastNodeRelationshipType.USE, callerNode.getId(), calleeNode.getId()));
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(String.format("Error parsing %s: %s", sources.describeLocation(sourceFile), e.getMessage()), e);
		}
	}
	
	private void getRast(int depth, HasChildrenNodes container, SourceFile sourceFile, JsValueV8 babelAst, Map<String, Object> callerMap) throws Exception {
		if (!babelAst.has("type")) {
			throw new RuntimeException("object is not an AST node");
		}
		String path = sourceFile.getPath();
		String type = babelAst.get("type").asString();
		int begin = babelAst.get("start").asInt();
		int end = babelAst.get("end").asInt();
		int bodyBegin = begin;
		int bodyEnd = end;
		if (babelAst.has("body")) {
			JsValueV8 body = babelAst.get("body");
			if (body.has("range")) {
				bodyBegin = body.get("start").asInt();
				bodyEnd = body.get("end").asInt();
				if (body.get("type").asString().equals("BlockStatement")) {
					bodyBegin = bodyBegin + 1;
					bodyEnd = bodyEnd - 1;
				}
			}
		}
		
		if (BabelNodeHandler.RAST_NODE_HANDLERS.containsKey(type)) {
			BabelNodeHandler handler = BabelNodeHandler.RAST_NODE_HANDLERS.get(type);
			RastNode rastNode = new RastNode(++nodeCounter);
			rastNode.setType(type);
			rastNode.setLocation(new Location(path, begin, end, bodyBegin, bodyEnd));
			rastNode.setLocalName(handler.getLocalName(rastNode, babelAst));
			rastNode.setSimpleName(handler.getSimpleName(rastNode, babelAst));
			rastNode.setNamespace(handler.getNamespace(rastNode, babelAst));
			rastNode.setStereotypes(handler.getStereotypes(rastNode, babelAst));
			rastNode.setParameters(handler.getParameters(rastNode, babelAst));
			container.addNode(rastNode);
			container = rastNode;
		} else {
			if ("CallExpression".equals(type)) {
				extractCalleeNameFromCallExpression(babelAst, callerMap, container);
			}
		}
		
		for (String key : babelAst.getOwnKeys()) {
			JsValueV8 value = babelAst.get(key);
			if (value.isObject()) {
				if (value.has("type")) {
					getRast(depth + 1, container, sourceFile, value, callerMap);
				}
			}
			if (value.isArray()) {
				for (int i = 0; i < value.size(); i++) {
					JsValueV8 element = value.get(i);
					if (element.has("type")) {
						getRast(depth + 1, container, sourceFile, element, callerMap);
					}
				}
			}
		}
	}

	private void extractCalleeNameFromCallExpression(JsValueV8 callExpresionNode, Map<String, Object> callerMap, HasChildrenNodes container) {
		JsValueV8 callee = callExpresionNode.get("callee");
		if (callee.get("type").asString().equals("MemberExpression")) {
			JsValueV8 property = callee.get("property");
			if (property.get("type").asString().equals("Identifier")) {
				String calleeName = property.get("name").asString();
				callerMap.put(calleeName, container);
			} else {
				// callee is a complex expression, not an identifier
			}
		} else if (callee.get("type").asString().equals("Identifier")) {
			String calleeName = callee.get("name").asString();
			callerMap.put(calleeName, container);
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
		this.babel = null;
		this.nodeJs.release();
		this.nodeJs = null;
	}
}
