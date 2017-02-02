package serp.util;


import java.util.*;


/**
 *	<p>Iterator type used that looks ahead one element to allow dynamic
 *	removal of invalid elements from iteration.</p>
 *
 *	<p>The use of this class is perhaps best illustrated by an example from
 *	within this package.  The {@link RefCollection} type and its subclasses use
 *	this iterator type to ensure that references that have expired are not 
 *	returned to the user.</p>
 *
 *	<p>Note that the {@link #remove} method takes linear time, as a new
 *	iterator must be fetched and iterated to 1 behind the present position
 *	before the change is applied.</p>
 *
 *	@author		Abe White
 */
public abstract class LookaheadIterator
	implements Iterator
{
	private Iterator	_itr	= null;
	private ItrValue	_next	= new ItrValue ();

	// index of last and current valid values
	private int _last 	= -1;
	private int _index	= -1;


	/**
	 *	Return the internal iterator.  Note that this iterator changes
	 *	after calls to {@link #remove}.
	 */
	public Iterator getIterator ()
	{
		initialize ();
		return _itr;
	}


	public boolean hasNext ()
	{
		initialize ();
		return _next.valid;	
	}


	public Object next ()
	{
		initialize ();
		if (!_next.valid)
			throw new NoSuchElementException ();

		Object next = _next.value;
		setNext ();
		return next;
	}
	
	
	public void remove ()
	{
		initialize ();

		// run through the list to the element just before our lookahead
		Iterator itr = newIterator ();
		for (int i = 0; i <= _last; i++)
			itr.next ();

		// find the previous item that is valid
		itr.remove ();

		// reset itr to prevent concurrent mod exception
		_index = _last - 1;
		_itr = itr;
		setNext ();

		// set to -1; can't call remove twice in a row
		_last = -1;
	}


	/**
	 *	Implement this method to return an iterator over the actual elements
	 *	of the collection.  If the {@link #remove} method is not overridden,
	 *	this method may be called multiple times and should return a new
	 *	iterator placed at the beginning of the collection each time.
	 */
	protected abstract Iterator newIterator ();


	/**
	 *	Implement this method to properly setup the given {@link ItrValue} 
	 *	instance.  The {@link ItrValue#value} field will be set with the
	 *	last value returned by the internal iterator given by 
	 *	{@link #newIterator}.  This value can be replaced if desired to 
	 *	return some other value on iteration.  Or, set the 
	 *	{@link ItrValue#valid} field to false to skip this value in the 
	 *	iteration.
	 */
	protected abstract void processValue (ItrValue val);


	/**
	 *	Initializes the iterator before use.  Cannot do this in the constructor
	 *	because the subclass constructor won't have been executed yet, so
	 *	the {@link #newIterator} method might fail.
	 */
	private void initialize ()
	{
		if (_itr == null)
		{
			_itr = newIterator ();
			setNext ();
		}
	}


	/**
	 *	Sets the internal next holder to the next valid object to be returned.
	 */
	private void setNext ()
	{
		_next.value = null;
		_next.valid = false;

		// find the next valid object
		int index = _index;
		while (_itr.hasNext ())
		{
			_next.value = _itr.next ();
			_next.valid = true;
			index++;

			processValue (_next);
			if (_next.valid)
				break;
		}
		_last = _index;
		_index = index;	
	}


	/**
	 *	<p>This struct holds information about an item to return
	 *	from the iterator.  Instances can be modified directly to change the
	 *	value that will be returned from iteration or to mark it invalid.</p>
	 *
	 *	@author		Abe White
	 */
	public static class ItrValue
	{
		/**
	 	 *	The item to return during iteration.
	 	 */
		public Object value = null;

		/**
		 *	If true, the above item is valid; if false, it is not and will 
	 	 *	be skipped during iteration.
		 */	
		public boolean valid = false;
	}
}
