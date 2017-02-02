package tyRuBa.util;

/**
 * @author cburns
 *
 * This simple class implements a mutex that can be used much more flexibly than synchronized
 * methods and synchronized blocks.
 */
public class Mutex {
	private int waiting = -1;
	
	public synchronized void obtain() {
		
		waiting++;
		if(waiting == 0) {
			//We're here first, can proceed to the protected code
			return;
		} else {
			try {
				this.wait();
			} catch(InterruptedException e) {
				throw new Error("This should not happen!");
			}
		}
	}
	
	public synchronized void release() {
		if(waiting > 0)
			this.notify(); //wake up one thread that's waiting on the mutex
			
		waiting--;
	}
}
