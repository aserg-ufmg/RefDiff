package refdiff.test.util;

import refdiff.parsers.js.JsPlugin;

public class JsParserSingleton {
	
	private static JsPlugin instance = null;
	
	public static JsPlugin get() {
		try {
			if (instance == null) {
				instance = new JsPlugin();
			}
			return instance; 
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
