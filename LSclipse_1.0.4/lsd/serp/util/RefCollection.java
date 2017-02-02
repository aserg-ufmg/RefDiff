package serp.util;


import java.util.*;


/**
 *	<p>Interface implemented by collections that support weak or soft 
 *	references for their values.</p>
 *
 *	@author		Abe White
 */
public interface RefCollection
	extends Collection
{
	/**
	 *	Harden the reference for the given value.  This will ensure that the
	 *	value is not garbage collected.  Note that this is a mutator method 
	 *	and can result in {@link ConcurrentModificationException}s being 
	 *	thrown by any iterator in use while this method is called.
	 *
	 *	@return		true if the reference to the value is now hard; false if the
	 *				value does not exist in the collection (or has already 
	 *				expired)
	 */
	public boolean makeHard (Object obj);


	/**
	 *	Soften the reference for the given value.  This will allow the value
	 *	to be expired from the collection and garbage collected.
	 *	This is the default for all new values added to the collection.
	 *	Note that this is a mutator method and can result in 
	 *	{@link ConcurrentModificationException}s being thrown by any
	 *	iterator in use while this method is called.
	 *
	 *	@return		true if the reference to the value is now soft; false
	 *				if the value does not exist in the collection or cannot
	 *				be maintained in a reference (as for nuill values)
	 */
	public boolean makeReference (Object obj);
}
