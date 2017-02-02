package tyRuBa.util;

/** Merges elements from a number of element sources. An element source is 
  called upon by the element Merger when it needs to produce a new element.

  The element sources themselves are elements produced by an element source.

  ElementCollectors work in a lazy way and will not request element sources to produce an
  element until the elements in the ElementCollector are being accessed.

  */

public class FlattenElementSource extends ElementSource {

	/** The next ready element */
	Object e = null;

	/** The source of sources */
	private ElementCollector sources = new ElementDuplicatesCollector();

	/** An element collector initialized with one source */
	public FlattenElementSource(ElementSource metaSource) {
		sources.setSource(metaSource);
	}

	public int status() {
		if (e != null)
			return ELEMENT_READY;
		else {
			RemovableElementSource remainingSources = sources.elements();
			int stat;
			while ((stat = remainingSources.status()) == ELEMENT_READY) {
				ElementSource firstSource =
					(ElementSource) remainingSources.peekNextElement();
				stat = firstSource.status();
				switch (stat) {
					case ELEMENT_READY :
						e = firstSource.nextElement();
						return ELEMENT_READY;
					case NO_MORE_ELEMENTS :
						remainingSources.removeNextElement();
						// It is depleted forever.
						// was for testing : remainingSources.nextElement();
						break;
					case NO_ELEMENTS_READY :
						remainingSources.nextElement();
						break;
				}
			}
			//All sources are depleted or blocked.
			stat = sources.elements().status();
			if (stat == NO_MORE_ELEMENTS)
				return NO_MORE_ELEMENTS; // all sources depleted
			else
				return NO_ELEMENTS_READY; // some sources are blocked
		}
	}

	public Object nextElement() {
		status();
		Object result = e;
		e = null;
		return result;
	}

	
	public void print(PrintingState p) {
		p.print("Flatten(");
		p.indent();p.newline();
			sources.print(p);
		p.outdent();
		p.print(")");
	}

}
