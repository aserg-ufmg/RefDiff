package tyRuBa.util;

import java.io.File;

public class Files {
	
	public static void deleteDirectory(File dir) {
		if (dir.exists()) {
			if (dir.isDirectory()) {
				File[] children = dir.listFiles();
				for (int i = 0; i < children.length; i++) {
					deleteDirectory(children[i]);
				}
			}  
			dir.delete();
		}
	}
	
}
