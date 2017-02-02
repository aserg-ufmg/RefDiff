package lsd.rule;

public class LSDInvalidTypeException extends Exception {
	public String toString() {
		return "Type mismatch or type is not one of the defined types.";
	}
}
