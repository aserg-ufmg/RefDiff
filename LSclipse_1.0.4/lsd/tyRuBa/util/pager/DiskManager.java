/*
 * Created on Jul 29, 2004
 */
package tyRuBa.util.pager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import serp.util.Semaphore;
import tyRuBa.util.Aurelizer;
import tyRuBa.util.DoubleLinkedList;
import tyRuBa.util.pager.Pager.ResourceId;
import tyRuBa.util.pager.Pager.Resource;

/**
 * Manages disk reads and writes. Writes are done in a separate thread as to
 * allow other operations to occur concurrently. A resource cannot be written
 * out if it is currently being read and vice versa.
 * @category FactBase
 * @author riecken
 */
public class DiskManager extends Thread {

    /** Maxiumum size that the taskQueue can be. */
    private int maxSize;

    /** Semaphore to ensure mutual exclusion on queue operations. */
    Semaphore queueMutex;

    /**
     * Semaphore that will block the write operation until there is room in the
     * queue to add another task.
     */
    Semaphore queueAvailable;

    /**
     * Semaphore that will block the writer thread until there is something in
     * the task queue.
     */
    Semaphore queueSize;

    /**
     * Semaphore to ensure mutual exclusion on operations on the resourceLocks
     * map.
     */
    Semaphore resourceLocksMutex;

    /** Flag that tells the thread when to die. */
    boolean alive;

    /** The task queue. */
    DoubleLinkedList taskQueue = new DoubleLinkedList();

    /** A Map of resourceId => semaphore that provide locks on resources. */
    Map resourceLocks = new HashMap();

    /**
     * number of times a read operation occured for a resource that was
     * currently in the page out queue.
     */
    public int couldHaveCanceledPageout;

    /** maximum size that the queue grew to. */
    private int highWaterMark = 0;

    /** Number of page out requests. */
    private int pageOutRequests = 0;

    /** Number of page in requests. */
    private int pageInRequests = 0;

    /** A write out task */
    private static class Task extends DoubleLinkedList.Entry {
        /** Resource to write out */
        Resource rsrc;

        /** Id for the resource. */
        ResourceId resourceID;

        /** Creates a new Task */
        Task(ResourceId resourceID, Resource rsrc) {
            this.resourceID = resourceID;
            this.rsrc = rsrc;
        }

        /** Write out the resource */
        void doIt() {
            try {
                OutputStream os = resourceID.writeResource();
                if (os != null) { //does resource support writing out
                    ObjectOutputStream oos = new ObjectOutputStream(os);
                    oos.writeObject(rsrc);
                    oos.close();
                }
            } catch (IOException e) {
                throw new Error("Could not page because of an IOException: " + e.getMessage());
            }
        }
    }

    /**
     * Creates a new DiskManager.
     * @param maxQueueSize maximum size of the task queue.
     */
    public DiskManager(int maxQueueSize) {
        maxSize = maxQueueSize;
        resourceLocksMutex = new Semaphore();
        queueMutex = new Semaphore();
        queueAvailable = new Semaphore(maxSize);
        queueSize = new Semaphore(0);
        alive = true;
    }
    
    /**
     * Ask the DiskManager whether it is currently idle. This method is intended
     * to be used to check whether it might now be a good time to perform some
     * pro-active saving of dirty resources.
     * 
     * It doesn't make much sense to queue up more work unless the DiskManager is
     * idle.
     */
    synchronized public boolean isIdle() {
    		return this.taskQueue.isEmpty();
    }
    
    /**
     * Write out thread run method. Pages out everything in the queue. Blocks
     * until there is something to do.
     */
    public void run() {
        while (alive) {
            //wait until there is something in the queue
            queueSize.down();
            if (!alive) {
                break;
            }
            queueMutex.down();
            Task nextTask = (Task) taskQueue.dequeue();
            queueMutex.up();
            //let whoever is waiting on queueAvailable to wake up because
            //there is room in the queue now
            queueAvailable.up();

            nextTask.doIt();
            releaseResourceLock(nextTask.resourceID);
        }
    }

    /**
     * Gets a lock on a resource.
     * @param resID resource to get the lock for.
     */
    void getResourceLock(ResourceId resID) {
        resourceLocksMutex.down();
        Semaphore lock = (Semaphore) resourceLocks.get(resID);
        if (lock == null) {
            lock = new Semaphore(1);
            resourceLocks.put(resID, lock);
        }
        resourceLocksMutex.up();
        lock.down();
    }

    /**
     * Releases a lock on a resource.
     * @param resID resource to release the lock for.
     */
    void releaseResourceLock(ResourceId resID) {
        resourceLocksMutex.down();
        Semaphore lock = (Semaphore) resourceLocks.get(resID);
        if (lock != null) {
            resourceLocks.remove(resID);
            lock.up();
        }
        resourceLocksMutex.up();
    }

    /**
     * Write out a resource to disk. Puts the resource on the task queue, then
     * returns immediately without actually having done the task yet.
     */
    public synchronized void writeOut(ResourceId resourceID, Resource rsrc) {
        Task task = new Task(resourceID, rsrc);
        getResourceLock(resourceID);
        queueAvailable.down();
        queueMutex.down();
        taskQueue.enqueue(task);
        queueMutex.up();
        queueSize.up();
        int waterLevel;
        if ((waterLevel = queueSize.getAvailable()) > highWaterMark)
            highWaterMark = waterLevel;
        pageOutRequests++;
    }

	/**
	 * Check whether a resource exists. Note that if a writeOut task for 
	 * this resource is in the queue the resource is considered to exist
	 * even though its actual representation on the storage device may
	 * not yet have been created.
	 */
	public synchronized boolean resourceExists(ResourceId rsrcID) {
		return (resourceLocks.get(rsrcID) != null) // Resource exists in the writeOut task queue
			|| rsrcID.resourceExists();
	}
	
    /** Read in a resource from disk. */
    public synchronized Resource readIn(ResourceId rsrcID) {
        pageInRequests++;
        Resource resource;
        if (resourceLocks.get(rsrcID) != null) {
            couldHaveCanceledPageout++;
        }
        getResourceLock(rsrcID);
        try {
            ObjectInputStream ois = new ObjectInputStream(rsrcID.readResource());
            resource = (Resource) ois.readObject();
        } catch (IOException e) {
            throw new Error("Could not page in because of IOException: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new Error("Could not page in because of ClassNotFoundException: " + e.getMessage());
        }
        releaseResourceLock(rsrcID);
        return resource;
    }

    /** Kill the write out thread */
    public synchronized void killMe() {
        alive = false;
        queueSize.up(); //make sure that the thread exits
    }

	/**
	 * Like kill, but more abrupt (used to simulate system crash).
	 */
	public void crash() {
		stop(); // stop thread abrubptly and unsafely
		taskQueue = null;
		resourceLocks = null;
		resourceLocksMutex = null;
		queueSize = null;
		queueMutex = null;
		queueAvailable = null;
	}
    
    /**
     * Forces the pending writeOuts to happen right now and waits for it to
     * complete.
     */
    public synchronized void flush() {
        while (queueSize.getAvailable() > 0) {
            try {
                sleep(500);
            } catch (InterruptedException e) {
                if (Aurelizer.debug_sounds != null)
                    Aurelizer.debug_sounds.enter("error");
                throw new Error("Don't interrupt me!!!");
            }
        }
    }

    /** Prints out some statistics about the diskmanager. */
    public void printStats() {
        System.err.println("Diskman.couldHaveCanceledPageout = " + couldHaveCanceledPageout);
        couldHaveCanceledPageout = 0;
        System.err.println("Diskman.biggestQueueSize = " + highWaterMark);
        highWaterMark = 0;
        System.err.println("Diskman.pageOutRequests = " + pageOutRequests);
        pageOutRequests = 0;
        System.err.println("Diskman.pageInRequests = " + pageInRequests);
        pageInRequests = 0;
    }


}