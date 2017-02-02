/*
 * Created on Jul 26, 2004
 *
 */
package tyRuBa.util.pager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import tyRuBa.util.DoubleLinkedList;

/**
 * This is an object that allows operations to be performed on files. The
 * responsibilty of the pager is to manage a queue of tasks and make sure that
 * tasks are only executed when the respective files are in memory. <br>
 * The pager is allowed to change the order in which tasks get executed, so it
 * can postpone the execution of tasks for which the files have not been paged
 * yet and give priority to tasks that can be executed immediately.
 * @category FactBase
 * @author riecken
 */
public class Pager {
	
	/** An Id for a resource */
	public static abstract class ResourceId {
		
		public abstract boolean equals(Object other);
		
		public abstract int hashCode();
		
		/** Open an input stream to read the resource. */
		public abstract InputStream readResource() throws IOException;
		
		/** Open an output stream to write to the resource. */
		public abstract OutputStream writeResource() throws IOException;
		
		/** Delete the resource. */
		public abstract void removeResource();
		
		/** Check if the resource exists in its storage location.
		 * Note that this is not supposed to take into account potentially
		 * pending resource creation tasks in the DiskMan task queue.
		 * 
		 * Therefore the only real safe way to check if a resource exists
		 * is to use the method on DiskMan (which calls this).
		 */
		public abstract boolean resourceExists();
		
	}
	
	/** A resource. */
	public static interface Resource extends Serializable {
		
	}
	
	/** A task to be done by the pager. */
	public static abstract class Task {
		
		/** Has the resource changed over the course of executing the task. */
		private boolean isChangedResource = false;
		
		/** The updated resource (if the resource has changed). */
		private Resource updatedResource = null;

		final private boolean mayChangeResource;
		
		public Task(boolean mayChangeResource) {
			super();
			this.mayChangeResource = mayChangeResource;
		}

		/**
		 * Performs a task on a resource. Note that rsrc can be null if the
		 * resource did not exist. In this case, the task may create a new
		 * resource, and then use the changedResource method to notify the pager
		 * that there is a new resource.
		 */
		public abstract Object doIt(Resource rsrc);
		
		/**
		 * Task.doIt may call changeResource to provide a replacement for the
		 * current resource.
		 * 
		 * Task.doIt may also perform side effect directly on the resource, but
		 * in this case they *must* call changedResource so the pager knows the
		 * resource is dirty.
		 */
		final protected void changedResource(Resource changedResource) {
			Assert.assertTrue(mayChangeResource);
			isChangedResource = true;
			updatedResource = changedResource;
		}
		
		/** Has the resource changed. */
		final boolean resourceIsChanged() {
			return isChangedResource;
		}
		
		/** Get the resource that changed. */
		public Resource getChangedResource() {
			return updatedResource;
		}

		public boolean mayChangeResource() {
			return mayChangeResource;
		}
	}
	
	/**
	 * Information about a reference that the pager uses to determine what gets
	 * paged out.
	 */
	private final class ResourceReferenceInfo extends DoubleLinkedList.Entry {
		
		/** The resource id. */
		private ResourceId resId;
		
		/** The resource. */
		private Resource resource;
		
		/** Whether the resource has been referenced. */
		private boolean referenced;
		
		/** When the resource was last referenced. */
		private long timeLastReferenced;
		
		/** Number of times the resource has been referenced since last paged. */
		private int numReferences;
		
		/** Whether the resource has been modified since the last page. */
		private boolean dirty;
		
		public ResourceReferenceInfo(ResourceId resId, Resource resource) {
			// Assert.assertNotNull(resource);
			this.resId = resId;
			this.resource = resource;
			this.referenced = false;
			this.timeLastReferenced = 0;
			this.dirty = false;
			this.numReferences = 0;
		}
		
		public ResourceId getResourceID() {
			return resId;
		}
		
		public Resource getResource() {
			return resource;
		}
		
		public void updateResource(Resource newResource) {
			// Assert.assertNotNull(newResource);
			this.resource = newResource;
		}
		
		public boolean isReferenced() {
			return referenced;
		}
		
		public void setReferenced(boolean referenced) {
			if (referenced == true) {
				incrementReferenceCounter();
				setTimeLastReferenced(System.currentTimeMillis());
			}
			this.referenced = referenced;
		}
		
		private void setTimeLastReferenced(long timeLastReferenced) {
			this.timeLastReferenced = timeLastReferenced;
		}
		
		private void incrementReferenceCounter() {
			this.numReferences++;
		}
		
		public void resetReferenceCounter() {
			this.numReferences = 0;
		}
		
		public boolean isDirty() {
			return dirty;
		}
		
		public void setDirty(boolean dirty) {
			synchronized (dirtyResources) {
				if (this.dirty == dirty)
					return;
				else {
					this.dirty = dirty;
					if (dirty)
						dirtyResources.add(this);
					else 
						dirtyResources.remove(this);
				}
			}
		}
		
		public int compareTo(Object o) {
			ResourceReferenceInfo other = (ResourceReferenceInfo) o;
			
			long myCompareNumber = this.timeLastReferenced + this.numReferences;
			long otherCompareNumber = other.timeLastReferenced + other.numReferences;
			
			if (otherCompareNumber > myCompareNumber) {
				return -1;
			} else if (otherCompareNumber < myCompareNumber) {
				return 1;
			} else {
				return 0;
			}
		}
		
		public String toString() {
			return "Rsrc("+resId+ (dirty?"=DIRTY":"") +")";
		}
	}
	
	/** Register last task time so we can detect if the QueryEngine is idle for some time 
	 * and take advantage to do some stuff, like cleaning pages. */
	private long lastTaskTime = System.currentTimeMillis();

	/** The maximum number of files the pager keeps in memory. */
	private int cacheSize;
	
	/** The map that tracks resources that are in memory. */
	private Map inMemory = new HashMap();
	
	/** A Set that tracks all dirty ResourceReferenceInfo objects. */
	private Set/*<ResourceReferenceInfo>*/ dirtyResources = new HashSet/*<ResourceReferenceInfo>*/();
	
	/** A Thread dedicated to writing out and reading from disk. */
	private DiskManager diskMan;
	
	/** Least Recently Used queue that is used to determine what gets paged out. */
	private DoubleLinkedList lruQueue;
	
	/** Flag to tell us whether we need to call backup. */
	private boolean needToCallBackup;
	
	private PageCleaner pageCleaner = null;
	
	/**
	 * Page cleaner is a thread which runs in background periodically checking
	 * if there are dirty resources and cleaning them (writing to disk) if 
	 * so. 
	 */
	class PageCleaner extends Thread {
		
		public PageCleaner() {
			super("tyRuBa.PageCleaner");
			this.setPriority(Thread.MIN_PRIORITY);
		}
		
		public void run() {
			while (true) {
				boolean cleaningTime;
				ResourceReferenceInfo toClean = null;
				if (!diskMan.isAlive())
					return;
				synchronized (dirtyResources) {
					cleaningTime = !dirtyResources.isEmpty() && diskMan.isIdle() && this.isIdle();
					if (cleaningTime)
						toClean = (ResourceReferenceInfo) dirtyResources.iterator().next();
				}
				if (cleaningTime) {
					//TODO: The writing of the resource should be able to live
					// outside the sunchronized block!
					writeResourceToDisk(toClean);
//					System.err.println("Background cleaning: "+next.getResourceID());
				}
				try {
					sleep(300);
				} catch (InterruptedException e) {
				}
			}
		}

		private boolean isIdle() {
			long idletime = System.currentTimeMillis()-lastTaskTime;
//			System.err.println("Pager IT: "+idletime);
			return idletime > 4000;
		}
	}
	
	/** Creates a new Pager. */
	public Pager(int cacheSize, int queueSize, long lastBackupTime, boolean backgrounCleaning) {
		this.cacheSize = cacheSize;
		this.needToCallBackup = false;
		this.lruQueue = new DoubleLinkedList();
		this.diskMan = new DiskManager(queueSize);
		diskMan.setPriority(Thread.MAX_PRIORITY);
		diskMan.start();
		if (backgrounCleaning) {
			this.pageCleaner = new PageCleaner();
			pageCleaner.start();
		}
	}

	public void enableBackGroundPaging() {
	}
	
	/**
	 * Make the pager perform a task synchronously. A synchronous task has to
	 * wait until all other asynchronous tasks have been finished before it can
	 * run.
	 */
	public Object synchDoTask(ResourceId rsrcID, Task task) {
		boolean lockHeld = false;
		this.lastTaskTime = System.currentTimeMillis();
		Resource rsrc = getResource(rsrcID);
		try {
			if (task.mayChangeResource()) {
				diskMan.getResourceLock(rsrcID);
				lockHeld = true;
			}
		}
		finally {
			if (lockHeld)
				diskMan.releaseResourceLock(rsrcID);
		}
		Object result = task.doIt(rsrc);
		if (task.resourceIsChanged()) {
			changeResource(rsrcID, task.getChangedResource());
		} else {
			referenceResource(rsrcID);
		}
		return result;
	}
	
	/**
	 * Do a task asynchronously (currently not implemented, the task will
	 * perform as if it is a synchronous task.
	 */
	public void asynchDoTask(ResourceId rsrcID, Task task) {
		this.lastTaskTime = System.currentTimeMillis();
		synchDoTask(rsrcID, task);
	}
	
	/** Update the reference info for a resource. */
	private void referenceResource(ResourceId rsrcID) {
		ResourceReferenceInfo rsrc_ref = (ResourceReferenceInfo) inMemory.get(rsrcID);
		if (rsrc_ref != null) {
			rsrc_ref.setReferenced(true);
			rsrc_ref.incrementReferenceCounter();
			lruQueue.remove(rsrc_ref);
			lruQueue.enqueue(rsrc_ref);
		}
	}
	
	/** Change a resource. May cause paging to occur. */
	private void changeResource(ResourceId rsrcID, Resource newResource) {
		needToCallBackup = true;
		ResourceReferenceInfo rsrc_ref = (ResourceReferenceInfo) inMemory.get(rsrcID);
		if (rsrc_ref == null) {
			//NEW
			ResourceReferenceInfo newInfo = new ResourceReferenceInfo(rsrcID, newResource);
			newInfo.setDirty(true);
			newInfo.setReferenced(true);
			if (needToPageOutIfOneMoreAdded()) {
				pageOutOne();
			}
			inMemory.put(rsrcID, newInfo);
			lruQueue.enqueue(newInfo);
		} else {
			if (newResource == null) {
				//DELETED
				rsrcID.removeResource();
				inMemory.remove(rsrcID);
				lruQueue.remove(rsrc_ref);
			} else {
				//UPDATED
				rsrc_ref.updateResource(newResource);
				rsrc_ref.setDirty(true);
				rsrc_ref.setReferenced(true);
				lruQueue.remove(rsrc_ref);
				lruQueue.enqueue(rsrc_ref);
			}
		}
	}
	
	/** Retrieve a resource. May cause paging to occur. */
	private Resource getResource(ResourceId rsrcID) {
		ResourceReferenceInfo rsrc_ref = (ResourceReferenceInfo) inMemory.get(rsrcID);
		if (rsrc_ref == null) {
			Resource result = getResourceFromDisk(rsrcID);
			return result;
		} else {
			Resource result = rsrc_ref.getResource();
			return result;
		}
	}
	
	/** Backup the data that is stored in the pager to disk. */
	public void backup() {
		//TODO: make backup work more as we would expect it to
		if (needToCallBackup) {
			for (Iterator iter = inMemory.values().iterator(); iter.hasNext();) {
				ResourceReferenceInfo refInfo = (ResourceReferenceInfo) iter.next();
				if (refInfo.isDirty()) {
					writeResourceToDisk(refInfo);
				}
			}
			needToCallBackup = false;
		}
		diskMan.flush();
	}
	
	/** Shut down the pager (which shuts down the DiskManager thread). */
	public void shutdown() {
		backup();
		diskMan.killMe();
	}
	
	/** 
	 * Crash is used for testing (to simulate a system crash).
	 * it kills the diskMan without a proper backup.
	 */
	public void crash() {
		diskMan.crash();
	}
	
	/** Change the amount of data that is cached in memory. */
	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
		pageUntilNonNeeded();
	}
	
	/** Get the cache size. */
	public int getCacheSize() {
		return cacheSize;
	}
	
	/** Whether we need to page out. */
	private boolean needToPageOut() {
		return inMemory.size() > cacheSize;
	}
	
	/** Whether we need to page out if we add one more resource to memory. */
	private boolean needToPageOutIfOneMoreAdded() {
		return (inMemory.size() + 1) > cacheSize;
	}
	
	/** Page out one resource. */
	private void pageOutOne() {
		if (inMemory.size() > 0) {
			ResourceReferenceInfo victim = null;
			
			//Get the Least recently used resource to page out
			victim = (ResourceReferenceInfo) lruQueue.dequeue();
			inMemory.remove(victim.getResourceID());
			
			if (victim != null) {
				if (victim.isDirty()) {
					writeResourceToDisk(victim);
				}
			} else {
				throw new Error("Could not find a victim to page, probably a coding error somewhere.");
			}
		}
	}
	
	/** Page until we are within the maximum cache size. */
	private void pageUntilNonNeeded() {
		while (needToPageOut()) {
			pageOutOne();
		}
	}
	
	/** Write a resource to disk. */
	private void writeResourceToDisk(ResourceReferenceInfo victim) {
		diskMan.writeOut(victim.getResourceID(), victim.getResource());
		victim.setDirty(false);
	}
	
	/** Read a resource from disk. */
	private Resource getResourceFromDisk(ResourceId rsrcID) {
		if (diskMan.resourceExists(rsrcID)) {
			if (needToPageOutIfOneMoreAdded()) {
				pageOutOne();
			}
			
			Resource resource = diskMan.readIn(rsrcID);
			changeResource(rsrcID, resource);
			return resource;
		} else {
			return null;
		}
	}
	
	/** Print some statistics. */
	public void printStats() {
		diskMan.printStats();
	}

	public boolean isDirty() {
		synchronized (dirtyResources) {
			return !dirtyResources.isEmpty();
		}
	}
	
}