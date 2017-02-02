package tyRuBa.util;

/**
 * @author cburns
 *
 * Class so we can pass references to a single integer around and change its contents.
 */
public class MutableInteger {
	public int intValue = 0;
	
	public MutableInteger() {
	}
	
	public MutableInteger(int newValue) {
		intValue = newValue;
	}
}
