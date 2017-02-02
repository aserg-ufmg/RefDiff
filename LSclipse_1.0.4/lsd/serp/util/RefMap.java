package serp.util;


import java.util.*;


/**
 *	<p>Interface implemented by maps that support weak or soft references for
 *	their keys or values.</p>
 *
 *	@author		Abe White
 */
public interface RefMap
	extends Map
{
	/**
	 *	Harden the reference for the given key.  This will ensure that the
	 *	key and the value it corresponds to are not garbage collected.
	 *	Note that this is a mutator method and can result in 
	 *	{@link ConcurrentModificationException}s being thrown by any
	 *	iterator in use while this method is called.
	 *
	 *	@return		true if the reference to the key is now hard; false if the
	 *				key does not exist in the map (or has already expired)
	 */
	public boolean makeHard (Object key);	


	/**
	 *	Soften the reference for the given key.  This will allow the key and
	 *	the value it corresponds to can be expired from the map, and the
	 *	key/value garbage collected.  This is the default for all new key/
	 *	value pairs added to the map.
	 *	Note that this is a mutator method and can result in 
	 *	{@link ConcurrentModificationException}s being thrown by any
	 *	iterator in use while this method is called.
	 *
	 *	@return		true if the reference to the key/value is now soft; false
	 *				if the key does not exist or the key/value cannot be
	 *				maintained in a reference (as for nuill values)
	 */
	public boolean makeReference (Object key);
}
