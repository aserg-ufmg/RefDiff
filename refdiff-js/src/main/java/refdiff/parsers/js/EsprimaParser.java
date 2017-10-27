package refdiff.parsers.js;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import refdiff.core.io.SourceReader;
import refdiff.core.rast.Location;
import refdiff.core.rast.RastNode;
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
    public RastRoot parse(Set<String> filesOfInterest, SourceReader reader) throws Exception {
        RastRoot root = new RastRoot();
        this.nodeCounter = 0;
        for (String path : filesOfInterest) {
            String content = reader.readAllContent(path);
            getRast(root.nodes, path, content);
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
