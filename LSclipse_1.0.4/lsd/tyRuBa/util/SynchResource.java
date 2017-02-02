/*
 * Created on Oct 3, 2003
 */
package tyRuBa.util;

/**
 * Interface for Resources on which a SynchronizedElementSource can
 * synchronize.
 * 
 * A SynchResource must provide a place to store a SynchPolicy
 */
public interface SynchResource {

	SynchPolicy getSynchPolicy();

}
