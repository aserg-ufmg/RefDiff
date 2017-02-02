package tyRuBa.util;

class EmptySource extends ElementSource {
	
	public static EmptySource the = new EmptySource();

	public int status() {
		return NO_MORE_ELEMENTS;
	}
	public Object nextElement() {
		throw new Error("TheEmpty ElementSource has no elements");
	}
	/** More efficient append to forget about useless empty sources */
	public ElementSource append(ElementSource other) {
		return other;
	}
	/** More efficient map for empty sources */
	public ElementSource map(Action what) {
		return theEmpty;
	}
	public void print(PrintingState p) {
		p.print("{*empty*}");
	}
	public boolean isEmpty() {
		return true;
	}
	
	public ElementSource first() {
		return this;
	}

}