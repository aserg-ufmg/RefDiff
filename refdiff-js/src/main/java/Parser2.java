import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class Parser2 {

    public static void main(String[] args) throws ScriptException, IOException, NoSuchMethodException {
        
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.eval(new FileReader("src/main/js/parser.js"));
        

        byte[] encoded = Files.readAllBytes(Paths.get("hello.js"));
        String source = new String(encoded, StandardCharsets.UTF_8);
        
        Invocable invocable = (Invocable) engine;
        ScriptObjectMirror ast = (ScriptObjectMirror) invocable.invokeFunction("parse", source);
        
        traverse(ast);
    }

	private static void traverse(ScriptObjectMirror ast) {
	    if (ast.hasMember("type")) {
	        String type = (String) ast.getMember("type");
	        ScriptObjectMirror range = (ScriptObjectMirror) ast.getMember("range");
	        System.out.println(type);
	        System.out.println(range.getSlot(0) + " " + range.getSlot(1));
	        
	        for (String key : ast.getOwnKeys(true)) {
	            Object obj = ast.getMember(key);
	            if (obj instanceof ScriptObjectMirror) {
	                traverse((ScriptObjectMirror) obj);
	            }
	        }
	    } else if (ast.isArray()) {
	        for (int i = 0; i < ast.size(); i++) {
	            Object element = ast.getSlot(i);
	            if (element instanceof ScriptObjectMirror) {
	                traverse((ScriptObjectMirror) element);
	            }
	        }
	    }
	}
    
}
