package tyRuBa.util;

import java.util.Iterator;

/**
 * @author cburns
 *
 * DoubleLinkedList class that operates by entry rather than by Object, so that O(1)
 * remove operations are possible.
 * 
 * There are few error checks. If you do something which is incorrect, such as adding an element
 * which is already on the list, removing an element which is not on the list, adding the list to itself,
 * etc etc etc, it would most likely result in a NullPointerException, and if not
 * it would break later.
 * 
 * The DoubleLinkedList is not synchronized.
 * 
 * The DoubleLinkedList cannot store anything that does not extend DoubleLinkedList.Entry. In order
 * to store things in the list, you should extend DoubleLinkedList.Entry and insert those objects
 * into the list. This reduces memory overhead and simplifies code. It also enforces the operation
 * by entry paradigm.
 */

public class DoubleLinkedList {

	public static class Entry {
		private Entry prev;
		private Entry next;
		
		public Entry() {
			this.prev = null;
			this.next = null;
		}
	}
	
	private Entry head = null;
	private Entry tail = null;
	private int size = 0;
	
	public DoubleLinkedList() {
	}
    
    public boolean isEmpty() {
        return size==0;
    }
	
	public int size() {
		return size;
	}
	
	public Entry head() {
		return head;
	}
	
	public Entry tail() {
		return tail;
	}
	
	public void clear() {
		head = null;
		tail = null;
		size = 0;
	}
	
	public void enqueue(Entry entry) {
		if(head != null) {
			entry.prev = null;
			entry.next = head;
			
			head.prev = entry;
			head = entry;
		} else {
			entry.prev = null;
			entry.next = null;
			
			head = entry;
			tail = head;
		}
		
		size++;
	}
	
	public void addLast(Entry entry) {
		if(tail != null) {
			entry.prev = tail;
			entry.next = null;
			
			tail.next = entry;
			tail = entry;
		} else {
			entry.prev = null;
			entry.next = null;
			
			tail = entry;
			head = tail;
		}

		size++;
	}
	
	/** Note that this operation shares elements, so only one of these lists should
	 * persist.
	 */
	public void addAll(DoubleLinkedList list) {
		if(list.head != null) {
			if(head != null) {
				head.prev = list.tail;
				list.tail.next = head;
				head = list.head;
				
				size += list.size;
			} else {
				head = list.head;
				tail = list.tail;
				
				size = list.size;
			}
		}
	}
	
	public void addAfter(DoubleLinkedList list, Entry entry) {
		if(list.head != null) {
			if(entry != tail) {
				list.tail.next = entry.next;
				entry.next.prev = list.tail;
				entry.next = list.head;
				list.head.prev = entry;
			} else {
				tail = list.tail;
				entry.next = list.head;
				list.head.prev = entry;
			}
			
			size += list.size;
		}
	}
	
	/**
	 * Add entry "after" after entry "entry"
	 */
	public void addAfter(Entry after, Entry entry) {
		if(entry != tail) {
			after.next = entry.next;
			entry.next.prev = after;
			entry.next = after;
			after.prev = entry;
		} else {
			after.next = null;
			tail = after;
			entry.next = after;
			after.prev = entry;
		}
		
		size++;
	}
	
	public void addBefore(DoubleLinkedList list, Entry entry) {
		if(list.head != null) {
			if(entry != head) {
				list.head.prev = entry.prev;
				entry.prev.next = list.head;
				entry.prev = list.tail;
				list.tail.next = entry;
			} else {
				head = list.head;
				entry.prev = list.tail;
				list.tail.next = entry;
			}
			
			size += list.size;
		}
	}
	
	
	/**
	 * Add entry "before" before entry "entry"
	 */
	public void addBefore(Entry before, Entry entry) {
		if(entry != head) {
			before.prev = entry.prev;
			entry.prev.next = before;
			entry.prev = before;
			before.next = entry;
		} else {
			before.prev = null;
			head = before;
			entry.prev = before;
			before.next = entry;
		}
		
		size++;
	}
	
	public void remove(Entry entry) {
		if(entry != head) {
			if(entry != tail) {
				entry.prev.next = entry.next;
				entry.next.prev = entry.prev;
			} else {
				entry.prev.next = null;
				tail = entry.prev;
			}
		} else {
			if(entry != tail) {
				entry.next.prev = null;
				head = entry.next;
			} else {
				head = null;
				tail = null;
			}
		}
		
		entry.prev = null;
		entry.next = null;
		
		size--;
	}

	public Entry dequeue() {
		Entry result = tail;
		remove(result);
		return result;
	}

	public Entry peek() {
		return tail;
	}
	
	public String toString() {
		String result = "DoubleLL( ";
		Entry current = head;
		while (current!=null) {
			result += current+" ";
			current = current.next;
		}
		return result + ")";
	}

	public Iterator iterator() {
		return new Iterator() {
			
			private Entry current = head;

			public boolean hasNext() {
				return current != null;
			}

			public Object next() {
				Entry result = current;
				current = current.next;
				return result;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

}
