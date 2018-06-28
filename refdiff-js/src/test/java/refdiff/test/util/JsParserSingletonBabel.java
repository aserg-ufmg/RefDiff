package refdiff.test.util;

import refdiff.parsers.js.BabelParser;

public class JsParserSingletonBabel {
	
	private static BabelParser instance = null;
	
	public static BabelParser get() {
		try {
			if (instance == null) {
				instance = new BabelParser();
			}
			return instance; 
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
