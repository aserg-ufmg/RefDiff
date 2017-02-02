/*
 * Created on May 21, 2003
 *
 */
package tyRuBa.modes;

import java.util.HashMap;

/**
 * A TVarFactory creates TVar and makes sure that the same TVar is not created twice
 */
public class TVarFactory {
	
	private HashMap hm = new HashMap();

	public TVar makeTVar(String name) {
		TVar result = (TVar) hm.get(name);
		if (result == null) {
			result = Factory.makeTVar(name);
			hm.put(name, result);
		}
		return result;
	}
	
}
