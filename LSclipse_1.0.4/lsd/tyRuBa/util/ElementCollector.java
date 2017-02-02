package tyRuBa.util;

/** An ElementCollector collects and remembers elements from an element source. 
  Theelement source is called upon when the collector needs to produce a new element.

  ElementCollectors work in a lazy wya and will not request element sources to produce an
  element until the elements in the ElementCollector are being accessed.

  Circular dependencies between ElementCollectors and ElementSources are handled by
  temporary locking ElementCollectors (see hurting flag) while they are trying to produce 
  a new element.
  */

public abstract class ElementCollector {

  /** The elemenst stored in the order collected. */
  LinkedList elementStore = new LinkedList();

  /** Register a newly collected element */
  protected void addElement(Object e) {
    elementStore.addElement(e);
  }

  /** An element collector initialized with one source */
  public ElementCollector(ElementSource s) {
    setSource(s);
  }

  /** An element collector with no sources (add later with setSource) */
  public ElementCollector() {}

    /** Create an ElementSource which produces the elements in this collector
      one by one. WARNING! Don't remove elements from a circularly dependent collector! 
      Strange things happen in combination with lazy evaluation. */
	public RemovableElementSource elements() {
		return new ElementCollectorSource(this);
	}
  
	/** Collectors are lazy so you have to kick them to get them working on the next element.
	returns an ElementSource status flag. */
	final protected int kick() {
		if (!this.hurting) {
			int foundElement;
			try {
				hurting = true;
				foundElement = newElementFromSource();
			} finally {
				// Make sure this flag is not accidentally left on
				// even when something bad (like thread-death happens) 
				hurting = false;
			}
			return foundElement;
		} else { // No, I cannot produce an element right now go bother someone else.
//			System.err.print("#");
			return ElementSource.NO_ELEMENTS_READY;
		}
	}

  /** If I am kicked it hurts. If kicked again while hurting, this means I must
    be kicking myself indirectly. So I should not try to produce an element
    since I am already working on one rigth now! */
  private boolean hurting = false;
  
  /** Try to get an element from the source until you find a new one or it blocks.
    If an element is found it should be added using the "addElement" method.
    Returns if status flag indicating succes or mode of failure for finding el */
  protected abstract int newElementFromSource();
            
  /** My source of elements where I will try to get more elements when I am kicked. */
  protected ElementSource source=ElementSource.theEmpty;
  
  /** Add a new element source. */
  public void setSource(ElementSource source) {
    this.source=source;
  }

	public void print(PrintingState p) {
		p.print(this.toString());
		if (!p.visited.contains(this)) {
			p.visited.add(this);
			p.print("(");
			p.indent();
				p.newline();
				p.print("collected= ");
				p.indent();
					elementStore.elements().print(p);
				p.outdent();
				p.newline();
				p.print("source= ");
				p.indent();
					if (source==null)
						p.print("null");
					else
						source.print(p);
				p.outdent();
			p.outdent();
			p.print(")");
		}
	}

}

class ElementCollectorSource extends RemovableElementSource {

	ElementCollectorSource(ElementCollector aCollector) {
		myCollector = aCollector;
		pos = myCollector.elementStore.elements();
	}

	/** The collector I'll kick when I need more elements to be produced */
	private ElementCollector myCollector;

	/** Keeps my current position in myCollectors linked list of elements */
	private RemovableElementSource pos;

	/** Try to make an element ready if necessary and return the
	status after */
	public int status() {
		//System.err.println("ElementCollector::status():1");
		if (pos.hasMoreElements()) {
			//System.err.println("ElementCollector::status():2a");
			return ELEMENT_READY;
		} else if (myCollector != null) {
			//System.err.println("ElementCollector::status():3b");
			int st = myCollector.kick();
			// Kick that lazy Collector into action.
			//System.err.println("ElementCollector::status():4b");
			if (st == NO_MORE_ELEMENTS) {
				myCollector = null; // possibly reclaimable as soon as depleted
			}
			return st;
		} else // myCollector==null: is forever depleted and already gone
			return NO_MORE_ELEMENTS;
	}

	public void removeNextElement() {
		status();
		pos.removeNextElement();
	}

	public Object peekNextElement() {
		return pos.peekNextElement();
	}

	public Object nextElement() {
		status(); //force an element to be produced if necesary.
		return pos.nextElement();
	}
	
	public void print(PrintingState p) {
		p.print("CollectorSource(");
		p.indent();p.newline();
			p.print("pos= "); 
			p.indent();
				pos.print(p);
			p.outdent();p.newline();
			p.print("on = ");
			p.indent();
				if (myCollector==null)
					p.print("null");
				else
					myCollector.print(p);
			p.outdent();
		p.outdent();
		p.print(")");
	}
}
