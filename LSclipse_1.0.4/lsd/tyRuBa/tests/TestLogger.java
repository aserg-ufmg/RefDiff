package tyRuBa.tests;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.Assert;

public class TestLogger {
	
	public static final boolean logging = false;
	
	private int logtime = 0;
	PrintWriter logFile;
	PrintStream console = System.err;

	public TestLogger(PrintWriter writer) {
		logFile = writer;
	}

	/** 
	 * A LogEntry collects information to be written out to the
	 * logfile. It is supposed to record information associated
	 * with some kind of event occurring in the code which has
	 * a takes place over a given interval of time.
	 * 
	 * LogEntries are created by call the begin method on the logger.
	 * logentries will record the creation time (a log sequence number).
	 * 
	 * logentries are written by calling the "end" method. And they will
	 * record the time of the call as the time the process logged by this
	 * entry was completed.
	 * 
	 * The current implementation assumes that begin_end events of
	 * logentries are "nested" like this
	 *   begin event 1
	 *     begin event 2
	 *       begin event 3
	 *       end event 3
	 *     end event 2
	 *   end event 1
	 */
	 public class LogEntry {

		LogEntry parent;		

		String kind;		
		ArrayList info = new ArrayList();
		
		int enter = 0;
		int exit = 0;
		
		LogEntry(LogEntry parent,String kind,int creationTime) {
			this.parent = parent;
			this.kind = kind;
			this.enter = creationTime;
		}
		
		public void addInfo(Object infoArg) {
			info.add(infoArg.toString());
		}
		
		public String toString() {
			return kind + 
				"(" +
					enter + "," + exit + "," + parentID() +
					infoString() +
				").";
		}

		private int parentID() {
			return parent == null ? 0 : parent.enter;
		}

		private String infoString() {
			String result = "";
			for (Iterator iter = info.iterator(); iter.hasNext();) {
				String element = (String) iter.next();
				result += ","+element;
			}
			return result;
		}

		public void exit() {
			exit = ++logtime;
			String msg = this.toString();
			println(msg);
			Assert.assertEquals(current,this);
			if (!(parent == null || parent.inProgress()))
				println("*** problem with parent entry "+parent);
			current = parent;
		}

		private void println(String msg) {
			if (console!=null)
				console.println(msg);
			if (logFile!=null)
				logFile.println(msg);
		}

		private boolean inProgress() {
			return exit==0; // will be set to >0 value if complete
		}

		public void exit(String info) {
			addInfo(info);
			exit();
		}
	}
	
	LogEntry current = new LogEntry(null,"BIGBANG",0);

	public LogEntry enter(String kind) {
		current = new LogEntry(current,kind,++logtime);
		return current;
	}

	public LogEntry enter(String kind, String params) {
		current = new LogEntry(current,kind,++logtime);
		current.addInfo(params);
		return current;
	}

	public LogEntry enter(String kind, int info) {
		return enter(kind,""+info);
	}
	
	public void logNow(String kind, String params) {
		LogEntry enter = enter(kind,params);
		enter.exit();
	}
	
	void close() {
		while (current!=null)
			current.exit(); 
		if (logFile!=null)
			logFile.close();
	}

	private static boolean loading = false;
	public synchronized LogEntry enterLoad(String path) {
		Assert.assertFalse("Reentrant load should not happen",loading);
		Assert.assertFalse("Load inside storeAll",current.kind.equals("storeAll"));
		loading = true;
		return enter("load","\""+path+"\"");
	}

	public synchronized void exitLoad(LogEntry entry) {
		Assert.assertTrue("Must enter load before exit load",loading);
		entry.exit();
		loading = false;
	}

	public void assertTrue(String msg, boolean b) {
		if (!b) {
			logNow("assertionFailed","\""+msg+"\"");
			Assert.fail(msg);
		}
	}

}
