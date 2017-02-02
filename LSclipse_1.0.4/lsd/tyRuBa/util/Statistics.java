/*
 * Created on Jun 28, 2004
 */
package tyRuBa.util;


/**
 * An interface to a statistics gatherer
 * @author riecken
 */
public interface Statistics {
    
    /** Start gathering statistics if we have not already done so */
    public void stopGathering();
    /** Stop gathering statistics */
    public void startGathering();
    /** Reset all statistics */
    public void reset();
    
    /** Retrieve an integer valued statistic keyed by the specified statistic name */
    public int getIntStat(String statName);
    /** Retrieve a long valued statistic keyed by the specified statistic name */
    public long getLongStat(String statName);
    /** Retrieve a float valued statistic keyed by the specified statistic name */
    public float getFloatStat(String statName);
    /** Retrieve an Object valued statistic keyed by the specified statistic name */
    public Object getObjectStat(String statName);
    
}
