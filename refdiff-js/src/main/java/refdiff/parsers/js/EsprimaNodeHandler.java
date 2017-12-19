package refdiff.parsers.js;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
		});
		
		RAST_NODE_HANDLERS.put("FunctionDeclaration", new EsprimaNodeHandler() {
			public String getLocalName(RastNode rastNode, JsValue esprimaNode) {
				return esprimaNode.get("id").get("name").asString();
			}
			
			public Set<Stereotype> getStereotypes(RastNode rastNode, JsValue esprimaNode) {
				return Collections.singleton(Stereotype.HAS_BODY);
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
				return Collections.emptySet();
			}
		});
	}
}
