package serp.util;


import java.lang.ref.*;
import java.util.*;


/**
 *	<p>Map implementation in which the keys are held as weak references.</p>
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
public class WeakKeyMap
	extends RefKeyMap
{
	/**
	 *	Equivalent to <code>WeakKeyMap (new HashMap ())</code>.
	 */
	public WeakKeyMap ()
	{
		super ();
	}


	/**
	 *	Construct a WeakKeyMap with the given interal map.  The internal
	 *	map will be cleared.  It should not be accessed in any way after being
 	 *	given to this constructor; this Map will 'inherit' its behavior, 
	 *	however.  For example, if the given map is a {@link LinkedHashMap}, 
	 *	the	<code>values</code> method of this map will return values in
 	 *	insertion order.
	 */
	public WeakKeyMap (Map map)
	{
		super (map);
	}


	protected RefMapKey createRefMapKey (Object key, ReferenceQueue queue, 
		boolean identity)
	{
		if (queue == null)
			return new WeakMapKey (key, identity);
		else
			return new WeakMapKey (key, queue, identity);
	}


	/**
 	 *	Struct holding the referenced key in a weak reference.
 	 */
	private static final class WeakMapKey 
		extends WeakReference
		implements RefMapKey
	{
		private boolean	_identity 	= false;
		private int		_hash		= 0;


		public WeakMapKey (Object key, boolean identity)
		{
			super (key);
			_identity = identity;
			_hash = (identity) ? System.identityHashCode(key) : key.hashCode ();
		}


		public WeakMapKey (Object key, ReferenceQueue queue, boolean identity)
		{
			super (key, queue);
			_identity = identity;
			_hash = (identity) ? System.identityHashCode(key) : key.hashCode ();
		}


		public Object getKey ()
		{
			return get ();
		}


		public int hashCode ()
		{
			return _hash;
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

			if (key == null)
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

