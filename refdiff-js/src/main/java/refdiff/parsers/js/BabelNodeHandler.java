package refdiff.parsers.js;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import refdiff.core.rast.Parameter;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.Stereotype;

abstract class BabelNodeHandler {
	
	public abstract String getLocalName(RastNode rastNode, JsValueV8 esprimaNode);
	
	public boolean isRastNode(JsValueV8 babelAst) {
		return true;
	}
	
	public abstract String getType(JsValueV8 babelAst);
	
	public abstract JsValueV8 getBodyNode(JsValueV8 babelAst);
	
	public String getSimpleName(RastNode rastNode, JsValueV8 babelAst) {
		return getLocalName(rastNode, babelAst);
	}
	
	public String getNamespace(RastNode rastNode, JsValueV8 babelAst) {
		return null;
	}
	
	public abstract Set<Stereotype> getStereotypes(RastNode rastNode, JsValueV8 babelAst);
	
	public List<Parameter> getParameters(RastNode rastNode, JsValueV8 babelAst) {
		return Collections.emptyList();
	}
	
	protected List<Parameter> extractParameters(JsValueV8 nodeWithParams) {
		JsValueV8 params = nodeWithParams.get("params");
		if (params.isArray()) {
			List<Parameter> parameters = new ArrayList<>(params.size());
			for (int i = 0; i < params.size(); i++) {
				JsValueV8 param = params.get(i);
				if (param.get("type").asString().equals("Identifier")) {
					parameters.add(new Parameter(param.get("name").asString()));
				} else {
					//
				}
			}
			return parameters;
		}
		return Collections.emptyList();
	}
	
	static final Map<String, BabelNodeHandler> RAST_NODE_HANDLERS = new HashMap<>();
	
	static {
		RAST_NODE_HANDLERS.put("Program", new BabelNodeHandler() {
			public String getLocalName(RastNode rastNode, JsValueV8 esprimaNode) {
				String filePath = rastNode.getLocation().getFile();
				if (filePath.lastIndexOf('/') != -1) {
					return filePath.substring(filePath.lastIndexOf('/') + 1);
				} else {
					return filePath;
				}
			}
			
			public String getNamespace(RastNode rastNode, JsValueV8 esprimaNode) {
				String filePath = rastNode.getLocation().getFile();
				if (filePath.lastIndexOf('/') != -1) {
					return filePath.substring(0, filePath.lastIndexOf('/') + 1);
				} else {
					return "";
				}
			}
			
			public Set<Stereotype> getStereotypes(RastNode rastNode, JsValueV8 esprimaNode) {
				return Collections.singleton(Stereotype.HAS_BODY);
			}

			@Override
			public JsValueV8 getBodyNode(JsValueV8 esprimaNode) {
				return esprimaNode.get("body");
			}

			@Override
			public String getType(JsValueV8 babelAst) {
				return JsNodeType.FILE;
			}
		});
		
		RAST_NODE_HANDLERS.put("ArrowFunctionExpression", new BabelNodeHandler() {
			public String getLocalName(RastNode rastNode, JsValueV8 esprimaNode) {
				return "";
			}
			
			public Set<Stereotype> getStereotypes(RastNode rastNode, JsValueV8 esprimaNode) {
				return Collections.singleton(Stereotype.HAS_BODY);
			}
			
			@Override
			public List<Parameter> getParameters(RastNode rastNode, JsValueV8 esprimaNode) {
				return extractParameters(esprimaNode);
			}
			
			@Override
			public JsValueV8 getBodyNode(JsValueV8 esprimaNode) {
				return esprimaNode.get("body");
			}

			@Override
			public String getType(JsValueV8 babelAst) {
				return JsNodeType.FUNCTION;
			}
		});
		
		RAST_NODE_HANDLERS.put("FunctionDeclaration", new BabelNodeHandler() {
			public String getLocalName(RastNode rastNode, JsValueV8 esprimaNode) {
				return esprimaNode.get("id").get("name").asString();
			}
			
			public Set<Stereotype> getStereotypes(RastNode rastNode, JsValueV8 esprimaNode) {
				return Collections.singleton(Stereotype.HAS_BODY);
			}
			
			@Override
			public List<Parameter> getParameters(RastNode rastNode, JsValueV8 esprimaNode) {
				return extractParameters(esprimaNode);
			}
			
			@Override
			public JsValueV8 getBodyNode(JsValueV8 esprimaNode) {
				return esprimaNode.get("body");
			}
			
			@Override
			public String getType(JsValueV8 babelAst) {
				return JsNodeType.FUNCTION;
			}
		});
		
		RAST_NODE_HANDLERS.put("VariableDeclarator", new BabelNodeHandler() {
			@Override
			public boolean isRastNode(JsValueV8 babelAst) {
				if (babelAst.has("init")) {
					String expressionType = babelAst.get("init").get("type").asString();
					return "FunctionExpression".equals(expressionType) || "ArrowFunctionExpression".equals(expressionType);
				}
				return false;
			}
			
			@Override
			public String getType(JsValueV8 babelAst) {
				return JsNodeType.FUNCTION;
			}
			
			public String getLocalName(RastNode rastNode, JsValueV8 esprimaNode) {
				return esprimaNode.get("id").get("name").asString();
			}
			
			public Set<Stereotype> getStereotypes(RastNode rastNode, JsValueV8 esprimaNode) {
				return Collections.singleton(Stereotype.HAS_BODY);
			}
			
			@Override
			public List<Parameter> getParameters(RastNode rastNode, JsValueV8 esprimaNode) {
				return extractParameters(esprimaNode.get("init"));
			}
			
			@Override
			public JsValueV8 getBodyNode(JsValueV8 esprimaNode) {
				return esprimaNode.get("init").get("body");
			}
		});
		
		RAST_NODE_HANDLERS.put("ClassDeclaration", new BabelNodeHandler() {
			public String getLocalName(RastNode rastNode, JsValueV8 esprimaNode) {
				return esprimaNode.get("id").get("name").asString();
			}
			
			public Set<Stereotype> getStereotypes(RastNode rastNode, JsValueV8 esprimaNode) {
				return Collections.emptySet();
			}
			
			@Override
			public JsValueV8 getBodyNode(JsValueV8 esprimaNode) {
				return esprimaNode.get("body");
			}
			
			@Override
			public String getType(JsValueV8 babelAst) {
				return JsNodeType.CLASS;
			}
		});
		
		RAST_NODE_HANDLERS.put("ClassMethod", new BabelNodeHandler() {
			public String getLocalName(RastNode rastNode, JsValueV8 esprimaNode) {
				return esprimaNode.get("key").get("name").asString();
			}
			
			public Set<Stereotype> getStereotypes(RastNode rastNode, JsValueV8 esprimaNode) {
				String kind = esprimaNode.get("kind").asString();
				if (kind.equals("method")) {
					return Collections.singleton(Stereotype.TYPE_MEMBER);
				} else if (kind.equals("constructor")) {
					return Collections.singleton(Stereotype.TYPE_CONSTRUCTOR);
				} else {
					return Collections.emptySet();
				}
			}
			
			@Override
			public List<Parameter> getParameters(RastNode rastNode, JsValueV8 esprimaNode) {
				return extractParameters(esprimaNode);
			}
			
			@Override
			public JsValueV8 getBodyNode(JsValueV8 esprimaNode) {
				return esprimaNode.get("body");
			}
			
			@Override
			public String getType(JsValueV8 babelAst) {
				return JsNodeType.FUNCTION;
			}
		});
		
		RAST_NODE_HANDLERS.put("ObjectProperty", new BabelNodeHandler() {
			@Override
			public boolean isRastNode(JsValueV8 babelAst) {
				String keyNodeType = babelAst.get("key").get("type").asString();
				String valueNodeType = babelAst.get("value").get("type").asString();
				boolean hasIdentifier = "Identifier".equals(keyNodeType);
				boolean hasFunctionExpression = "FunctionExpression".equals(valueNodeType) || "ArrowFunctionExpression".equals(valueNodeType);
				return hasIdentifier && hasFunctionExpression;
			}
			
			@Override
			public String getType(JsValueV8 babelAst) {
				return JsNodeType.FUNCTION;
			}
			
			public String getLocalName(RastNode rastNode, JsValueV8 babelAst) {
				return babelAst.get("key").get("name").asString();
			}
			
			public Set<Stereotype> getStereotypes(RastNode rastNode, JsValueV8 babelAst) {
				return Collections.singleton(Stereotype.HAS_BODY);
			}
			
			@Override
			public List<Parameter> getParameters(RastNode rastNode, JsValueV8 babelAst) {
				return extractParameters(babelAst.get("value"));
			}
			
			@Override
			public JsValueV8 getBodyNode(JsValueV8 babelAst) {
				return babelAst.get("value").get("body");
			}
		});
	}
}
