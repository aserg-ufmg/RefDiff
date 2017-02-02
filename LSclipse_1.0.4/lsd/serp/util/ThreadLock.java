package serp.util;


import java.io.*;


/**
 *	<p>Lock implementation.  The lock is aware of the thread that owns it, and 
 *	allows that thread to {@link #lock} multiple times without blocking.  Only
 *	the owning thread can {@link #unlock}, and the lock will not be released to
 *	other threads until {@link #unlock} has been called the same number of 
 *	times as {@link #lock}.</p>
 *
 *	<p>Using this lock is similar to synchronizing on an object, but is more
 *	flexible (for example, the calls to {@link #lock} and {@link #unlock} can
 *	be surrounded by if statements).</p>
 *
 *	<p>Note that the lock resets on serialization.</p>
 *
 *	@author		Abe White
 */
public class ThreadLock
	implements Serializable
{
	private transient int		_count	= 0;
	private transient Thread 	_owner 	= null;

	
	/**
	 *	Atomically lock.  Blocks until the lock is available.
	 */
	public synchronized void lock ()
	{
		Thread thread = Thread.currentThread ();
		if (thread != _owner)
			while (_count > 0)
				try { wait (); } catch (InterruptedException ie) {}

		_count++;
		_owner = thread;
	}


	/**
	 *	Atomically lock.  Blocks until the lock is available or a timeout
	 *	occurs.
	 *
	 *	@para	timeout		the number of milliseconds to wait before timing out
	 *	@return				true if the lock was obtained, false on timeout
	 */
	public synchronized boolean lock (long timeout)
	{
		// use version that doesn't need to check time; more efficient
		if (timeout == 0)
		{
			lock ();
			return true;
		}

		Thread thread = Thread.currentThread ();
		if (thread != _owner && _count > 0)
		{
			long time = System.currentTimeMillis ();
			long end = time + timeout;
			while (_count > 0 && time < end)
			{
				try
				{
					wait (end - time);
				}
				catch (InterruptedException ie)
				{
				}
				time = System.currentTimeMillis ();
			}
		}

		if (thread != _owner && _count > 0)
			return false;

		_count++;
		_owner = thread;
		return true;
	}


	/**
	 *	Releases the lock.  This method can only be called by the owning
	 *	thread.
	 *
	 *	@throws	IllegalStateException if current thread is not owner
	 */
	public synchronized void unlock ()
	{
		Thread thread = Thread.currentThread ();
		if (thread != _owner)
			throw new IllegalStateException ();
		
		_count--;
		if (_count == 0)
			notify ();
	}


	/**
	 *	Return true if this lock is locked.
	 */
	public boolean isLocked ()
	{
		return _count > 0;
	}
}
