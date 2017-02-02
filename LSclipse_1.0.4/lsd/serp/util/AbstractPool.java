package serp.util;


import java.util.*;


/**
 *	<p>Abstract generic pool implementation.  Subclasses must implement the 
 *	{@link #freeSet} and {@link #takenMap} methods to return mutable views 
 *	of the free and taken pool instances, respectively.</p>
 *
 *	<p>This implementation is not synchronized.</p>
 *	
 *	@author		Abe White
 */
public abstract class AbstractPool
	implements Pool
{
	// comparator that always compares objects equal
	private static Comparator COMP_TRUE = new Comparator ()
	{
		public int compare (Object o1, Object o2)
		{
			return 0;
		}
	};

	// comparator that compares equal if the objects are equal according
	// to their equals() method
	private static Comparator COMP_EQUAL = new Comparator ()
	{
		public int compare (Object o1, Object o2)
		{
			if (o1 == o2 || (o1 != null && o2 != null && o1.equals (o2)))
				return 0;
			return (System.identityHashCode (o1) < System.identityHashCode (o2))
				? -1 : 1;
		}
	};

	private int _min		= 0;
	private int _max		= 0;
	private int	_wait		= 0;
	private int	_autoReturn	= 0;


	/**
	 *	Construct an empty pool with <code>min, max, wait, autoReturn</code> 
	 *	properties of 0.
	 */
	public AbstractPool ()
	{
	}


	/**
	 *	Construct a pool with the given properties.
	 *
	 *	@param	min			the minimum pool size, including taken instances
	 *	@param	max			the maximum pool size, including taken instances
	 *	@param	wait		the maximum number of milliseconds to wait for a
	 *						free instance
	 *	@param	autoReturn	the number of milliseconds after which taken
	 *						instances can be automatically reclaimed
	 */
	public AbstractPool (int min, int max, int wait, int autoReturn)
	{
		setMinPool (min);
		setMaxPool (max);
		setWait (wait);
		setAutoReturn (autoReturn);
	}


	/**
	 *	Construct a pool initialized with the given collection of free
	 *	instances.
	 */
	public AbstractPool (Collection c)
	{
		addAll (c);
	}


	public int getMaxPool ()
	{
		return _max;
	}


	public void setMaxPool (int max)
	{
		if (max < 0 || max < _min)
			throw new IllegalArgumentException (String.valueOf (max));
		_max = max;

		// trim to as close to max size as possible
		if (_max > 0)
		{
			int trim = size () + takenMap ().size () - _max;
			if (trim > 0)
			{
				Iterator itr = freeSet ().iterator ();
				for (int i = 0; i < trim && itr.hasNext (); i++)
				{
					itr.next ();
					itr.remove ();	
				}
			}
		}
	}


	public int getMinPool ()
	{
		return _min;
	}


	public void setMinPool (int min)
	{
		if (min < 0 || (_max > 0 && min > _max))
			throw new IllegalArgumentException (String.valueOf (min));
		_min = min;
	}


	public int getWait ()
	{
		return _wait;
	}


	public void setWait (int millis)
	{
		if (millis < 0)
			throw new IllegalArgumentException (String.valueOf (millis));
		_wait = millis;
	}


	public int getAutoReturn ()
	{
		return _autoReturn;
	}


	public void setAutoReturn (int millis)
	{
		if (millis < 0)
			throw new IllegalArgumentException (String.valueOf (millis));
		_autoReturn = millis;
	}


	public Iterator iterator ()
	{
		return new Iterator ()
		{
			private Iterator _itr = freeSet ().iterator ();


			public boolean hasNext ()
			{
				return _itr.hasNext ();
			}


			public Object next ()
			{
				return _itr.next ();
			}

			
			public void remove ()
			{
				if (size () + takenMap ().size () <= _min)
					throw new IllegalStateException ();

				_itr.remove ();
				synchronized (AbstractPool.this)
				{
					AbstractPool.this.notifyAll ();
				}
			}
		};
	}


	public int size ()
	{
		return freeSet ().size ();
	}


	public boolean isEmpty ()
	{
		return size () == 0;
	}


	public boolean contains (Object obj)
	{
		return freeSet ().contains (obj);
	}


	public boolean containsAll (Collection c)
	{
		return freeSet ().containsAll (c);
	}


	public Object[] toArray ()
	{
		return freeSet ().toArray ();
	}


	public Object[] toArray (Object[] fill)
	{
		return freeSet ().toArray (fill);
	}


	public boolean add (Object obj)
	{
		if (obj == null)
			return false;

		Map taken = takenMap ();
		boolean removed = takenMap ().remove (obj) != null;
		boolean added = (_max == 0 || size () + taken.size () < _max)
			&& freeSet ().add (obj);

		if (removed || added)
		{
			synchronized (this)
			{
				notifyAll ();
			}
		}

		return added;
	}


	public boolean addAll (Collection c)
	{
		boolean ret = false;
		for (Iterator itr = c.iterator (); itr.hasNext ();)
			ret = add (itr.next ()) || ret;

		return ret;	
	}


	public boolean remove (Object obj)
	{
		if (size () + takenMap ().size () <= _min)
			return false;

		if (freeSet ().remove (obj))
		{
			synchronized (this)
			{
				notifyAll ();
			}
			return true;
		}
		return false;
	}


	public boolean removeAll (Collection c)
	{
		boolean ret = false;
		for (Iterator itr = c.iterator (); itr.hasNext ();)
			ret = remove (itr.next ()) || ret;

		return ret;
	}


	public boolean retainAll (Collection c)
	{
		Object next;
		Collection remove = new LinkedList ();
		for (Iterator itr = freeSet ().iterator (); itr.hasNext ();)
		{
			next = itr.next ();
			if (!c.contains (next))
				remove.add (next);
		}
		return removeAll (remove);
	}


	public void clear ()
	{
		freeSet ().clear ();
		takenMap ().clear ();
		synchronized (this)
		{
			notifyAll ();
		}
	}


	public boolean equals (Object obj)
	{
		if (obj == this)
			return true;
		if (!(obj instanceof Pool))
			return false;

		Pool p = (Pool) obj;
		return p.size () == size () && p.containsAll (this);
	}


	public int hashCode ()
	{
		int sum = 0;
		Object next;
		for (Iterator itr = freeSet ().iterator (); itr.hasNext ();)
		{
			next = itr.next ();
			sum += (next == null) ? 0 : next.hashCode ();	
		}

		return sum;
	}


	/**
	 *	Return a free object from the pool.
 	 *
	 *	@see	#get(Object,Comparator)
 	 */
	public Object get ()
	{
		return get (null, COMP_TRUE);
	}


	/**
	 *	Return a free object from the pool that compares true using
 	 *	{@link Object#equals} to the given instance.
 	 *
	 *	@see	#get(Object,Comparator)
 	 */
	public Object get (Object match)
	{
		return get (match, COMP_EQUAL);
	}


	/**
	 *	Return a free object matching from the pool.  The object must match the
	 *	given instance according to the given {@link Comparator}.  All
	 *	other <code>get</code> methods are implemented in terms of this version.
	 *
	 *	@param	match	the object to compare to; may be null
	 *	@param	comp	the comparator to use; if null and <code>match</code> 
	 *					is null, any object will match; if null and 
	 *					<code>match</code> is not null,
	 *					matching will be based on the {@link Object#equals}
	 *					method
	 *	@throws			NoSuchElementException if no matching object can be
	 *					obtained in the set wait period
 	 */
	public Object get (Object match, Comparator comp)
	{
		if (comp == null && match == null)
			comp = COMP_TRUE;
		else if (comp == null)
			comp = COMP_EQUAL;

		Object obj = find (match, comp);
		if (obj != null)
			return obj;

		// no match; wait
		long now = System.currentTimeMillis ();
		long end = now + _wait;
		while (now < end)
		{
			// must synchronize to use the wait method
			synchronized (this)
			{
				try { wait (end - now); } catch (InterruptedException ie) {}
			}

			// check again for match
			obj = find (match, comp);
			if (obj != null)
				return obj;

			now = System.currentTimeMillis ();
		}

		throw new NoSuchElementException ();
	}


	public Set takenSet ()
	{
		return Collections.unmodifiableSet (takenMap ().keySet ());
	}


	/**
	 *	Locates a pooled object that matches the given instance according to
	 *	the given comparator.  The comparator will always be valid.  This
	 *	method is used by {@link #get} to find matching instances.  The pool
	 *	is first cleaned via the {@link #clean} method, then if the 
	 *	free set has any elements in it, it is iterated to find a matching 
	 *	instance.  Beore returning any matches the taken object is placed
	 *	in the taken set along with the current time.
	 *
	 *	@return		a matching instance, or null if none
	 */
	protected Object find (Object match, Comparator comp)
	{
		clean ();

		// none free?
		if (size () == 0)
			return null;

		// check for match
		Map taken = takenMap ();
		Object next = null;
		for (Iterator itr = freeSet ().iterator (); itr.hasNext ();)
		{
			next = itr.next ();
			if (comp.compare (match, next) == 0)
			{
				itr.remove ();
				taken.put (next, new Long (System.currentTimeMillis ()));
				return next;
			}
		}

		return null;
	}


	/**
	 *	Returns expired taken objects to the pool, and removes any null
	 *	entries from the taken set (can occur if the taken set map does not
	 *	hold strong references to its entries).
	 */
	protected void clean ()
	{
		// put expired taken objects back in the pool
		if (_autoReturn > 0)
		{
			Collection back = null;
			long now = System.currentTimeMillis ();
			Map.Entry entry;
			for (Iterator itr = takenMap ().entrySet ().iterator (); 
				itr.hasNext ();)
			{
				entry = (Map.Entry) itr.next ();

				if (entry.getKey () == null)
					itr.remove ();
				else if (((Long) entry.getValue ()).longValue () 
					+ _autoReturn < now)
				{
					if (back == null)
						back = new LinkedList ();
					back.add (entry.getKey ());
				}
			}

			// add expired elements back into pool
			if (back != null)
				addAll (back);
		}
	}


	/**
	 *	Provide a modifiable set view of the free pool instances.
	 */
	protected abstract Set freeSet ();


	/**
	 *	Provide a modifiable map view of the taken pool instances.  Each 
	 *	entry will be used to map a taken instance to a 
	 *	{@link Long} value representing the millisecond
	 *	time at which the key was taken.  Implementations are free to use
	 *	weak mappings.
	 */
	protected abstract Map takenMap ();
}
