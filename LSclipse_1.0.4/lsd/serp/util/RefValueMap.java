package serp.util;


import java.lang.ref.*;
import java.util.*;


/**
 *	<p>Abstract base class for maps whose values are stored as weak or soft
 *	references.</p>
 *
 *	<p>Subclasses must define the {@link #createRefMapValue} method 
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
 *	construction.  Thus, the hashing algorithm, ordering, etc of the given
 *	map will be preserved.  Performance is similar to that of the internal
 *	map instance.</p>
 *
 *	@author		Abe White
 */
abstract class RefValueMap
	implements RefMap
{
	private Map 			_map 	= null;
	private ReferenceQueue	_queue	= new ReferenceQueue ();


	/**
	 *	Equivalent to <code>RefValueMap (new HashMap ())</code>.
	 */
	public RefValueMap ()
	{
		this (new HashMap ());
	}


	/**
	 *	Construct a RefValueMap with the given interal map.  The internal
	 *	map will be cleared.  It should not be accessed in any way after being
 	 *	given to this constructor; this map will 'inherit' its behavior, 
	 *	however.  For example, if the given map is a {@link LinkedHashMap}, 
	 *	the	{@link #values} method of this map will return values in
 	 *	insertion order.
	 */
	public RefValueMap (Map map)
	{
		_map = map;
		_map.clear ();
	}


	public boolean makeHard (Object key)
	{
		removeExpired ();

		if (!containsKey (key))
			return false;

		Object value = _map.get (key);
		if (value instanceof RefMapValue)
		{
			RefMapValue ref = (RefMapValue) value;
			value = ref.getValue ();
			if (value == null)
				return false;

			ref.invalidate ();
			_map.put (key, value);
		}
		return true;
	}


	public boolean makeReference (Object key)
	{
		removeExpired ();

		Object value = _map.get (key);
		if (value == null)
			return false;
		if (!(value instanceof RefMapValue))
			put (key, value);
		return true;
	}


	public void clear ()
	{
		Collection values = _map.values ();
		Object value;
		for (Iterator itr = values.iterator (); itr.hasNext ();)
		{
			value = itr.next ();
			if (value instanceof RefMapValue)
				((RefMapValue) value).invalidate ();
			itr.remove ();
		}
	}


	public boolean containsKey (Object key)
	{
		return _map.containsKey (key);
	}


	public boolean containsValue (Object value)
	{
		return values ().contains (value);
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
		Object value = _map.get (key);
		if (!(value instanceof RefMapValue))
			return value;
		return ((RefMapValue) value).getValue ();
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

		Object replaced = putFilter (key, value);
		if (!(replaced instanceof RefMapValue))
			return replaced;
		return ((RefMapValue) replaced).getValue ();
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
		Object replaced;
		if (value == null)
			replaced = _map.put (key, null);
		else
			replaced = _map.put (key, createRefMapValue (key, value, _queue));

		if (replaced instanceof RefMapValue)
			((RefMapValue) replaced).invalidate ();
		return replaced;
	}


	public Object remove (Object key)
	{
		removeExpired ();

		Object value = _map.remove (key);
		if (!(value instanceof RefMapValue))
			return value;

		RefMapValue ref = (RefMapValue) value;
		ref.invalidate ();
		return ref.getValue ();
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
	 *	Create a weak or soft reference to hold the given value.
	 */
	protected abstract RefMapValue createRefMapValue (Object key, Object value,
		ReferenceQueue queue);


	private void removeExpired ()
	{
		for (RefMapValue ref; (ref = (RefMapValue) _queue.poll ()) != null;)
		{
			try
			{
				_queue.remove (1L);
			}
			catch (InterruptedException ie)
			{
			}
			if (ref.isValid ())
				_map.remove (ref.getKey ());
		}
	}


	/**
 	 *	<p>Interface representing a map entry whose value is stored using a 
	 *	weak or soft reference.</p>
	 *
 	 *	@author		Abe White
 	 */
	static interface RefMapValue
	{
		/**
		 *	Return the key the value is stored under.
		 */
		public Object getKey ();


		/**
		 *	Return the contained value.
		 */
		public Object getValue ();


		/**
	 	 *	Return true if the {@link #invalidate} method has not been called.
		 */
		public boolean isValid ();

		
		/**
	  	 *	Cause this instance to return <code>false</code> to all future
		 *	{@link #isValid} calls.
		 */
		public void invalidate ();
	}


	/**
	 *	View of a single map entry.
	 */
	private class MapEntry
		implements Map.Entry
	{
		Map.Entry _entry = null;


		public MapEntry (Map.Entry entry)
		{
			_entry = entry;
		}

	
		public Object getKey ()
		{
			return _entry.getKey ();
		}


		public Object getValue ()
		{
			Object value = _entry.getValue ();
			if (!(value instanceof RefMapValue))
				return value;
			return ((RefMapValue) value).getValue ();
		}


		public Object setValue (Object value)
		{
			Object ret = _entry.getValue ();
			if (value == null)
				_entry.setValue (null);
			else
				_entry.setValue (createRefMapValue (_entry.getKey (), 
					value, _queue));

			if (!(ret instanceof RefMapValue))
				return ret;

			RefMapValue ref = (RefMapValue) ret;
			ref.invalidate ();
			return ref.getValue ();
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
			return RefValueMap.this.size ();
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

				if (entry.getValue () instanceof RefMapValue)
				{
					RefMapValue ref = (RefMapValue) entry.getValue ();
					if (ref.getValue () == null)
						value.valid = false;
				}
				value.value = new MapEntry (entry);		
			}
		}
	}


	/**
	 *	View of the values collection.
	 */
	private class ValueCollection
		extends AbstractCollection
	{
		public int size ()
		{
			return RefValueMap.this.size ();
		}
	
	
		public Iterator iterator ()
		{
			return new ValueIterator ();
		}


		/**
	 	 *	Iterator type that filters expired values.
		 */
		private class ValueIterator
			extends LookaheadIterator
		{
			protected Iterator newIterator ()
			{
				return _map.values ().iterator ();
			}

		
			protected void processValue (ItrValue value)
			{
				if (value.value instanceof RefMapValue)
				{
					RefMapValue ref = (RefMapValue) value.value;
					if (ref.getValue () == null)
						value.valid = false;
					else
						value.value = ref.getValue ();
				}
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
			return RefValueMap.this.size ();
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
				return _map.entrySet ().iterator ();
			}


			protected void processValue (ItrValue value)
			{
				Map.Entry entry = (Map.Entry) value.value;

				if (entry.getValue () instanceof RefMapValue)
				{
					RefMapValue ref = (RefMapValue) entry.getValue ();
					if (ref.getValue () == null)
						value.valid = false;
				}
				value.value = entry.getKey ();
			}
		}
	}
}
