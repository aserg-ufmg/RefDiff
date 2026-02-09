package refdiff.test.util;

import refdiff.parsers.go.GoPlugin;

public class GoParserSingleton {
	
	private static GoPlugin instance = null;
	
	public static GoPlugin get() {
		try {
			if (instance == null) {
				instance = new GoPlugin();
			}
			return instance; 
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
