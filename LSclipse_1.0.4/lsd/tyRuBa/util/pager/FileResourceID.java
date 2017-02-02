/*
 * Created on Jul 27, 2004
 */
package tyRuBa.util.pager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import tyRuBa.util.pager.Pager.ResourceId;

/**
 * An Id for a resource that is stored on disk. Accessed using files.
 * @category FactBase
 * @author riecken
 */
public class FileResourceID extends ResourceId {

    /** Relative location to the base location. */
    private String relativeId;

    /** Base location. */
    private FileLocation base;

    /** File pointing to the resource. Only created if necessary. */
    private File lazyActualFile = null;

    /** Creates a new FileResourceID. */
    public FileResourceID(FileLocation location, String relativeID) {
        base = location;
        relativeId = relativeID;
    }

    public String toString() {
    		return "FileResourceID(" + base + "/" + relativeId +")";
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object other) {
        if (other instanceof FileResourceID) {
            FileResourceID id_other = (FileResourceID) other;
            return (id_other.relativeId.equals(relativeId) && id_other.base.equals(base));
        } else {
            return false;
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return 23 * base.hashCode() + 47 * relativeId.hashCode();
    }

    /** Opens an InputStream to the resource. */
    public InputStream readResource() throws IOException {
        if (lazyActualFile == null) {
            lazyActualFile = new File(base.getBase(), relativeId);
        }
        return new FileInputStream(lazyActualFile);
    }

    /** Opens an OutputStream to the resource. */
    public OutputStream writeResource() throws IOException {
        File baseFile = base.getBase();
        if (!baseFile.exists()) {
            baseFile.mkdirs();
        }
        if (lazyActualFile == null) {
            lazyActualFile = new File(base.getBase(), relativeId);
        }
        return new FileOutputStream(lazyActualFile);
    }

    /** Deletes the resource */
    public void removeResource() {
        if (lazyActualFile == null) {
            lazyActualFile = new File(base.getBase(), relativeId);
        }
        lazyActualFile.delete();
    }

    /** Checks whether the resource exists. */
    public boolean resourceExists() {
        if (lazyActualFile == null) {
            lazyActualFile = new File(base.getBase(), relativeId);
        }
        return lazyActualFile.exists();
    }

}