import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class Parser2 {

    public static void main(String[] args) throws ScriptException, FileNotFoundException, NoSuchMethodException {
        
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.eval(new FileReader("src/main/js/parser.js"));
        
        Invocable invocable = (Invocable) engine;
        ScriptObjectMirror ast = (ScriptObjectMirror) invocable.invokeFunction("parse", "const answer = 42");
        
        traverse(ast);
    }

	private static void traverse(ScriptObjectMirror ast) {
		String type = (String) ast.getMember("type");
		Object range = ast.getMember("range");
		
		System.out.println(type);
		System.out.println(range);
	}
    
}
