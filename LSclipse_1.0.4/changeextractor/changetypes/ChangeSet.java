package changetypes;

import java.io.PrintStream;
import java.util.HashSet;

public class ChangeSet extends HashSet<AtomicChange> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final int[] changecount = new int[AtomicChange.ChangeTypes.values().length];

	public void print(PrintStream out) {
		if (this.size()>0) {
			out.println("~~~Changes~~~");
			for (AtomicChange ac : this) {
				out.println(ac.toString());
			}
		} else {
			out.println("No changes");
		}
	}

	//Normalize changeset by removing irrelevant changes, eg remove CM when DM is present
	public void normalize() {
	}
}
