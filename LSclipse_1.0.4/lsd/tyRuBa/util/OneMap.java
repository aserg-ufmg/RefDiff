package tyRuBa.util;

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;

import java.lang.reflect.Array;

/**
 * 
 * @author cburns
 *
 * Class that implements the Map interface for exactly one element.
 * 
 * Add operations will simply overwrite objects that are already there if size() != 0.
 * 
 * This class is only really useful for reducing memory overhead. It sure is a lot of work for
 * one silly mapping :P
 */

public class OneMap implements Map, Serializable {
	private Object key = null;
	private Object value = null;
	
	public void clear() {
		key = null;
		value = null;
	}
	
	public int size() {
		return (key == null && value == null) ? 0 : 1;
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public boolean containsKey(Object key) {
		return (key == null && this.key == null) || (key != null && key.equals(this.key));
	}
	
	public boolean containsValue(Object value) {
		return (value == null && this.value == null) || (value != null && value.equals(this.value));
	}
	
	public Object get(Object key) {
		return ((key == null && this.key == null) || 
		(key != null && key.equals(this.key))) ? value : null;
	}
	
	public Object put(Object key, Object value) {
		Object previous = get(key);
			
		this.key = key;
		this.value = value;
			
		return previous;
	}
	
	public Object remove(Object key) {
		Object previous = get(key);
		
		if(containsKey(key)) {
			this.key = null;
			this.value = null;
		}
		
		return previous;
	}
	
	public void putAll(Map t) {
		Iterator m = t.entrySet().iterator();
		
		if(m.hasNext()) {
			Map.Entry e = (Map.Entry)m.next();
			put(e.getKey(), e.getValue());
		}
	}
	
	public Set keySet() {
		return new KeySet();
	}
	
	private class Entry implements Map.Entry {
		private Object key;
		private Object value;
		
		public Entry(Object key, Object value) {
			this.key = key;
			this.value = value;
		}
		
		public Object getKey() { return key; }
		
		public Object getValue() { return value; }
		
		public Object setValue(Object value) {
			Object previous = this.value;
			this.value = value;
			OneMap.this.value = value;
			
			return previous;
		}
	}
	
	private class KeySet implements Set {
		
		public boolean add(Object object) {
			throw new UnsupportedOperationException();
		}
		
		public boolean addAll(Collection c) {
			throw new UnsupportedOperationException();
		}
		
		public boolean remove(Object object) {
			return null != OneMap.this.remove(object);
		}
		
		public void removeAll() {
			clear();
		}
		
		public boolean retainAll(Collection collection) {
			Iterator iterator = collection.iterator();
				
			while(iterator.hasNext())
				if(OneMap.this.containsKey(iterator.next()))
					return false;
			
			clear();
			return true;
		}
		
		public boolean removeAll(Collection collection) {
			Iterator iterator = collection.iterator();
			
			while(iterator.hasNext())
				if(remove(iterator.next()))
					return true;
					
			return false;
		}
		
		public int size() {
			return OneMap.this.size();
		}
		
		public boolean isEmpty() {
			return OneMap.this.isEmpty();
		}
		
		public void clear() {
			OneMap.this.clear();
		}
		
		public Iterator iterator() {
			return new SetIterator();
		}
		
		private class SetIterator implements Iterator {
			private boolean exhausted;
			private boolean cleared;
			
			public SetIterator() {
				exhausted = isEmpty();
				cleared = exhausted;
			}
			
			public boolean hasNext() {
				return !exhausted;
			}
			
			public Object next() {
				if(hasNext()) {
					exhausted = true;
					return OneMap.this.key;
				} else
					throw new NoSuchElementException();
			}
			
			public void remove() {
				if(exhausted && !cleared) {
					cleared = true;
					OneMap.this.clear();
				} else
					throw new IllegalStateException();
			}
		}
		
		public boolean contains(Object object) {
			if(OneMap.this.key == object)	
				return true;
			else
				return false;
		}
		
		public boolean containsAll(Collection collection) {
			return (collection.size() == 0) ||
			(collection.size() == 1 && OneMap.this.size() == 1 && 
			contains(collection.iterator().next()));
		}
		
		public Object[] toArray() {
			if(OneMap.this.size() == 0)
				return new Object[0];
			else
				return new Object[] { OneMap.this.key };
		}
		
		public Object[] toArray(Object[] a) {
			if(OneMap.this.size() == 0) {
				if(a.length > 0)
					a[0] = null;
				return a;
			} else {
				if(a.length >= 1) {
					a[0] = OneMap.this.key;
					if(a.length > 1)
						a[1] = null;
						
					return a;
				} else {
					Object[] r = (Object[])Array.newInstance(a.getClass(), 1);
					r[0] = OneMap.this.key;
					return r;
				}
			}
		}
	}
	
	public Set entrySet() {
		return new EntrySet();
	}
	
	public class EntrySet implements Set {
		public boolean add(Object object) {
			throw new UnsupportedOperationException();
		}
		
		public boolean addAll(Collection c) {
			throw new UnsupportedOperationException();
		}
		
		public boolean remove(Object object) {
			if(contains(object)) {
				clear();
				return true;
			} else
				return false;
		}
		
		public void removeAll() {
			clear();
		}
		
		public boolean retainAll(Collection collection) {
			Iterator iterator = collection.iterator();
				
			while(iterator.hasNext())
				if(contains(iterator.next()))
					return false;
			
			clear();
			return true;
		}
		
		public boolean removeAll(Collection collection) {
			Iterator iterator = collection.iterator();
			
			while(iterator.hasNext()) {
				if(remove(iterator.next()))
					return true;
			}
					
			return false;
		}
		
		public int size() {
			return OneMap.this.size();
		}
		
		public boolean isEmpty() {
			return OneMap.this.isEmpty();
		}
		
		public void clear() {
			OneMap.this.clear();
		}
		
		public Iterator iterator() {
			return new SetIterator();
		}
		
		private class SetIterator implements Iterator {
			private boolean exhausted;
			private boolean cleared;
			
			public SetIterator() {
				exhausted = isEmpty();
				cleared = exhausted;
			}
			
			public boolean hasNext() {
				return !exhausted;
			}
			
			public Object next() {
				if(hasNext()) {
					exhausted = true;
					return new Entry(OneMap.this.key, OneMap.this.value);
				} else
					throw new NoSuchElementException();
			}
			
			public void remove() {
				if(exhausted && !cleared) {
					cleared = true;
					OneMap.this.clear();
				} else
					throw new IllegalStateException();
			}
		}
		
		public boolean contains(Object object) {
			if(object instanceof Entry) {
				Entry entry = (Entry)object;
				if(OneMap.this.key == entry.getKey() && OneMap.this.value == entry.getValue())
					return true;
			}
			
			return false;
		}
		
		public boolean containsAll(Collection collection) {
			return (collection.size() == 0) ||
			(collection.size() == 1 && OneMap.this.size() == 1 && 
			contains(collection.iterator().next()));
		}
		
		public Object[] toArray() {
			if(isEmpty())
				return new Object[0];
			else
				return new Object[] { new Entry(OneMap.this.key, OneMap.this.value) };
		}
		
		public Object[] toArray(Object[] a) {
			if(OneMap.this.size() == 0) {
				if(a.length > 0)
					a[0] = null;
				return a;
			} else {
				if(a.length >= 1) {
					a[0] = new Entry(OneMap.this.key, OneMap.this.value);
					if(a.length > 1)
						a[1] = null;
						
					return a;
				} else {
					Object[] r = (Object[])Array.newInstance(a.getClass(), 1);
					r[0] = new Entry(OneMap.this.key, OneMap.this.value);
					return r;
				}
			}
		}
	}
	
	public Collection values() {
		return new Values();
	}
	
	private class Values implements Collection {
		public void clear() {
			OneMap.this.clear();
		}
		
		public boolean add(Object object) {
			throw new UnsupportedOperationException();
		}
		
		public boolean addAll(Collection collection) {
			throw new UnsupportedOperationException();
		}
		
		public boolean remove(Object object) {
			if(contains(object)) {
				clear();
				return true;
			} else
				return false;
		}
		
		public boolean containsAll(Collection collection) {
			return (collection.size() == 0) ||
			(collection.size() == 1 && OneMap.this.size() == 1 && 
			contains(collection.iterator().next()));
		}
		
		public boolean contains(Object object) {
			return OneMap.this.containsValue(object);
		}
		
		public boolean isEmpty() {
			return OneMap.this.isEmpty();
		}
		
		public int size() {
			return OneMap.this.size();
		}
		
		public boolean retainAll(Collection collection) {
			Iterator iterator = collection.iterator();
				
			while(iterator.hasNext())
				if(contains(iterator.next()))
					return false;
			
			clear();
			return true;
		}
		
		public boolean removeAll(Collection collection) {
			Iterator iterator = collection.iterator();
			
			while(iterator.hasNext())
				if(remove(iterator.next()))
					return true;
		
			return false;
		}
		
		public Iterator iterator() {
			return new ValueIterator();
		}
		
		private class ValueIterator implements Iterator {
			private boolean exhausted;
			private boolean cleared;
			
			public ValueIterator() {
				exhausted = isEmpty();
				cleared = exhausted;
			}
			
			public boolean hasNext() {
				return !exhausted;
			}
			
			public Object next() {
				if(hasNext()) {
					exhausted = true;
					return OneMap.this.value;
				} else
					throw new NoSuchElementException();
			}
			
			public void remove() {
				if(exhausted && !cleared) {
					cleared = true;
					OneMap.this.clear();
				} else
					throw new IllegalStateException();
			}
		}
		
		public Object[] toArray() {
			if(isEmpty())
				return new Object[0];
			else
				return new Object[] { OneMap.this.value };
		}
		
		public Object[] toArray(Object[] a) {
			if(OneMap.this.size() == 0) {
				if(a.length > 0)
					a[0] = null;
				return a;
			} else {
				if(a.length >= 1) {
					a[0] = OneMap.this.value;
					if(a.length > 1)
						a[1] = null;
						
					return a;
				} else {
					Object[] r = (Object[])Array.newInstance(a.getClass(), 1);
					r[0] = OneMap.this.value;
					return r;
				}
			}
		}
	}
}