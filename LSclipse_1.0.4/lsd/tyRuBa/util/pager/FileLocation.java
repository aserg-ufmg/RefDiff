/*
 * Created on Jul 26, 2004
 */
package tyRuBa.util.pager;

import java.io.File;

import tyRuBa.util.pager.Pager.ResourceId;

/**
 * Represents a location on disk
 * @category FactBase
 * @author riecken
 */
public class FileLocation extends Location {

    /** Base location. */
    File base = null;

    /** precomputed hashcode for efficiency. */
    int myHashCode;

    /** Creates a new FileLocation. */
    public FileLocation(File theBasePath) {
        base = theBasePath;
        myHashCode = base.hashCode();
    }

    /** Creates a new FileLocation. */
    public FileLocation(String filename) {
        this(new File(filename));
    }

    /** Gets the base location. */
    public File getBase() {
        return base;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object other) {
        if (other instanceof FileLocation) {
            FileLocation flOther = (FileLocation) other;
            return flOther.base.equals(base);
        } else {
            return false;
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return myHashCode;
    }

    /**
     * Creates a resourceId for the given path relative to the base.
     */
    public ResourceId getResourceID(String relativeID) {
        return new FileResourceID(this, relativeID);
    }
    
    public String toString() {
    		return base.toString();
    }

}