package serp.util;


import java.util.*;


/**
 *	<p>Map type that hashes on object identity.  Mimics the functionality of
 *	the {@link IdentityHashMap} in Java 1.4.</p>
 *
 *	@author		Abe White
 */
public class IdentityMap
	extends HashMap
{
	/**
	 *	@see	HashMap#HashMap()
	 */
	public IdentityMap ()
	{
		super ();
	}

	
	/**
	 *	@see	HashMap#HashMap(int)
	 */
	public IdentityMap (int initialCapacity)
	{
		super (initialCapacity);
	}

	
	/**
	 *	@see	HashMap#HashMap(int,float)
	 */
	public IdentityMap (int initialCapacity, float loadFactor)
	{
		super (initialCapacity, loadFactor);
	}

	
	/**
	 *	@see	HashMap#HashMap(Map)
	 */
	public IdentityMap (Map map)
	{
		super ();
		putAll (map);
	}


	public Object clone ()
	{
		return new IdentityMap (this);
	}


	public boolean containsKey (Object key)
	{
		return super.containsKey (createKey (key));
	}


	public Set entrySet ()
	{
		return new EntrySet (super.entrySet ());
	}


	public Object get (Object key)
	{
		return super.get (createKey (key));
	}


	public Set keySet ()
	{
		return new KeySet (super.keySet ());
	}


	public Object put (Object key, Object value)
	{
		return super.put (createKey (key), value);
	}


	public void putAll (Map map)
	{
		Map.Entry entry;
		for (Iterator itr = map.entrySet ().iterator (); itr.hasNext ();)
		{
			entry = (Map.Entry) itr.next ();
			put (entry.getKey (), entry.getValue ());
		}		
	}


	public Object remove (Object key)
	{
		return super.remove (createKey (key));
	}


	/**
	 *	Creates an identity key from the given one.
	 */
	private static Object createKey (Object key)
	{
		if (key == null)
			return key;

		return new IdentityKey (key);
	}


	/**
	 *	Key type implementing identity hashing.
	 */
	private static class IdentityKey
	{
		private Object _key = null;

		
		public IdentityKey (Object key)
		{
			_key = key;
		}


		public Object getKey ()
		{
			return _key;
		}


		public int hashCode ()
		{
			return System.identityHashCode (_key);
		}


		public boolean equals (Object other)
		{
			if (this == other)
				return true;
			if (!(other instanceof IdentityKey))
				return false;

			return ((IdentityKey) other).getKey () == _key;
		}
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
			IdentityKey key = (IdentityKey) _entry.getKey ();
			if (key == null)
				return null;
			
			return key.getKey ();
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
		private Set _entrySet = null;

		
		public EntrySet (Set entrySet)
		{
			_entrySet = entrySet;
		}


		public int size ()
		{
			return _entrySet.size ();
		}
	
	
		public boolean add (Object o)
		{
			Map.Entry entry = (Map.Entry) o;
			return _entrySet.add (new MapEntry (entry));
		}
	
	
		public Iterator iterator ()
		{
			return new Iterator ()
			{
				Iterator _itr = _entrySet.iterator ();
	
	
				public boolean hasNext ()
				{
					return _itr.hasNext ();
				}
	
				
				public Object next ()
				{
					return new MapEntry ((Map.Entry) _itr.next ());
				}
	
	
				public void remove ()
				{
					_itr.remove ();
				}
			};
		}
	}


	/**
	 *	View of the key set.
	 */
	private class KeySet
		extends AbstractSet
	{
		private Set _keySet = null;

	
		public KeySet (Set keySet)
		{
			_keySet = keySet;
		}


		public int size ()
		{
			return _keySet.size ();
		}


		public boolean remove (Object rem)
		{
			for (Iterator itr = _keySet.iterator (); itr.hasNext ();)
			{
				if (((IdentityKey) itr.next ()).getKey () == rem)
				{
					itr.remove ();
					return true;
				}
			}
			return false;
		}
	
	
		public Iterator iterator ()
		{
			return new Iterator ()
			{
				private Iterator _itr = _keySet.iterator ();
	
	
				public boolean hasNext ()
				{
					return _itr.hasNext ();
				}
	
	
				public Object next ()
				{
					IdentityKey key = (IdentityKey) _itr.next ();
					if (key == null)
						return null;
	
					return key.getKey ();
				}
	
	
				public void remove ()
				{
					_itr.remove ();
				}
			};
		}
	}
}
