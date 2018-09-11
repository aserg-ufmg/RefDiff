package refdiff.evaluation;

import java.util.Objects;

public class KeyPair {
	
	private final String key1;
	private final String key2;
	
	public KeyPair(String key1, String key2) {
		this.key1 = key1;
		this.key2 = key2;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof KeyPair) {
			KeyPair other = (KeyPair) obj;
			return Objects.equals(key1, other.key1) && Objects.equals(key2, other.key2);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(key1, key2);
	}
	
	public String getKey1() {
		return key1;
	}
	
	public String getKey2() {
		return key2;
	}
	
}
