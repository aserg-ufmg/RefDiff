package refdiff.parsers.js;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import refdiff.core.rast.RastNode;
import refdiff.core.rast.Stereotype;

public abstract class EsprimaNodeHandler {

    public abstract String getLogicalName(RastNode rastNode, ScriptObjectMirror esprimaNode);

    public abstract Set<Stereotype> getStereotypes(RastNode rastNode, ScriptObjectMirror esprimaNode);

    static final Map<String, EsprimaNodeHandler> HANDLERS = new HashMap<>();
    
    static {
        HANDLERS.put("Program", new EsprimaNodeHandler() {
            public String getLogicalName(RastNode rastNode, ScriptObjectMirror esprimaNode) {
                return rastNode.location.file;
            }
            public Set<Stereotype> getStereotypes(RastNode rastNode, ScriptObjectMirror esprimaNode) {
                return Collections.singleton(Stereotype.HAS_BODY);
            }
        });
        
        HANDLERS.put("ArrowFunctionExpression", new EsprimaNodeHandler() {
            public String getLogicalName(RastNode rastNode, ScriptObjectMirror esprimaNode) {
                return "";
            }
            public Set<Stereotype> getStereotypes(RastNode rastNode, ScriptObjectMirror esprimaNode) {
                return Collections.singleton(Stereotype.HAS_BODY);
            }
        });
        
        HANDLERS.put("FunctionDeclaration", new EsprimaNodeHandler() {
            public String getLogicalName(RastNode rastNode, ScriptObjectMirror esprimaNode) {
                Object identifier = esprimaNode.getMember("id");
                if (identifier instanceof ScriptObjectMirror) {
                    return ((ScriptObjectMirror) identifier).getMember("name").toString();
                } else {
                    return "";
                }
            }
            public Set<Stereotype> getStereotypes(RastNode rastNode, ScriptObjectMirror esprimaNode) {
                return Collections.singleton(Stereotype.HAS_BODY);
            }
        });
    }
}
