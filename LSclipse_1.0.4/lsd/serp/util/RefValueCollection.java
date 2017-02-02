package serp.util;


import java.lang.ref.*;
import java.util.*;


/**
 *	<p>Abstract base class for collections whose values are stored as weak or 
 *	soft references.</p>
 * 
 *	<p>Subclasses must define the {@link #createRefValue} method only.  Expired
 *	values are removed from the collection before any mutator methods;
 *	removing before	accessor methods can lead to 
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
 *	identity hashing, which is supported.
 *	Performance is similar to that of the internal collection instance.</p>
 *
 *	@author		Abe White
 */
abstract class RefValueCollection
	implements RefCollection
{
	private Collection		_coll 		= null;
	private ReferenceQueue	_queue		= new ReferenceQueue ();
	private boolean 		_identity	= false;


	/**
	 *	Equivalent to <code>RefValueCollection (new LinkedList ())</code>.
	 */
	public RefValueCollection ()
	{
		this (new LinkedList ());
	}


	/**
	 *	Construct a RefCollection with the given interal collection.  The 
	 *	internal collection will be cleared.  It should not be accessed in any
	 *	way after being given to this constructor; this collection will 
	 *	'inherit' its behavior, however.  For example, if the given collection 
	 *	is a {@link TreeSet}, the elements will be maintained in natural
	 *	order and will not allow duplicates.
	 */
	public RefValueCollection (Collection coll)
	{
		// can't use an identity coll internally; the hashing wouldn't work
		// but we can duplicate the functionality
		if (coll instanceof MapSet && ((MapSet) coll).isIdentity ())
		{
			_identity = true;
			_coll = new HashSet ();
		}
		else
		{
			_coll = coll;
			_coll.clear ();
		}
	}


	public boolean makeHard (Object obj)
	{
		removeExpired ();
		
		// replace with actual value; if list maintain position
		if (_coll instanceof List)
		{
			Object value;
			for (ListIterator li = ((List) _coll).listIterator(); li.hasNext();)
			{
				value = li.next ();
				if (equal (obj, value))
				{
					li.set (obj);
					return true;
				}
			}
		}
		else if (remove (obj))
		{
			_coll.add (obj);
			return true;
		}

		return false;
	}


	public boolean makeReference (Object obj)
	{
		removeExpired ();
		
		// can't make a reference from null
		if (obj == null)
			return false;

		// replace with reference value; if list maintain position
		Object value;
		if (_coll instanceof List)
		{
			for (ListIterator li = ((List) _coll).listIterator(); li.hasNext();)
			{
				value = li.next ();
				if (equal (obj, value))
				{
					li.set (createRefValue (obj, _queue, _identity));
					return true;
				}
			}
		}
		else
		{
			for (Iterator itr = _coll.iterator (); itr.hasNext ();)
			{
				value = itr.next ();
				if (equal (obj, value))
				{
					itr.remove ();
					add (obj);
					return true;
				}	
			}	
		}
		return false;
	}


	/**
	 *	Tests if a new object and a stored value are equal according to
	 *	the current comparison critieria (dependent on the identity setting).
	 */
	private boolean equal (Object obj, Object value)
	{
		if (value instanceof RefValue)
			value = ((RefValue) value).getValue ();

		return ((_identity || obj == null) && obj == value)
			|| (!_identity && obj != null && obj.equals (value));
	}


	public boolean add (Object obj)
	{
		removeExpired ();
		return addFilter (obj);
	}


	public boolean addAll (Collection objs)
	{
		removeExpired ();

		boolean added = false;
		for (Iterator itr = objs.iterator (); itr.hasNext ();)
			added = added || addFilter (itr.next ());
		return added;
	}


	private boolean addFilter (Object obj)
	{
		if (obj == null)
			return _coll.add (null);
		return _coll.add (createRefValue (obj, _queue, _identity));	
	}


	public void clear ()
	{
		_coll.clear ();
	}


	public boolean contains (Object obj)
	{
		if (obj == null)
			return _coll.contains (null);
		return _coll.contains (createRefValue (obj, null, _identity));
	}


	public boolean containsAll (Collection objs)
	{
		boolean contains = true;
		for (Iterator itr = objs.iterator (); contains && itr.hasNext ();)
			contains = contains (itr.next ());
		return contains;
	}


	public boolean equals (Object other)
	{
		return _coll.equals (other);
	}


	public boolean isEmpty ()
	{
		return _coll.isEmpty ();
	}


	public boolean remove (Object obj)
	{
		removeExpired ();
		return removeFilter (obj);
	}
	

	public boolean removeAll (Collection objs)
	{
		removeExpired ();

		boolean removed = false;
		for (Iterator itr = objs.iterator (); itr.hasNext ();)
			removed = removed || removeFilter (itr.next ());
		return removed;
	}


	public boolean retainAll (Collection objs)
	{
		removeExpired ();

		boolean removed = false;
		for (Iterator itr = iterator (); itr.hasNext ();)
		{
			if (!objs.contains (itr.next ()))
			{
				itr.remove ();
				removed = true;
			}
		}

		return removed;
	}


	private boolean removeFilter (Object obj)
	{
		if (obj == null)
			return _coll.remove (null);
		return _coll.remove (createRefValue (obj, null, _identity));
	}


	public int size ()
	{
		return _coll.size ();
	}	


	public Object[] toArray ()
	{
		// not too efficient
		ArrayList list = new ArrayList (size ());
		for (Iterator itr = iterator (); itr.hasNext ();)
			list.add (itr.next ());
		
		return list.toArray ();
	}


	public Object[] toArray (Object[] a)
	{
		// not too efficient
		ArrayList list = new ArrayList (size ());
		for (Iterator itr = iterator (); itr.hasNext ();)
			list.add (itr.next ());
		
		return list.toArray (a);
	}


	public Iterator iterator ()
	{
		return new ValuesIterator ();
	}


	/**
	 *	Create a weak or soft reference to hold the given value.  In general,
	 *	the returned reference should guarantee that its {@link Object#equals},
	 *	{@link Object#hashCode}, and {#link Comparable#compareTo} methods
	 *	will be delegated to the given value.
	 *
	 *	@param	value		the value to hold; will not be null
	 *	@param	queue		the reference queue to place the reference in, or 
	 *						null if the reference should not be placed in a 
	 *						queue
	 *	@param	identity	if true, the {@link Object#equals} and 
	 *						{@link Object#hashCode} methods of the returned
	 *						reference should work by the JVM identity of the
	 *						value, not its corresponding methods
	 */
	protected abstract RefValue createRefValue (Object value, 
		ReferenceQueue queue, boolean identity);


	private void removeExpired ()
	{
		for (Object value; (value = _queue.poll ()) != null;)
		{
			try
			{
				_queue.remove (1L);
			}
			catch (InterruptedException ie)
			{
			}
			_coll.remove (value);
		}	
	}


	/**
	 *	<p>Represents a value held by weak or soft reference.</p>
	 *	
	 *	@author		Abe White
	 */
	static interface RefValue
	{
		/**
		 *	Return the referenced value.
	 	 */
		public Object getValue ();
	}


	/**
	 *	Iterates over the contained values, skipping expired references.
	 */
	private class ValuesIterator
		extends LookaheadIterator
	{
		protected Iterator newIterator ()
		{
			return _coll.iterator ();
		}


		protected void processValue (ItrValue value)
		{
			if (value.value instanceof RefValue)
			{
				RefValue ref = (RefValue) value.value;
				if (ref.getValue () == null)
					value.valid = false;
				else
					value.value = ref.getValue ();
			}
		}
	}
}
