package refdiff.parsers.js;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import refdiff.core.io.SourceFile;
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
		engine.eval("function parse(script) {return esprima.parseModule(script, {range: true});}");
		engine.eval("function tokenize(source) {return esprima.tokenize(source, {comment: true});}");
		this.invocableScript = (Invocable) engine;
	}
	
	@Override
	public RastRoot parse(Set<SourceFile> filesOfInterest) throws Exception {
		RastRoot root = new RastRoot();
		this.nodeCounter = 0;
		for (SourceFile sourceFile : filesOfInterest) {
			String content = sourceFile.getContent();
			getRast(root, sourceFile, content);
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
	
	private void getRast(RastRoot root, SourceFile sourceFile, String content) throws Exception {
		ScriptObjectMirror esprimaAst = (ScriptObjectMirror) this.invocableScript.invokeFunction("parse", content);
		Map<String, Object> callerMap = new HashMap<>();
		getRast(0, root, sourceFile, esprimaAst, callerMap);
		root.forEachNode((calleeNode, depth) -> {
			if (calleeNode.getType().equals("FunctionDeclaration") && callerMap.containsKey(calleeNode.getLocalName())) {
				RastNode callerNode = (RastNode) callerMap.get(calleeNode.getLocalName());
				root.getRelationships().add(new RastNodeRelationship(RastNodeRelationshipType.USE, callerNode.getId(), calleeNode.getId()));
			}
		});
	}
	
	private void getRast(int depth, HasChildrenNodes container, SourceFile sourceFile, ScriptObjectMirror esprimaAst, Map<String, Object> callerMap) throws Exception {
		if (!esprimaAst.hasMember("type")) {
			throw new RuntimeException("object is not an AST node");
		}
		String path = sourceFile.getPath();
		String type = (String) esprimaAst.getMember("type");
		ScriptObjectMirror range = (ScriptObjectMirror) esprimaAst.getMember("range");
		int begin = ((Number) range.getSlot(0)).intValue();
		int end = ((Number) range.getSlot(1)).intValue();
		
		if (EsprimaNodeHandler.RAST_NODE_HANDLERS.containsKey(type)) {
			EsprimaNodeHandler handler = EsprimaNodeHandler.RAST_NODE_HANDLERS.get(type);
			RastNode rastNode = new RastNode(++nodeCounter);
			rastNode.setType(type);
			rastNode.setLocation(new Location(path, begin, end));
			rastNode.setLocalName(handler.getLocalName(rastNode, esprimaAst));
			rastNode.setStereotypes(handler.getStereotypes(rastNode, esprimaAst));
			container.getNodes().add(rastNode);
			container = rastNode;
		} else {
			if ("CallExpression".equals(type)) {
				Object identifier = esprimaAst.getMember("callee");
				String calleeName = ((ScriptObjectMirror) identifier).getMember("name").toString();
				callerMap.put(calleeName, container);
			}
		}
		
		for (String key : esprimaAst.getOwnKeys(true)) {
			Object obj = esprimaAst.getMember(key);
			if (obj instanceof ScriptObjectMirror) {
				ScriptObjectMirror objectOrArray = (ScriptObjectMirror) obj;
				if (objectOrArray.isArray()) {
					for (int i = 0; i < objectOrArray.size(); i++) {
						Object element = objectOrArray.getSlot(i);
						if (element instanceof ScriptObjectMirror) {
							ScriptObjectMirror e = (ScriptObjectMirror) element;
							if (e.hasMember("type")) {
								getRast(depth + 1, container, sourceFile, e, callerMap);
							}
						}
					}
				} else {
					if (objectOrArray.hasMember("type")) {
						getRast(depth + 1, container, sourceFile, objectOrArray, callerMap);
					}
				}
			}
		}
	}
	
}
