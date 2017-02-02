package serp.util;


import java.lang.ref.*;
import java.util.*;


/**
 *	<p>Map implementation in which the keys are held as soft references.</p>
 *
 *	<p>Expired values are removed from the map before any mutator methods;
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
public class SoftKeyMap
	extends RefKeyMap
{
	/**
	 *	Equivalent to <code>SoftKeyMap (new HashMap ())</code>.
	 */
	public SoftKeyMap ()
	{
		super ();
	}


	/**
	 *	Construct a SoftKeyMap with the given interal map.  The internal
	 *	map will be cleared.  It should not be accessed in any way after being
 	 *	given to this constructor; this Map will 'inherit' its behavior, 
	 *	however.  For example, if the given map is a {@link LinkedHashMap}, 
	 *	the	<code>values</code> method of this map will return values in
 	 *	insertion order.
	 */
	public SoftKeyMap (Map map)
	{
		super (map);
	}


	protected RefMapKey createRefMapKey (Object key, ReferenceQueue queue,
		boolean identity)
	{
		if (queue == null)
			return new SoftMapKey (key, identity);
		else
			return new SoftMapKey (key, queue, identity);
	}


	/**
 	 *	Struct holding the referenced key in a soft reference.
 	 */
	private static final class SoftMapKey 
		extends SoftReference
		implements RefMapKey
	{
		private boolean _identity = false;


		public SoftMapKey (Object key, boolean identity)
		{
			super (key);
			_identity = identity;
		}


		public SoftMapKey (Object key, ReferenceQueue queue,
			boolean identity)
		{
			super (key, queue);
			_identity = identity;
		}


		public Object getKey ()
		{
			return get ();
		}


		public int hashCode ()
		{
			Object obj = get ();
			if (obj == null)
				return 0;

			if (_identity)
				return System.identityHashCode (obj);
			return obj.hashCode ();
		}


		public boolean equals (Object other)
		{
			if (this == other)
				return true;

			if (other instanceof RefMapKey)
				other = ((RefMapKey) other).getKey ();
				
			Object obj = get ();
			if (obj == null)
				return false;
			if (_identity)
				return obj == other;
			return obj.equals (other);
		}


		public int compareTo (Object other)
		{
			if (this == other)
				return 0;
			
			Object key = getKey ();
			Object otherKey;
			if (other instanceof RefMapKey)
				otherKey = ((RefMapKey) other).getKey ();
			else
				otherKey = other;

			if (key == null && otherKey == null)
				return 0;
			if (key == null && otherKey != null)
				return -1;
			if (otherKey == null)
				return 1;

			if (!(key instanceof Comparable))
				return System.identityHashCode (otherKey) 
					- System.identityHashCode (key);
			
			return ((Comparable) key).compareTo (otherKey);
		}
	}		
}

