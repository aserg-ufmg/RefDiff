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
	
	public String getSimpleName(RastNode rastNode, JsValueV8 esprimaNode) {
		return getLocalName(rastNode, esprimaNode);
	}
	
	public String getNamespace(RastNode rastNode, JsValueV8 esprimaNode) {
		return null;
	}
	
	public abstract Set<Stereotype> getStereotypes(RastNode rastNode, JsValueV8 esprimaNode);
	
	public List<Parameter> getParameters(RastNode rastNode, JsValueV8 esprimaNode) {
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
				return rastNode.getLocation().getFile();
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
		});
		
		RAST_NODE_HANDLERS.put("ClassDeclaration", new BabelNodeHandler() {
			public String getLocalName(RastNode rastNode, JsValueV8 esprimaNode) {
				return esprimaNode.get("id").get("name").asString();
			}
			
			public Set<Stereotype> getStereotypes(RastNode rastNode, JsValueV8 esprimaNode) {
				return Collections.emptySet();
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
			
		});
	}
}
