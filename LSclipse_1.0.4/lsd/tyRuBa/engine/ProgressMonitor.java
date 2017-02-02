package tyRuBa.engine;

/**
 * If anybody is interested in tracking the progress of
 * the query engine performning an update of its buckets
 * they should implement this interface and pass a ProgressMonitor
 * to the update method in QueryEngine.
 */
public interface ProgressMonitor {
	
	void beginTask(String name,int totalWork);
	
	void worked(int units);
	
	void done();

}
