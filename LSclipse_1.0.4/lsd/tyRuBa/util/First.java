/*
 * Created on Jul 10, 2003
 */
package tyRuBa.util;

/**
 * An ElementSource that keeps only the first element of
 * another Source. Implemented in such a way that it
 * does not destroy the lazy evaluation property of the
 * other source. Foo
 * 
 * This source does not keep a reference to the other source
 * after it has retrieved its first element.
 */
public class First extends ElementSource {
	
	private ElementSource source;
	
	public First(ElementSource from) {
		source = from;
	}

	public void print(PrintingState p) {
		p.print("First(");
		source.print(p);
		p.outdent();
		p.print(")");
	}

	public int status() {
		if (source==null)
			return NO_MORE_ELEMENTS;
		else {
			int stat = source.status();
			if (source.status()==NO_MORE_ELEMENTS) {
				source = null;
			}
			return stat;
		}
	}

	public Object nextElement() {
		ElementSource it = source;
		source = null;
		return it.nextElement();
	}

	public ElementSource first() {
		return this;
	}

}
