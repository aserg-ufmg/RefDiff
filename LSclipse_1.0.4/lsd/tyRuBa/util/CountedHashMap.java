package tyRuBa.util;


import java.io.Serializable;

/**
 * @author cburns
 *
 * Hashtable that maintains a 16 bit count of insertions and removals. Once a key 
 * has been removed as many times as it has been inserted, the mapping will be 
 * destroyed.
 * 
 * Also has a 16 bit flag field for other information.
 * 
 * This implementation uses distributed lock synchronization.
 * 
 * HashMap was reimplemented instead of extended because of memory constraints and lock contention,
 * and because only ints are used as keys.
 * 
 * In general, threads will only contend on put() and remove(), and if entries are actually created
 * or removed.
 */
public class CountedHashMap implements Serializable {
	private static final int DEFAULT_INITIAL_SIZE = 32;
	private static final int MAX_CONCURRENCY = 32;
	private static final double DEFAULT_LOAD_FACTOR = .75;
	
	
	
	private static class Entry implements Serializable {
		public final int key;
		public Object value;
		public int data;
		public Entry next;
		
		public Entry(int key, Object value, Entry next) {
			this.key = key;
			this.value = value;
			this.data = 0x00000001; //count = 1, flags = 0
			this.next = next;
		}
		
		public final int preIncrementCount() {
			return ++data & 0x0000FFFF;
		}
		
		public final int preDecrementCount() {
			return --data & 0x0000FFFF;
		}
		
		public final void setFlags(int flags) {
			data = (data & 0x0000FFFF) + (flags << 16);
		}
		
		public final int getFlags() {
			return data >> 16;
		}
		
		public String toString() {
			return String.valueOf(key) + "=" + value;
		}
	}
	
	private transient Object[] mutex;
	{
		mutex = new Object[MAX_CONCURRENCY];
		int i = MAX_CONCURRENCY;
		while(i-- != 0)
			mutex[i] = new Object();
	}
	
	private Entry[] table;
	private int size;
	private double loadFactor;
	private int threshold;
	
	private void rehash(boolean grow) {
		rehash(0, grow);
	}
	
	private void rehash(int obtained, boolean grow) {
		if(obtained == MAX_CONCURRENCY) {
			Entry ntable[] = grow ? new Entry[table.length * 2] : new Entry[table.length / 2];
			int location = 0;
			int nlocation;
			Entry bucket;		
			Entry next;
		
			while(location < table.length) {
				bucket = table[location];
			
				while(bucket != null) {
					next = bucket.next;
			
					nlocation = ((bucket.key % ntable.length) + ntable.length) % ntable.length;
				
					bucket.next = ntable[nlocation];
					ntable[nlocation] = bucket;
				
					bucket = next;
				}
			
				location++;
			}
		
			table = ntable;
		
			threshold = (int)(((double)table.length) * loadFactor);
		} else
			synchronized(mutex[obtained++]) { rehash(obtained, grow); }
	}
	
	public CountedHashMap() {
		this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
	}
	
	public CountedHashMap(int initialSize) {
		this(initialSize, DEFAULT_LOAD_FACTOR);
	}
	
	public CountedHashMap(double loadFactor) {
		this(DEFAULT_INITIAL_SIZE, loadFactor);
	}
	
	public CountedHashMap(int initialSize, double loadFactor) {
			
		this.table = new Entry[initialSize];
		this.size = 0;
		this.loadFactor = loadFactor;
		this.threshold = (int)(((double)table.length) * loadFactor);
	}
	
//	public void clear() {
//		clear(0);
//	}

//	private void clear(int obtained) {
//		if(obtained == MAX_CONCURRENCY) {
//			this.table = new Entry[DEFAULT_INITIAL_SIZE];
//			this.size = 0;
//		} else
//			synchronized(mutex[obtained++]) { clear(obtained); }
//	}

	public boolean containsKey(int key) {
		int length = table.length;
		int location = ((key % length) + length) % length;
		
		synchronized(mutex[location % MAX_CONCURRENCY]) {
			if(length == table.length) { //See if the table was rehashed while we were obtaining our lock
				Entry bucket = table[location];
				while(bucket != null) {
					if(key == bucket.key)
						return true;
					else
						bucket = bucket.next;
				}
		
				return false;
			}
		}
		
		return containsKey(key); //Wrong lock due to rehash, reenter.
	}

	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException(); //Not supported because it's slow, plus it would require
												   //nasty lock accumulation
	}

	public Object get(int key) {
		int length = table.length;
		int location = ((key % length) + length) % length;
		
		synchronized(mutex[location % MAX_CONCURRENCY]) {
			if(length == table.length) { //See if the table was rehashed while we were obtaining our lock
				Entry bucket = table[location];
				while(bucket != null) {
					if(key == bucket.key)
						return bucket.value;
					else
						bucket = bucket.next;
				}
				
				return null;
			}
		}	
		
		return get(key); //Wrong lock due to rehash, reenter.
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public Object put(int key, Object value) {
		
//		FactBaseTest.logger.println("putRef("+key+","+value+")");
		
		int length = table.length;
		int location = ((key % length) + length) % length;
		
		synchronized(mutex[location % MAX_CONCURRENCY]) {
			if(length == table.length) { //See if the table was rehashed while we were obtaining our lock
				Object previous;
		
				Entry bucket = table[location];
		
				while(bucket != null) {
					if(key == bucket.key) {
						previous = bucket.value;
						bucket.value = value;
						bucket.preIncrementCount();
						return previous;
					} else
						bucket = bucket.next;
				}
		
				table[location] = new Entry(key, value, table[location]);
		
				synchronized(mutex) {
					if(++size > threshold)
						rehash(true);
				}
		
				return null;
			}
		}
		
		return put(key, value); //Wrong lock due to rehash, reenter.
	}
	
	public Object remove(int key) {
//		FactBaseTest.logger.println("remove("+key+")");

		int length = table.length;
		int location = ((key % length) + length) % length;
		
		synchronized(mutex[location % MAX_CONCURRENCY]) {
			if(length == table.length) { //See if the table was rehashed while we were obtaining our lock
				Entry previous = null;
				Entry bucket = table[location];
		
				while(bucket != null) {
					if(key == bucket.key) {
						if(bucket.preDecrementCount() == 0) {
							if(previous == null)
								table[location] = bucket.next;
							else
								previous.next = bucket.next;
				
							synchronized(mutex) { 
								if(--size < threshold/3)
									rehash(false);
							}
						}	
				
						return bucket.value;
					} else {
						previous = bucket;
						bucket = bucket.next;
					}
				}
		
				return null;
			}
		}
		
		return remove(key); //Wrong lock due to rehash, reenter.
	}
	
	public boolean setFlags(int key, int flags) {
		int length = table.length;
		int location = ((key % length) + length) % length;
		
		synchronized(mutex[location % MAX_CONCURRENCY]) {
			if(length == table.length) { //See if the table was rehashed while we were obtaining our lock
				Entry bucket = table[location];
		
				while(bucket != null) {
					if(key == bucket.key) {
						bucket.setFlags(flags);
						return true;
					} else
						bucket = bucket.next;
				}
		
				return false;
			}
		}
		
		return setFlags(key, flags); //Wrong lock due to rehash, reenter.
	}
	
	public int getFlags(int key) {
		int length = table.length;
		int location = ((key % length) + length) % length;
		
		synchronized(mutex[location % MAX_CONCURRENCY]) {
			if(length == table.length) { //See if the table was rehashed while we were obtaining our lock.
				Entry bucket = table[location];
		
				while(bucket != null) {
					if(key == bucket.key)
						return bucket.getFlags();
					else
						bucket = bucket.next;
				}
		
				return 0;	
			}	
		}
		
		return getFlags(key); //Wrong lock due to rehash, reenter.
	}

	public int size() {
		return size;
	}
	
	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
		in.defaultReadObject();
		
		mutex = new Object[MAX_CONCURRENCY];
		int i = MAX_CONCURRENCY;
		while(i-- != 0)
			mutex[i] = new Object();
	}
}

