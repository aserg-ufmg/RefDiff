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

abstract class EsprimaNodeHandler {
	
	public abstract String getLocalName(RastNode rastNode, JsValue esprimaNode);
	
	public String getSimpleName(RastNode rastNode, JsValue esprimaNode) {
		return getLocalName(rastNode, esprimaNode);
	}
	
	public String getNamespace(RastNode rastNode, JsValue esprimaNode) {
		return null;
	}
	
	public abstract Set<Stereotype> getStereotypes(RastNode rastNode, JsValue esprimaNode);
	
	public List<Parameter> getParameters(RastNode rastNode, JsValue esprimaNode) {
		return Collections.emptyList();
	}
	
	protected List<Parameter> extractParameters(JsValue nodeWithParams) {
		JsValue params = nodeWithParams.get("params");
		if (params.isArray()) {
			List<Parameter> parameters = new ArrayList<>(params.size());
			for (int i = 0; i < params.size(); i++) {
				JsValue param = params.get(i);
				parameters.add(new Parameter(param.get("name").asString()));
			}
			return parameters;
		}
		return Collections.emptyList();
	}
	
	static final Map<String, EsprimaNodeHandler> RAST_NODE_HANDLERS = new HashMap<>();
	
	static {
		RAST_NODE_HANDLERS.put("Program", new EsprimaNodeHandler() {
			public String getLocalName(RastNode rastNode, JsValue esprimaNode) {
				return rastNode.getLocation().getFile();
			}
			
			public String getNamespace(RastNode rastNode, JsValue esprimaNode) {
				String filePath = rastNode.getLocation().getFile();
				if (filePath.lastIndexOf('/') != -1) {
					return filePath.substring(0, filePath.lastIndexOf('/') + 1);
				} else {
					return "";
				}
			}
			
			public Set<Stereotype> getStereotypes(RastNode rastNode, JsValue esprimaNode) {
				return Collections.singleton(Stereotype.HAS_BODY);
			}
		});
		
		RAST_NODE_HANDLERS.put("ArrowFunctionExpression", new EsprimaNodeHandler() {
			public String getLocalName(RastNode rastNode, JsValue esprimaNode) {
				return "";
			}
			
			public Set<Stereotype> getStereotypes(RastNode rastNode, JsValue esprimaNode) {
				return Collections.singleton(Stereotype.HAS_BODY);
			}
			
			@Override
			public List<Parameter> getParameters(RastNode rastNode, JsValue esprimaNode) {
				return extractParameters(esprimaNode);
			}
		});
		
		RAST_NODE_HANDLERS.put("FunctionDeclaration", new EsprimaNodeHandler() {
			public String getLocalName(RastNode rastNode, JsValue esprimaNode) {
				return esprimaNode.get("id").get("name").asString();
			}
			
			public Set<Stereotype> getStereotypes(RastNode rastNode, JsValue esprimaNode) {
				return Collections.singleton(Stereotype.HAS_BODY);
			}
			
			@Override
			public List<Parameter> getParameters(RastNode rastNode, JsValue esprimaNode) {
				return extractParameters(esprimaNode);
			}
		});
		
		RAST_NODE_HANDLERS.put("ClassDeclaration", new EsprimaNodeHandler() {
			public String getLocalName(RastNode rastNode, JsValue esprimaNode) {
				return esprimaNode.get("id").get("name").asString();
			}
			
			public Set<Stereotype> getStereotypes(RastNode rastNode, JsValue esprimaNode) {
				return Collections.emptySet();
			}
		});
		
		RAST_NODE_HANDLERS.put("MethodDefinition", new EsprimaNodeHandler() {
			public String getLocalName(RastNode rastNode, JsValue esprimaNode) {
				return esprimaNode.get("key").get("name").asString();
			}
			
			public Set<Stereotype> getStereotypes(RastNode rastNode, JsValue esprimaNode) {
				if (esprimaNode.get("kind").asString().equals("method")) {
					return Collections.singleton(Stereotype.TYPE_MEMBER);
				} else {
					return Collections.emptySet();
				}
			}
			
			@Override
			public List<Parameter> getParameters(RastNode rastNode, JsValue esprimaNode) {
				return extractParameters(esprimaNode.get("value"));
			}
			
		});
	}
}
