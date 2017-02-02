/*
 * Created on Jul 26, 2004
 */
package tyRuBa.util.pager;

import java.io.File;
import java.net.URL;

/**
 * A "base" location. This is a factory for making resourceIDs (relative to that location).
 * @category FactBase
 * @author riecken
 */
public abstract class Location {

    /**
     * Creates a resourceId for the given path relative to the base.
     */
    public abstract Pager.ResourceId getResourceID(String relativeID);
    
    /** Make a URLLocation. */
    public Location make(final URL theBaseURL) {
        return new URLLocation(theBaseURL);
    }

    /** Make a FileLocation. */
    public Location make(final File theBasePath) {
        return new FileLocation(theBasePath);
    }

}
