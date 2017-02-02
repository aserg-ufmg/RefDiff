/*
 * Created on Aug 9, 2004
 *
 */
package tyRuBa.util;


/** All keys that are inserted into this map must implement this interface.
 *  -There is no way to reconstruct the original object that the key was created from. */
public interface TwoLevelKey {
    public String getFirst();
    public Object getSecond();
}