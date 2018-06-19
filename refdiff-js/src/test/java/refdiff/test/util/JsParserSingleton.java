package refdiff.test.util;

import refdiff.parsers.js.JsParser;

public class JsParserSingleton {
	
	private static JsParser instance = null;
	
	public static JsParser get() {
		try {
			if (instance == null) {
				instance = new JsParser();
			}
			return instance; 
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
