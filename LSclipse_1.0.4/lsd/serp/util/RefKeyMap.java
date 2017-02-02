package serp.util;


import java.lang.ref.*;
import java.util.*;


/**
 *	<p>Abstract base class for maps whose keys are stored as weak or soft
 *	references.  This class is more flexible than the standard
 *	{@link WeakHashMap} because it also allows soft references and because it
 *	can inherit the functionality of any other map type.  For example, it can
 *	use JVM identity-based hashing if constructed with an 
 *	{@link IdentityMap} or maintain its entries in insertion order if
 *	constructed with a {@link LinkedHashMap}.</p>
 * 
 *	<p>Subclasses must define the {@link #createRefMapKey} method 
 *	only.  Expired values are removed from the map before any mutator methods;
 *	removing before	accessor methods can lead to 
 *	{@link ConcurrentModificationException}s.  Thus, the following methods may 
 *	produce results which include key/value pairs that have expired: 
 *	<ul>
 *	<li><code>size</code></li>
 *	<li><code>isEmpty</code></li>
 *	<li><code>containsKey</code></li>
 *	<li><code>keySet.size,contains,isEmpty</code></li>
 *	<li><code>entrySet.size,contains,isEmpty</code></li>
 *	<li><code>values.size,contains,isEmpty</code></li>
 *	</ul></p>
 *
 *	<p>By default, all methods are delegated to the internal map provided at
 *	construction.  Thus, the ordering, etc of the given	map will be preserved;
 *	however, the hashing algorithm cannot be duplicated.  A special case is 
 *	made for the {@link IdentityMap}'s hashing, which is supported.
 *	Performance is similar to that of the internal map instance.</p>
 *
 *	@author		Abe White
 */
abstract class RefKeyMap
	implements RefMap
{
	private Map 			_map 		= null;
	private ReferenceQueue	_queue		= new ReferenceQueue ();
	private boolean 		_identity	= false;
	


	/**
	 *	Equivalent to <code>RefKeyMap (new HashMap ())</code>.
	 */
	public RefKeyMap ()
	{
		this (new HashMap ());
	}


	/**
	 *	Construct a RefKeyMap with the given interal map.  The internal
	 *	map will be cleared.  It should not be accessed in any way after being
 	 *	given to this constructor; this map will 'inherit' its behavior, 
	 *	however.  For example, if the given map is a {@link LinkedHashMap}, 
	 *	the	<code>values</code> method of this map will return values in
 	 *	insertion order.
	 */
	public RefKeyMap (Map map)
	{
		// can't use an identity map internally; the hashing wouldn't work
		// but we can duplicate the functionality
		if (map instanceof IdentityMap)
		{
			_identity = true;
			_map = new HashMap ();
		}
		else
		{
			_map = map;
			_map.clear ();
		}
	}


	public boolean makeHard (Object key)
	{
		removeExpired ();

		// no key?
		if (!containsKey (key))
			return false;
		
		// remove ref and put actual key 
		Object value = remove (key);
		_map.put (key, value);
		return true;
	}


	public boolean makeReference (Object key)
	{
		removeExpired ();

		// can't make null keys references
		if (key == null)
			return false;

		// already contain ref?
		if (!containsKey (key))
			return false;

		Object value = remove (key);
		put (key, value);
		return true;
	}


	public void clear ()
	{
		_map.clear ();
	}


	public boolean containsKey (Object key)
	{
		if (key == null)
			return _map.containsKey (null);
		return _map.containsKey (createRefMapKey (key, null, _identity));
	}


	public boolean containsValue (Object value)
	{
		return _map.containsValue (value);
	}


	public Set entrySet ()
	{
		return new EntrySet ();
	}


	public boolean equals (Object other)
	{
		return _map.equals (other);
	}


	public Object get (Object key)
	{
		if (key == null)
			return _map.get (null);
		return _map.get (createRefMapKey (key, null, _identity));
	}


	public boolean isEmpty ()
	{
		return _map.isEmpty ();
	}


	public Set keySet ()
	{
		return new KeySet ();
	}


	public Object put (Object key, Object value)
	{
		removeExpired ();
		return putFilter (key, value);
	}


	public void putAll (Map map)
	{
		removeExpired ();

		Map.Entry entry;
		for (Iterator itr = map.entrySet ().iterator (); itr.hasNext ();)
		{
			entry = (Map.Entry) itr.next ();
			putFilter (entry.getKey (), entry.getValue ());
		}		
	}


	private Object putFilter (Object key, Object value)
	{
		if (key == null)
			return _map.put (null, value);

		// have to explicitly remove key, then add new one; otherwise 
		// backing map may choose to reuse key already in map
		key = createRefMapKey (key, _queue, _identity);
		Object ret = _map.remove (key);
		_map.put (key, value);
		return ret;
	}


	public Object remove (Object key)
	{
		removeExpired ();
		if (key == null)
			return _map.remove (null);
		return _map.remove (createRefMapKey (key, null, _identity));
	}


	public int size ()
	{
		return _map.size ();
	}


	public Collection values ()
	{
		return new ValueCollection ();
	}


	public String toString ()
	{
		return _map.toString ();
	}


	/**
	 *	Create a weak or soft reference to hold the given key.  In general,
 	 *	the returned reference should guarantee that its {@link Object#equals},
 	 *	{@link Object#hashCode} and {@link Comparable#compareTo} methods will 
 	 *	be delegated to the given key value.  
	 *
	 *	@param	key			the key value to hold; will not be null
	 *	@param	queue		the reference queue to place the reference in, or
 	 *						null if the reference should not be placed in 
	 *						a queue
	 *	@param	identity	if true, the {@link Object#equals} and 
	 *						{@link Object#hashCode} methods of the returned 
	 *						reference should work by the JVM identity of the
 	 *						key, not its corresponding methods
	 */
	protected abstract RefMapKey createRefMapKey (Object key,
		ReferenceQueue queue, boolean identity);


	private void removeExpired ()
	{
		for (Object key; (key = _queue.poll ()) != null;)
		{
			try
			{
				_queue.remove (1L);
			}
			catch (InterruptedException ie)
			{
			}
			_map.remove (key);
		}
	}


	/**
	 *	<p>Represents a key held by weak or soft reference.</p>
	 *	
 	 *	@author		Abe White
	 */
	static interface RefMapKey
		extends Comparable
	{
		/**
	 	 *	Return the referenced key.
		 */
		public Object getKey ();
	}


	/**
	 *	View of a single map entry.
	 */
	private static final class MapEntry
		implements Map.Entry
	{
		Map.Entry _entry = null;


		public MapEntry (Map.Entry entry)
		{
			_entry = entry;
		}

	
		public Object getKey ()
		{
			Object key = _entry.getKey ();
			if (!(key instanceof RefMapKey))
				return key;
			return ((RefMapKey) key).getKey ();
		}


		public Object getValue ()
		{
			return _entry.getValue ();
		}


		public Object setValue (Object value)
		{
			return _entry.setValue (value);
		}


		public boolean equals (Object other)
		{
			if (other == this)
				return true;
			if (!(other instanceof Map.Entry))
				return false;

			Object key = getKey ();
			Object key2 = ((Map.Entry) other).getKey ();
			if ((key == null && key2 != null)
				|| (key != null && !key.equals (key2)))
				return false;

			Object val = getValue ();
			Object val2 = ((Map.Entry) other).getValue ();
			return (val == null && val2 == null)
				|| (val != null && val2.equals (val2));
		}
	}


	/**
	 *	View of the entry set.
	 */
	private class EntrySet
		extends AbstractSet
	{
		public int size ()
		{
			return RefKeyMap.this.size ();
		}
	
	
		public boolean add (Object o)
		{
			Map.Entry entry = (Map.Entry) o;
			put (entry.getKey (), entry.getValue ());
			return true;
		}
	
	
		public Iterator iterator ()
		{
			return new EntryIterator ();
		}


		/**
		 *	Iterator that filters expired entries.
		 */
		private class EntryIterator
			extends LookaheadIterator
		{
			protected Iterator newIterator ()
			{
				return _map.entrySet ().iterator ();
			}


			protected void processValue (ItrValue value)
			{
				Map.Entry entry = (Map.Entry) value.value;

				if (entry.getKey () instanceof RefMapKey)
				{
					RefMapKey ref = (RefMapKey) entry.getKey ();
					if (ref.getKey () == null)
						value.valid = false;
				}
				value.value = new MapEntry (entry);
			}
		}
	}


	/**
	 *	View of the key set.
	 */
	private class KeySet
		extends AbstractSet
	{
		public int size ()
		{
			return RefKeyMap.this.size ();
		}
	
	
		public Iterator iterator ()
		{
			return new KeyIterator ();
		}


		/**
		 *	Iterator that filters expired keys.
		 */
		private class KeyIterator
			extends LookaheadIterator
		{
			protected Iterator newIterator ()
			{
				return _map.keySet ().iterator ();
			}


			protected void processValue (ItrValue value)
			{
				if (value.value instanceof RefMapKey)
				{
					RefMapKey ref = (RefMapKey) value.value;
					if (ref.getKey () == null)
						value.valid = false;
					else
						value.value = ref.getKey ();
				}
			}
		}
	}


	/**
	 *	View of the value collection.
	 */
	private class ValueCollection
		extends AbstractCollection
	{
		public int size ()
		{
			return RefKeyMap.this.size ();
		}
	
	
		public Iterator iterator ()
		{
			return new ValueIterator ();
		}


		/**
		 *	Iterator that filters expired values.
		 */
		private class ValueIterator
			extends LookaheadIterator
		{
			protected Iterator newIterator ()
			{
				return _map.entrySet ().iterator ();
			}


			protected void processValue (ItrValue value)
			{
				Map.Entry entry = (Map.Entry) value.value;

				if (entry.getKey () instanceof RefMapKey)
				{
					RefMapKey ref = (RefMapKey) entry.getKey ();
					if (ref.getKey () == null)
						value.valid = false;
				}
				value.value = entry.getValue ();
			}
		}
	}
}
