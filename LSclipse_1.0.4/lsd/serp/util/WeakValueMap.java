package serp.util;


import java.lang.ref.*;
import java.util.*;


/**
 *	<p>Map implementation in which the values are held as weak references.</p>
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
 *	construction.  Thus, the hashing algorithm, ordering, etc of the given
 *	map will be preserved.  Performance is similar to that of the internal
 *	map instance.</p>
 *
 *	@author		Abe White
 */
public class WeakValueMap
	extends RefValueMap
{
	/**
	 *	Equivalent to <code>WeakValueMap (new HashMap ())</code>.
	 */
	public WeakValueMap ()
	{
		super ();
	}


	/**
	 *	Construct a WeakValueMap with the given interal map.  The internal
	 *	map will be cleared.  It should not be accessed in any way after being
 	 *	given to this constructor; this map will 'inherit' its behavior, 
	 *	however.  For example, if the given map is a {@link LinkedHashMap}, 
	 *	the	{@link #values} method of this map will return values in
 	 *	insertion order.
	 */
	public WeakValueMap (Map map)
	{
		super (map);
	}


	protected RefMapValue createRefMapValue (Object key, Object value, 
		ReferenceQueue queue)
	{
		return new WeakMapValue (key, value, queue);
	}


	/**
 	 *	Struct holding the cached value in a weak reference, along with its
 	 *	key so that we can remove it when it expires.
 	 */
	private static final class WeakMapValue 
		extends WeakReference
		implements RefMapValue
	{
		private Object 	_key 	= null;
		private boolean _valid	= true;


		public WeakMapValue (Object key, Object value, ReferenceQueue queue)
		{
			super (value, queue);
			_key = key;
		}


		public Object getKey ()
		{
			return _key;
		}
	
	
		public Object getValue ()
		{
			return get ();
		}
	

		public boolean isValid ()
		{
			return _valid;
		}


		public void invalidate ()
		{
			_valid = false;
		}

	
		public boolean equals (Object other)
		{
			if (this == other)
				return true;
			if (other == null)
				return false;
	
			if (!(other instanceof WeakMapValue))
				return get () != null && get ().equals (other);
			else 
				return get () != null && get ().equals 
					(((WeakMapValue) other).get ());	
		}
	}
}	

