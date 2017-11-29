package refdiff.test.util;

import refdiff.parsers.js.EsprimaParser;

public class EsprimaParserSingleton {
	
	private static EsprimaParser instance = null;
	
	public static EsprimaParser get() {
		try {
			if (instance == null) {
				instance = new EsprimaParser();
			}
			return instance; 
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
