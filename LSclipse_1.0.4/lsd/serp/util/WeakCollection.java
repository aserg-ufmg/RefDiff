package serp.util;


import java.lang.ref.*;
import java.util.*;


/**
 *	<p>Collection implementation in which the values are held as weak 
 *	references.</p>
 *
 *	<p>Expired values are removed from the collection before any mutator 
 *	methods; removing before	accessor methods can lead to 
 *	{@link ConcurrentModificationException}s.  Thus, the following methods may 
 *	produce results which include values that have expired:
 *	<ul>
 *	<li><code>size</code></li>
 *	<li><code>isEmpty</code></li>
 *	</ul></p>
 *
 *	<p>By default, all methods are delegated to the internal collection 
 *	provided at	construction.  Thus, the ordering, etc of the given	collection 
 *	will be preserved.  A special case is also made for {@link MapSet}s using
 *	identity hashing, which is supported.</p>
 *
 *	<p>Performance is similar to that of the internal collection instance.</p>
 *
 *	@author		Abe White
 */
public class WeakCollection
	extends RefValueCollection
{
	/**
	 *	Equivalent to <code>WeakCollection (new LinkedList ())</code>.
	 */
	public WeakCollection ()
	{
		super ();
	}


	/**
	 *	Construct a WeakCollection with the given interal collection.  The 
	 *	internal collection will be cleared.  It should not be accessed in any
	 *	way after being given to this constructor; this collection will 
	 *	'inherit' its behavior, however.  For example, if the given collection 
	 *	is a {@link TreeSet}, the elements will be maintained in natural
	 *	order and will not allow duplicates.
	 */
	public WeakCollection (Collection coll)
	{
		super (coll);
	}


	protected RefValue createRefValue (Object value, ReferenceQueue queue,
		boolean identity)
	{
		if (queue == null)
			return new WeakValue (value, identity);
		else
			return new WeakValue (value, queue, identity);
	}


	/**
 	 *	Struct holding the referenced value in a weak reference.
 	 */
	private static final class WeakValue 
		extends WeakReference
		implements RefValue
	{
		private boolean _identity = false;


		public WeakValue (Object value, boolean identity)
		{
			super (value);
			_identity = identity;
		}


		public WeakValue (Object value, ReferenceQueue queue,
			boolean identity)
		{
			super (value, queue);
			_identity = identity;
		}


		public Object getValue ()
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

			if (other instanceof RefValue)
				other = ((RefValue) other).getValue ();
				
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
			
			Object value = getValue ();
			Object otherValue;
			if (other instanceof RefValue)
				otherValue = ((RefValue) other).getValue ();
			else
				otherValue = other;

			if (value == null && otherValue == null)
				return 0;
			if (value == null && otherValue != null)
				return -1;
			if (otherValue == null)
				return 1;

			if (!(value instanceof Comparable))
				return System.identityHashCode (otherValue) 
					- System.identityHashCode (value);
			
			return ((Comparable) value).compareTo (otherValue);
		}
	}		
}

