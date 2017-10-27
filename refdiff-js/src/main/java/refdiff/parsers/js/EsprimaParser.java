package refdiff.parsers.js;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import refdiff.parsers.FileContentReader;
import refdiff.parsers.RastParser;
import refdiff.rast.Location;
import refdiff.rast.RastNode;
import refdiff.rast.RastRoot;

public class EsprimaParser implements RastParser {

    private Invocable invocableScript;
    private int nodeCounter = 0;

    public EsprimaParser() throws Exception {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.eval("load('classpath:esprima.js');");
        engine.eval("function parse(script) {return esprima.parseModule(script, {range: true});}");
        this.invocableScript = (Invocable) engine;
    }

    public static void main(String[] args) throws Exception {
        EsprimaParser parser = new EsprimaParser();
        Set<String> files = Collections.singleton("hello.js");
        FileContentReader contentReader = new FileContentReader();
        RastRoot root = parser.parse(files, contentReader);

        root.forEachNode((node, depth) -> {
            for (int i = 0; i < depth; i++)
                System.out.print("  ");
            System.out.println(node);
        });
    }

    @Override
    public RastRoot parse(Set<String> filesOfInterest, FileContentReader reader) throws Exception {
        RastRoot root = new RastRoot();
        this.nodeCounter = 0;
        for (String path : filesOfInterest) {
            String content = reader.readAllContent(path);
            getRast(root.nodes, path, content);
        }
        return root;
    }

    private void getRast(List<RastNode> list, String path, String content) throws Exception {
        ScriptObjectMirror esprimaAst = (ScriptObjectMirror) this.invocableScript.invokeFunction("parse", content);
        getRast(list, path, esprimaAst);
    }

    private void getRast(List<RastNode> list, String path, ScriptObjectMirror esprimaAst) throws Exception {
        if (!esprimaAst.hasMember("type")) {
            throw new RuntimeException("object is not an AST node");
        }

        String type = (String) esprimaAst.getMember("type");
        ScriptObjectMirror range = (ScriptObjectMirror) esprimaAst.getMember("range");
        int begin = ((Number) range.getSlot(0)).intValue();
        int end = ((Number) range.getSlot(1)).intValue();

        if (EsprimaNodeHandler.HANDLERS.containsKey(type)) {
            EsprimaNodeHandler handler = EsprimaNodeHandler.HANDLERS.get(type);
            RastNode rastNode = new RastNode();
            rastNode.id = ++nodeCounter;
            rastNode.type = type;
            rastNode.location = new Location(path, begin, end);
            rastNode.logicalName = handler.getLogicalName(rastNode, esprimaAst);
            rastNode.stereotypes = handler.getStereotypes(rastNode, esprimaAst);
            list.add(rastNode);
            list = rastNode.nodes;
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
                                getRast(list, path, e);
                            }
                        }
                    }
                } else {
                    if (objectOrArray.hasMember("type")) {
                        getRast(list, path, objectOrArray);
                    }
                }
            }
        }
    }

}
