package tyRuBa.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author wannop
 *
 * An empty iterator. Now uses the standardized Iterator exceptions.
 */
public class EmptyIterator implements Iterator {

	public static final EmptyIterator the = new EmptyIterator();

	private EmptyIterator() {
		super();
	}

	public boolean hasNext() {
		return false;
	}

	public Object next() {
		throw new NoSuchElementException();
	}

	public void remove() {
		throw new IllegalStateException();
	}
}
