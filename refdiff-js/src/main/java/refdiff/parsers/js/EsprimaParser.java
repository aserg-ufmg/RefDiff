package refdiff.parsers.js;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
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

public class EsprimaParser implements RastParser, SourceTokenizer {
	
	private Invocable invocableScript;
	private int nodeCounter = 0;
	
	public EsprimaParser() throws Exception {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
		
		engine.eval("load('classpath:esprima.js');");
		engine.eval("function parse(script) {return esprima.parseModule(script, {range: true, jsx: true});}");
		engine.eval("function tokenize(source) {return esprima.tokenize(source, {comment: true});}");
		engine.eval("function toJson(object) {return JSON.stringify(object);}");
		this.invocableScript = (Invocable) engine;
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
			ScriptObjectMirror array = (ScriptObjectMirror) this.invocableScript.invokeFunction("tokenize", source);
			List<String> tokens = new ArrayList<>(array.size());
			for (int i = 0; i < array.size(); i++) {
				String token = ((ScriptObjectMirror) array.getSlot(i)).getMember("value").toString();
				tokens.add(token);
			}
			return tokens;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void getRast(RastRoot root, SourceFile sourceFile, String content, SourceFileSet sources) throws Exception {
		try {
//			System.out.print(String.format("Parsing %s ... ", sources.describeLocation(sourceFile)));
//			long timestamp = System.currentTimeMillis();
			ScriptObjectMirror esprimaAst = (ScriptObjectMirror) this.invocableScript.invokeFunction("parse", content);
//			System.out.println(String.format("Done in %d ms", System.currentTimeMillis() - timestamp));
			JsValue astRoot = new JsValue(esprimaAst, this::toJson);
			Map<String, Object> callerMap = new HashMap<>();
			getRast(0, root, sourceFile, astRoot, callerMap);
			root.forEachNode((calleeNode, depth) -> {
				if (calleeNode.getType().equals("FunctionDeclaration") && callerMap.containsKey(calleeNode.getLocalName())) {
					RastNode callerNode = (RastNode) callerMap.get(calleeNode.getLocalName());
					root.getRelationships().add(new RastNodeRelationship(RastNodeRelationshipType.USE, callerNode.getId(), calleeNode.getId()));
				}
			});
		} catch (ScriptException e) {
			throw new RuntimeException(String.format("Error parsing %s: %s", sources.describeLocation(sourceFile), e.getMessage()));
		}
	}
	
	private void getRast(int depth, HasChildrenNodes container, SourceFile sourceFile, JsValue esprimaAst, Map<String, Object> callerMap) throws Exception {
		if (!esprimaAst.has("type")) {
			throw new RuntimeException("object is not an AST node");
		}
		String path = sourceFile.getPath();
		String type = esprimaAst.get("type").asString();
		JsValue range = esprimaAst.get("range");
		int begin = range.get(0).asInt();
		int end = range.get(1).asInt();
		int bodyBegin = begin;
		int bodyEnd = end;
		if (esprimaAst.has("body")) {
			JsValue body = esprimaAst.get("body");
			if (body.has("range")) {
				JsValue bodyRange = body.get("range");
				bodyBegin = bodyRange.get(0).asInt();
				bodyEnd = bodyRange.get(1).asInt();
			}
		}
		
		if (EsprimaNodeHandler.RAST_NODE_HANDLERS.containsKey(type)) {
			EsprimaNodeHandler handler = EsprimaNodeHandler.RAST_NODE_HANDLERS.get(type);
			RastNode rastNode = new RastNode(++nodeCounter);
			rastNode.setType(type);
			rastNode.setLocation(new Location(path, begin, end, bodyBegin, bodyEnd));
			rastNode.setLocalName(handler.getLocalName(rastNode, esprimaAst));
			rastNode.setSimpleName(handler.getSimpleName(rastNode, esprimaAst));
			rastNode.setNamespace(handler.getNamespace(rastNode, esprimaAst));
			rastNode.setStereotypes(handler.getStereotypes(rastNode, esprimaAst));
			rastNode.setParameters(handler.getParameters(rastNode, esprimaAst));
			container.addNode(rastNode);
			container = rastNode;
		} else {
			if ("CallExpression".equals(type)) {
				JsValue callee = esprimaAst.get("callee");
				if (callee.get("type").asString().equals("MemberExpression")) {
					String calleeName = callee.get("property").get("name").asString();
					callerMap.put(calleeName, container);
				} else if (callee.get("type").asString().equals("Identifier")) {
					String calleeName = callee.get("name").asString();
					callerMap.put(calleeName, container);
				} else {
					// callee is a complex expression, not an identifier
				}
			}
		}
		
		for (String key : esprimaAst.getOwnKeys()) {
			JsValue value = esprimaAst.get(key);
			if (value.isObject()) {
				if (value.has("type")) {
					getRast(depth + 1, container, sourceFile, value, callerMap);
				}
			}
			if (value.isArray()) {
				for (int i = 0; i < value.size(); i++) {
					JsValue element = value.get(i);
					if (element.has("type")) {
						getRast(depth + 1, container, sourceFile, element, callerMap);
					}
				}
			}
		}
	}
	
	private String toJson(Object object) {
		try {
			return this.invocableScript.invokeFunction("toJson", object).toString();
		} catch (NoSuchMethodException | ScriptException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List<String> getAllowedFileExtensions() {
		return Arrays.asList(".js");
	}
}
