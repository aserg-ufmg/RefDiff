/*
 * Created on Jul 26, 2004
 */
package tyRuBa.util.pager;

import java.net.MalformedURLException;
import java.net.URL;

import tyRuBa.util.pager.Pager.ResourceId;

/**
 * Represents a location on by a URL
 * @category FactBase
 * @author riecken
 */
public class URLLocation extends Location {

    /** Base location. */
    URL base = null;

    /** Creates a new URLLocation. */
    public URLLocation(String theBaseURL) throws MalformedURLException {
        this(new URL(theBaseURL));
    }

    /** Creates a new URLLocation. */
    public URLLocation(URL theBaseURL) {
        base = theBaseURL;
    }

    /** Gets the base location. */
    public URL getBase() {
        return base;
    }

    /**
     * Creates a resourceId for the given path relative to the base.
     */
    public ResourceId getResourceID(String relativeID) {
        return new URLResourceID(this, relativeID);
    }

}