package tyRuBa.util;

/** A ConsList is a linked list type structure, but it can only be manipulated
    in side-effect-free manner. A functional operation "cons" is the ony way to add
    elements. The contents of a ConsList once constructed cannot be changed. */
public abstract class ConsList {

	public abstract boolean isEmpty();
	/** Only works on non-empty consList! */
	public abstract Object car();
	/** Only works on non-empty consList! */
	public abstract ConsList cdr();

	private static class ConsCel extends ConsList {
		Object car;
		ConsList cdr;
		ConsCel(Object a, ConsList b) {
			car = a;
			cdr = b;
		}
		public boolean isEmpty() {
			return false;
		}
		public Object car() {
			return car;
		}
		public ConsList cdr() {
			return cdr;
		}
	}

	/** Empty list (there can be only one!) */
	public final static ConsList theEmpty = new ConsList() {
		public boolean isEmpty() {
			return true;
		}
		public Object car() {
			throw new Error("Illegal operation -- car -- on empty ConsList");
		}
		public ConsList cdr() {
			throw new Error("Illegal operation -- cdr -- on empty ConsList");
		}
	};

	/** Construction operation */
	public static ConsList cons(Object acar, ConsList acdr) {
		return new ConsCel(acar, acdr);
	}

	public int length() {
		if (isEmpty())
			return 0;
		else
			return 1 + cdr().length();
	}

	public Object[] asArray() {
		ConsList rest = this;
		int len = length();
		Object[] result = new Object[len];
		for (int i = 0; i < len; i++) {
			result[i] = rest.car();
			rest = rest.cdr();
		}
		return result;
	}

	public Object[] reverseArray() {
		ConsList rest = this;
		int len = length();
		Object[] result = new Object[len];
		for (int i = len - 1; i >= 0; i--) {
			result[i] = rest.car();
			rest = rest.cdr();
		}
		return result;
	}

	public String toString() {
		Object[] els = asArray();
		StringBuffer text = new StringBuffer();
		for (int i = 0; i < els.length; i++) {
			if (i > 0) {
				text.append("/");
			}
			text.append(els[i].toString());
		}
		return text.toString();
	}

}
