package serp.util;


import java.util.*;


/**
 *	<p>Pool implementation using object identity to maintain unique
 *	instances.  Taken instances are held weakly, so there is not an
 *	absolute requirement that all taken instances be manually returned to the 
 *	pool.</p>
 *	
 *	@author		Abe White
 */
public class IdentityPool
	extends AbstractPool
{
	private Set _free	= new MapSet (new IdentityMap ());
	private Map _taken	= new WeakKeyMap (new IdentityMap ());


	/**
	 *	@see	AbstractPool#AbstractPool()
	 */
	public IdentityPool ()
	{
		super ();
	}


	/**
	 *	@see	AbstractPool#AbstractPool(int,int,int,int)
	 */
	public IdentityPool (int min, int max, int wait, int autoReturn)
	{
		super (min, max, wait, autoReturn);
	}


	/**
	 *	@see	AbstractPool#AbstractPool(Collection)
	 */
	public IdentityPool (Collection c)
	{
		super (c);
	}

	
	protected Set freeSet ()
	{
		return _free;
	}


	protected Map takenMap ()
	{
		return _taken;
	}
}
