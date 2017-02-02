package serp.util;


import java.io.*;
import java.util.*;


/**
 *	<p>Static methods that operate on pools.</p>
 *
 *	@author		Abe White
 */
public class Pools
{
	/**
	 *	Return a synchronized pool backed by the given instance.
	 */
	public static Pool synchronizedPool (Pool pool)
	{
		if (pool == null)
			throw new NullPointerException ();
		return new SynchronizedPool (pool);
	}


	/**
	 *	Synchronized wrapper for Pools.
	 */
	private static class SynchronizedPool
		implements Pool, Serializable
	{
		private Pool _pool = null;


		public SynchronizedPool (Pool pool)
		{
			_pool = pool;
		}


		public synchronized int getMaxPool ()
		{
			return _pool.getMaxPool ();
		}


		public synchronized void setMaxPool (int max)
		{
			_pool.setMaxPool (max);
		}


		public synchronized int getMinPool ()
		{
			return _pool.getMinPool ();
		}


		public synchronized void setMinPool (int min)
		{
			_pool.setMinPool (min);
		}


		public synchronized int getWait ()
		{
			return _pool.getWait ();
		}


		public synchronized void setWait (int millis)
		{
			_pool.setWait (millis);
		}


		public synchronized int getAutoReturn ()
		{
			return _pool.getAutoReturn ();
		}


		public synchronized void setAutoReturn (int millis)
		{
			_pool.setAutoReturn (millis);
		}


		public Iterator iterator ()
		{
			return _pool.iterator ();
		}


		public synchronized int size ()
		{
			return _pool.size ();
		}


		public synchronized boolean isEmpty ()
		{
			return _pool.isEmpty ();
		}


		public synchronized boolean contains (Object obj)
		{
			return _pool.contains (obj);
		}


		public synchronized boolean containsAll (Collection c)
		{
			return _pool.containsAll (c);
		}


		public synchronized Object[] toArray ()
		{
			return _pool.toArray ();
		}


		public synchronized Object[] toArray (Object[] fill)
		{
			return _pool.toArray (fill);
		}


		public synchronized boolean add (Object obj)
		{
			return _pool.add (obj);
		}


		public synchronized boolean addAll (Collection c)
		{
			return _pool.addAll (c);
		}


		public synchronized boolean remove (Object obj)
		{
			return _pool.remove (obj);
		}


		public synchronized boolean removeAll (Collection c)
		{
			return _pool.removeAll (c);
		}


		public synchronized boolean retainAll (Collection c)
		{
			return _pool.retainAll (c);
		}


		public synchronized void clear ()
		{
			_pool.clear ();
		}


		public synchronized boolean equals (Object obj)
		{
			return _pool.equals (obj);
		}


		public synchronized int hashCode ()
		{
			return _pool.hashCode ();
		}


		public synchronized Object get ()
		{
			return _pool.get ();
		}


		public synchronized Object get (Object match)
		{
			return _pool.get (match);
		}


		public synchronized Object get (Object match, Comparator comp)
		{
			return _pool.get (match, comp);
		}

		
		public synchronized Set takenSet ()
		{
			return _pool.takenSet ();
		}
	}
}
