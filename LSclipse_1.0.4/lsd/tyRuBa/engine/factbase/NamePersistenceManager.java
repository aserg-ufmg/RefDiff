/*
 * Created on Jul 7, 2004
 */
package tyRuBa.engine.factbase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * A NamePersistenceManager manages the mappings between Strings and filenames
 * for those strings. In this case the filenames are numbers.
 * @category FactBase
 * @author riecken
 */
public class NamePersistenceManager implements Serializable {

    /** Mapping of names to filenames. */
    private Map nameMap;

    /** Path at which the mappings are persisted. */
    private String storagePath;

    /**
     * Creates a new NamePersistenceManager.
     * @param storagePath path at which the mappings are persisted.
     */
    public NamePersistenceManager(String storagePath) {
        File nameFile = new File(storagePath + "/names.data");
        this.storagePath = storagePath;
        if (nameFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(nameFile);
                ObjectInputStream ois = new ObjectInputStream(fis);

                nameMap = (HashMap) ois.readObject();

                ois.close();
                fis.close();
            } catch (IOException e) {
                throw new Error("Could not load names because of IOException");
            } catch (ClassNotFoundException e) {
                throw new Error("Could not load names because of ClassNotFoundException");
            }
        } else {
            nameMap = new HashMap();
        }
    }

    /**
     * Creates a new NamePersistenceManager.
     * @param storageLocation url location at which the persisted mappings are.
     */
    public NamePersistenceManager(URL storageLocation) {
        storagePath = storageLocation.toString();
        try {
            ObjectInputStream ois = new ObjectInputStream(storageLocation.openStream());
            nameMap = (HashMap) ois.readObject();

            ois.close();
        } catch (IOException e) {
            System.err.println(storagePath);
            throw new Error("Could not load names because of IOException", e);
        } catch (ClassNotFoundException e) {
            throw new Error("Could not load names because of ClassNotFoundException", e);
        }
    }

    /**
     * Gets a filename from a given Java string.
     * @param tyRuBaName String to get filename for.
     */
    public String getPersistentName(String tyRuBaName) {
        String result = (String) nameMap.get(tyRuBaName);
        if (result == null) {
            int nextNum = nameMap.size();
            result = String.valueOf(nextNum);
            nameMap.put(tyRuBaName, result);
        }
        return result;
    }

    /**
     * Backs up this manager. The mappings are written out so that they can be
     * loaded at some future point.
     */
    public void backup() {
        File nameFile = new File(storagePath + "/names.data");
        try {
            FileOutputStream fos = new FileOutputStream(nameFile, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(nameMap);

            oos.close();
            fos.close();
        } catch (IOException e) {
            throw new Error("Could not save names because of IOException");
        }
    }

}