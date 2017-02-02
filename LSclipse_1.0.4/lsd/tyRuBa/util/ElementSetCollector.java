package tyRuBa.util;

import java.util.HashSet;

/** An ElementCollector collects and remembers elements from an element source. 
  Theelement source is called upon when the collector needs to produce a new element.

  ElementCollectors work in a lazy wya and will not request element sources to produce an
  element until the elements in the ElementCollector are being accessed.

  Circular dependencies between ElementCollectors and ElementSources are handled by
  temporary locking ElementCollectors (see hurting flag) while they are trying to produce 
  a new element.
  */

public class ElementSetCollector extends ElementCollector {

  /** Keep track of all the elements I have seen so far */
  private java.util.Set seen = new HashSet();

  protected void addElement(Object e) {
    super.addElement(e);
    seen.add(e);
  }

  /** An element collector initialized with one source */
  public ElementSetCollector(ElementSource s) {
    super(s);
  }

  /** An element collector with no sources (add later with setSource) */
  public ElementSetCollector() {}

  private boolean isPresent(Object el) {
    return seen.contains(el);
  }
  
    /** Try to get an element from the source until you find a new one or it blocks.
      Returns a status flag */
	protected int newElementFromSource() {
		int status;
		Object element;
		do {
			if (source==null)
				return ElementSource.NO_MORE_ELEMENTS;
			status = source.status();
			if (status == ElementSource.ELEMENT_READY) {
				element = source.nextElement();
				if (!isPresent(element)) {
					addElement(element);
					return ElementSource.ELEMENT_READY;
				}
			} else {// No element found in the source
				if (status == ElementSource.NO_MORE_ELEMENTS) {
					source = null; // potentially reclaimable
					seen = null; // no more elements will come we don't need
					             // to check for duplicates anymore.
				}
				return status;
			}
		} while (true);
	}
            
  /** Small test Application */
  public static void main(String args[]) {
    final ElementSetCollector testSet = new ElementSetCollector();
    testSet.setSource(
       testSet.elements().map(new Action() {
	 public Object compute(Object a) {
	   int i = ((Integer)a).intValue();
	   i = (i+1)%10; 
	   return new Integer(i);
	 }
       })
       .append(
	       ElementSource.singleton(new Integer(1))
       ));
    RemovableElementSource testSetEls = new ElementSetCollector(testSet.elements()).elements();
    //RemovableElementSource testSetEls = testSet.elements();
    while (testSetEls.status()==ElementSource.ELEMENT_READY) {
      System.out.println(testSetEls.peekNextElement());
      testSetEls.removeNextElement();
    }
  }

}
