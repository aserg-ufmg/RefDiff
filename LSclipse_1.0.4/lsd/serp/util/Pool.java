package serp.util;


import java.util.*;


/**
 *	<p>A pool of resources.</p>
 *
 *	<p>All methods inherited from the {@link Collection} refer to the free 
 *	members of the	pool.  The {@link #takenSet} method can be used to access
 *	the taken items.  In pools, the {@link Collection#add} and related methods 
 *	are overloaded to either add new pooled items or return taken instances.  
 *	Whether returning taken instances is required depends on the pool 
 *	implementation.</p>
 *
 *	<p>Unlike most collections, pools do not allow null values.</p>
 *
 *	@author		Abe White
 */
public interface Pool
	extends Set
{
	/**
	 *	Return the maximum number of pooled items, including taken instances.
	 *	A value of 0 indicates that there should be no maximum.  This property
	 *	defaults to 0.
	 */
	public int getMaxPool ();


	/**
	 *	Set the maximum number of pooled items, including taken instances.
	 *	A value of 0 indicates that there should be no maximum.  This property
	 *	defaults to 0.  If it is set to less than the current size of the pool,
	 *	the free instances will be trimmed to size, and subsequence 
	 *	{@link Collection#add} operations will return false until the maximum 
	 *	is met.
	 */
	public void setMaxPool (int max);


	/**
	 *	Return the minimum number of pooled items, including taken instances.
	 *	This property defaults to 0.  If greater than 0, 
	 *	{@link Collection#remove} methods may fail if the size would be trimmed
	 *	to under the minimum value.
	 */
	public int getMinPool ();


	/**
	 *	Set the minimum number of pooled items, including taken instances.
	 *	This property defaults to 0.  If set to greater than 0, 
	 *	{@link Collection#remove} operations may fail if the constraint would be
	 *	violated.  However, the {@link Collection#clear} operation will always
	 *	succeed.
	 */
	public void setMinPool (int min);


	/**
	 *	Return the maximum number of milliseconds the {@link #get} method will
	 *	wait for a free pool item.  This property defaults to 0.
	 */
	public int getWait ();


	/**
	 *	Set the maximum number of milliseconds the {@link #get} method will
	 *	wait for a free pool item.  This property defaults to 0.
	 */
	public void setWait (int millis);


	/**
	 *	Return the maximum number of milliseconds a pooled item can be taken
	 *	before it is automatically returned to the pool.
	 *	If this property is	set to 0, taken items will never be automatically
	 *	returned.  This	property defaults to 0.
	 */ 
	public int getAutoReturn ();


	/**
	 *	Return the maximum number of milliseconds a pooled item can be taken
	 *	before it is automatically returned to the pool.  If this property is
	 *	set to 0, taken items will never be automatically returned.  This
	 *	property defaults to 0.
	 */ 
	public void setAutoReturn (int millis);


	/**
	 *	Returns an iterator over the free elements of the pool.  The
	 *	iterator's {@link Iterator#remove} remove method may throw an
	 *	{@link IllegalStateException} if removing an element would
	 *	violoate the minimum pool size.
	 */
	public Iterator iterator ();


	/**
	 *	Return a free pooled instance.  This method does change the state of 
	 *	the pool, and thus should be considered a mutator.
	 */
	public Object get ();


	/**
	 *	Return a free pooled instance that compares equal to the given
	 *	object using the <code>equals</code> method.
	 *
	 *	@see	#get
	 */
	public Object get (Object match);


	/**
	 *	Return a free pooled instance that compares equal to the given 
	 *	object via the given comparator.
	 *
	 *	@see	#get
	 */
	public Object get (Object match, Comparator comp);


	/**
	 *	Return a read-only view of the set of taken instances.
	 */
	public Set takenSet ();


	/**
	 *	Pool equality should be implemented to compare the free elements
	 *	of the pool.
	 */
	public boolean equals (Object obj);


	/**
	 *	The hash code should be implemented to be consistent with equality.
	 */
	public int hashCode ();
}
