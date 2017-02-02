package tyRuBa.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

/** An element source is similar to a java.util.Enumeration. Except that it can sometimes
  be temporarily out of elements. A java.util.Enumeration on the other hand either has more
  elements or it has not.

  Elements source have three possible conditions:
   ELEMENT_READY     : I can produce an element now.
   NO_ELEMENTS_READY : I cannot produce an element now.
   NO_MORE_ELEMENTS  : I cannot produce any more elements, not now AND not in 
                       the future.

  It is required that an element source discriminates at least between NO_ELEMENTS_READY
  and ELEMENT_READY. For efficiency it is advisiable that an elment signals NO_MORE_ELEMENTS
  if it can. This allows an element collector to remove the source from its list of potential
  element sources.
  **/
public abstract class ElementSource {
	
	public boolean isEmpty() {
		return false;
	}
	
	public abstract void print(PrintingState p);

	/** Status flags returned by status() */
	public static final int ELEMENT_READY = 1;
	public static final int NO_ELEMENTS_READY = 0;
	public static final int NO_MORE_ELEMENTS = -1;

	/** Returns a status flag: ELEMENT_READY, ... */
	public abstract int status();

	/** If an element is available, return it, and move on to the next. */
	public abstract Object nextElement();

	/** Returns true if an element is ready right now */
	public boolean hasMoreElements() {
		return status() == ELEMENT_READY;
	}

	/** Make an ElementSource which produces a single element. */
	public static ElementSource singleton(final Object e) {
//		PoormansProfiler.countSingletons++;
		return new ElementSource() {
			private Object myElement = e;
			public int status() {
				if (myElement == null)
					return NO_MORE_ELEMENTS;
				else
					return ELEMENT_READY;
			}
			public Object nextElement() {
				Object el = myElement;
				myElement = null;
				return el;
			}
			public void print(PrintingState p) {
				p.print("{");
				if (myElement==null)
					p.print("null");
				else
					p.print(myElement.toString());
				p.print("}");
			}
			public boolean isEmpty() {
				return myElement==null;
			}
			public ElementSource first() {
				return this;
			}

		};
	}

	/** The element source with no elements */
	public static final ElementSource theEmpty = EmptySource.the;

	/** Append an element source to this one. If this source is blocked or empty the
	  appended source is checked */
	public ElementSource append(ElementSource other) {
		if (other.isEmpty())
			return this;
		else
			return new AppendSource(this, other);
	}
	
	/** Map an action onto an element source to obtain a different element source */
	public ElementSource map(Action what) {
		return new MapElementSource(this, what);
	}

	/** With elements from an enumeration */
//	public static ElementSource with(final java.util.Enumeration els) {
//		return new ElementSource() {
//			public int status() {
//				return els.hasMoreElements() ? ELEMENT_READY : NO_MORE_ELEMENTS;
//			}
//			public Object nextElement() {
//				return els.nextElement();
//			}
//			public void print(PrintingState p) {
//				p.print(els.toString());
//			}
//		};
//	}

	/** With elements from an array */
	public static ElementSource with(final Object[] els) {
		return new ElementSource() {
			int pos = 0;
			public int status() {
				return (pos < els.length) ? ELEMENT_READY : NO_MORE_ELEMENTS;
			}
			public Object nextElement() {
				return els[pos++];
			}
			public void print(PrintingState p) {
				p.print("{");
				for (int i = 0; i < els.length; i++) {
					if (i>0)
						p.print(",");
					p.print(els[i].toString());
				}
				p.print("}");
			}
			public ElementSource first() {
				// An Arrya based sourse is not lazy... so don;t bother being lazy either
				if (hasMoreElements())
					return ElementSource.singleton(nextElement());
				else
					return ElementSource.theEmpty;
			}
		};
	}
	
	/** With elements from an ArrayList */
	public static ElementSource with(final ArrayList els) {
		if (els.isEmpty())
			return ElementSource.theEmpty;
		else
			return new ArrayListSource(els);
	}
	
	/** With an iterator **/
	public static ElementSource with(final Iterator it) {
		return new ElementSource() {
			
			public int status() {
				return it.hasNext() ? ELEMENT_READY : NO_MORE_ELEMENTS;
			}
			
			public Object nextElement() {
				return it.next();
			}
			
			public void print(PrintingState p) {
				p.print("{");
				p.print("NOT CURRENTLY SUPPORTED");
				p.print("}");
			}
		};
	}
   
	/**
	 * This will step through all the elements in the ElementSource.
	 * Use this if the side effects matter, but you don't care about the
	 * actual values in the ElementSource.
	 */
	public void forceAll() {
		while (hasMoreElements())
			nextElement();
	}
	
	public String toString() {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		print(new PrintingState(new PrintStream(result)));
		return result.toString();
	}

	public ElementSource first() {
		return new First(this);
	}

	/** Get a Source of only the first element of the ElementSource without respecting its
	 * lazyness.
	 */
	public ElementSource immediateFirst() {
		int stat = status();
		if (stat==ELEMENT_READY)
			return ElementSource.singleton(nextElement());
		else if (stat==NO_MORE_ELEMENTS)
			return ElementSource.theEmpty;
		else  // NO_ELEMENTS_READY
			return first(); // Can't be immediate, revert to lazy mode.
	}

	public ElementSource flatten() {
		return new FlattenElementSource(this);
	}

	public SynchronizedElementSource synchronizeOn(SynchResource resource) {
		return new SynchronizedElementSource(resource,this);
	}

	/** 
	 * Be aware that, as is usual with elementSources, getting elements 
	 * which is implied by counting
	 * them, consumes the elements, so there is no way to get the
	 * elements after you have counted them
	 */
	public int countElements() {
		int result = 0;
		while (this.hasMoreElements()) {
			this.nextElement();
			result++;
		}
		return result;
	}

	/** 
	 * Users may call thi method to signify that they are done
	 * with this elementsource, possibly allowing the system to release
	 * resources needed to produce pending elements which will never be
	 * consumed.
	 * 
	 * Default implementation does nothing, can be overridden by specific
	 * subclasses.
	 */
	public void release() {
	}

    public Object firstElementOrNull() {
        if (this.hasMoreElements()) {
            Object result = nextElement();
            this.release();
            return result;
        }
        else
            return null;
    }

}
