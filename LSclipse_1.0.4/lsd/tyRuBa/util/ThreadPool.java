package tyRuBa.util;

import java.util.LinkedList;

/**
 * @author cburns
 *
 * Thread pool class. Allows convenient asynchronous execution of Runnable objects without lots of 
 * relatively heavyweight calls to new Thread(runnable).start().
 * 
 * If the run method of the Runnable throws an unhandled exception or error, it will be caught and 
 * ignored.
 */
public class ThreadPool {
	
	private LinkedList jobs; //Runnable jobs waiting to be executed
	
	private Object mutex; //Lock for ensuring that everything runs smoothly.
	
	private int workers; //How many workers are currently running.
	
	private boolean halt = false;
	private boolean exit = false;
	
	private class Worker implements Runnable {
		
		public void run() {
			Runnable job;
			
			do {
				synchronized(mutex) {
					if(halt) { cleanUp(); return; } 
					
					while(jobs.size() == 0) {
						if(exit) { cleanUp(); return; } 
						
						try {
							mutex.wait();
						} catch(InterruptedException e) {}
						
						if(halt) { cleanUp(); return; }
					}
					
					job = (Runnable)jobs.removeFirst();
					
					try { 
						job.run();
					} catch(Throwable t) {}
				}
				
				job = null; //Make sure we don't retain the job against garbage collection.
				
			} while(true);
		}
		
		private void cleanUp() {
			workers--;
			mutex.notifyAll();
		}
	}
	
	/**
	 * Create a new ThreadPool with 
	 * @param workers
	 * threads.
	 */
	public ThreadPool(int workers) {
		if(workers <= 0)
			throw new IllegalArgumentException("There must be at least 1 worker thread");
		
		this.workers = workers;
		
		this.jobs = new LinkedList();
		
		this.mutex = jobs;
		
		int i = workers;
		while(i-- != 0)
			new Thread(new Worker()).start();
	}
	
	/**
	 * Queue
	 * @param job
	 * for execution. It will be run as once a thread becomes available.
	 */
	public void delegate(Runnable job) {
		synchronized(mutex) {
			if(halt || exit) throw new IllegalStateException("ThreadPool is being shutdown");
			
			jobs.addLast(job);
			mutex.notify();
		}
	}
	
	/**
	 * @return the number of jobs that are currently waiting. Actual value may be less if the calling thread
	 * has exclusive access, may be more or less if multiple threads can call delegate().
	 */
	public int waiting() {
		return jobs.size();
	}
	
	/**
	 * Terminate all threads once they return from their current jobs.
	 */
	public void halt() {
		synchronized(mutex) {
			halt = true;
			mutex.notifyAll();
		}
	}
	
	/**
	 * Terminate all threads once there are no more jobs waiting to be completed.
	 */
	public void exit() {
		synchronized(mutex) {
			exit = true;
			mutex.notifyAll();
		}
	}
	
	/**
	 * Same as exit(), but blocks until all jobs are finished and all threads are terminated.
	 */
	public void waitExit() {
		synchronized(mutex) {
			exit = true;
			mutex.notifyAll();
			
			while(workers != 0) {
				try {
					mutex.wait();
				} catch(InterruptedException e) {}
			}
		}
	}
	
	/**
	 * Same as halt(), but blocks until all threads are terminated (or are about to terminate).
	 */
	public void waitHalt() {
		synchronized(mutex) {
			halt = true;
			mutex.notifyAll();
			
			while(workers != 0) {
				try {
					mutex.wait();
				} catch(InterruptedException e) {}
			}
		}
	}
	
	protected void finalize() {
		exit();
	}
}
