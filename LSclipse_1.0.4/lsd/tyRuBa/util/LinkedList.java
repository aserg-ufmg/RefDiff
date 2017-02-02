package tyRuBa.util;

public class LinkedList {

  private class Bucket {
    Object el;
    Bucket next = null;
    
    Bucket(Object e) { el=e; }
    Bucket(Object e,Bucket r) {el=e; next=r;}
  }

  /** The start of the stored elements list */
  private Bucket head = new Bucket("dummy"); //dummy bucket makes code to add elements easier
  /** The end of the stored elements list */
  private Bucket tail = head;
  
  /** Add an element to the end */
  public void addElement(Object e) {
    tail.next = new Bucket(e);
    tail=tail.next;
  }

  /** Empty? */
  public boolean isEmpty() {
    return head.next==null;
  }

  /** Create an ElementSource which produces the elements in this LinkedList
    one by one */
  public RemovableElementSource elements() {
    return new RemovableElementSource() {
      private Bucket pos = head;

      public int status() {
	if (pos.next!=null)
	  return ELEMENT_READY;
	else
	  return NO_ELEMENTS_READY;
      }
      
      public Object peekNextElement() {
	return pos.next.el;
      }

      public void removeNextElement() {
	if ((pos.next=pos.next.next)==null)
	  tail=pos;
      }

      public Object nextElement() {
	pos = pos.next;
	return pos.el;
      }
      
      public void print(PrintingState p) {
      	p.print("Linked[");
      	for (Bucket current = pos.next;current!=null;current = current.next) {
      		p.printObj(current.el);
      		if (current.next!=null)
      			p.print(",");
      	}
      	p.print("]");
      }
    };
  }

}
