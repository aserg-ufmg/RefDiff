/*
 * Created on Jun 18, 2004
 */
package tyRuBa.engine.factbase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tyRuBa.engine.Validator;

/**
 * A ValidatorManager that is persisted to a file on disk.
 * @category FactBase
 * @author riecken
 */
public class FileBasedValidatorManager implements ValidatorManager {

    /** The directory in which this validator manager is persisted. */
    private String storagePath;

    /** The map of validatorHandle to validator (Long => Validator). */
    private Map validators;

    /** The map of validatorHandle to validatorId (Long => String). */
    private Map identifiers;

    /** The map of validatorId to validatorHandle (String => Long). */
    private Map handles;

    /** The last time that any validator was invalidated. */
    private long lastInvalidateTime;

    /** The counter that is used to issue validator handles. */
    private long validatorCounter;

    /**
     * Creates a new FileBasedValidatorManager.
     * @param storagePath where to store the validators.
     */
    public FileBasedValidatorManager(String storagePath) {
        this.storagePath = storagePath;
        this.validators = new HashMap();
        this.identifiers = new HashMap();
        this.handles = new HashMap();
        this.lastInvalidateTime = -1;
        this.validatorCounter = 0;

        File validatorFile = new File(storagePath + "/validators.data");
        if (validatorFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(validatorFile);
                ObjectInputStream ois = new ObjectInputStream(fis);

                int size = ois.readInt();
                for (int i = 0; i < size; i++) {
                    String id = (String) ois.readObject();
                    Validator validator = (Validator) ois.readObject();

                    Long handle = new Long(validator.handle());
                    handles.put(handle, id);
                    identifiers.put(id, handle);
                    validators.put(handle, validator);
                }
                lastInvalidateTime = ois.readLong();
                validatorCounter = ois.readLong();

                ois.close();
                fis.close();
            } catch (IOException e) {
                throw new Error("Could not load validators because of IOException");
            } catch (ClassNotFoundException e) {
                throw new Error("Could not load validators because of ClassNotFoundException");
            }

        }
    }

    /**
     * Creates a new FileBasedValidatorManager.
     * @param url url to stored validator manager.
     */
    public FileBasedValidatorManager(URL url) {
        this.storagePath = url.toString();
        this.validators = new HashMap();
        this.identifiers = new HashMap();
        this.handles = new HashMap();
        try {
            ObjectInputStream ois = new ObjectInputStream(url.openStream());
            int size = ois.readInt();
            for (int i = 0; i < size; i++) {
                String id = (String) ois.readObject();
                Validator validator = (Validator) ois.readObject();
                Long handle = new Long(validator.handle());
                handles.put(handle, id);
                identifiers.put(id, handle);
                validators.put(handle, validator);
            }
            lastInvalidateTime = ois.readLong();
            validatorCounter = ois.readLong();
            ois.close();
        } catch (IOException e) {
            throw new Error("Could not load validators because of IOException");
        } catch (ClassNotFoundException e) {
            throw new Error("Could not load validators because of ClassNotFoundException");
        }
    }

    /**
     * @see tyRuBa.engine.factbase.ValidatorManager#add(tyRuBa.engine.Validator,
     * java.lang.String)
     */
    public void add(Validator v, String identifier) {
        v.setHandle(validatorCounter++);
        Long handle = new Long(v.handle());
        if (!validators.containsKey(handle)) {
            validators.put(handle, v);
            identifiers.put(identifier, handle);
            handles.put(handle, identifier);
        }
    }

    /**
     * @see tyRuBa.engine.factbase.ValidatorManager#update(long,
     * java.lang.Boolean, java.lang.Boolean)
     */
    public void update(long validatorHandle, Boolean outdated, Boolean hasFacts) {
        Validator v = (Validator) validators.get(new Long(validatorHandle));
        if (v != null) {
            if (outdated != null) {
                v.setOutdated(outdated.booleanValue());
            }
            if (hasFacts != null) {
                v.setHasAssociatedFacts(hasFacts.booleanValue());
            }
        }
    }

    /**
     * @see tyRuBa.engine.factbase.ValidatorManager#remove(long)
     */
    public void remove(long validatorHandle) {
        Long handle = new Long(validatorHandle);
        Validator v = (Validator) validators.get(handle);
        validators.remove(handle);
        String identifier = (String) handles.get(handle);
        identifiers.remove(identifier);
        handles.remove(handle);
        lastInvalidateTime = System.currentTimeMillis();
    }

    /**
     * @see tyRuBa.engine.factbase.ValidatorManager#remove(java.lang.String)
     */
    public void remove(String identifier) {
        Long handle = (Long) identifiers.get(identifier);
        if (handle != null) {
            identifiers.remove(identifier);
            remove(handle.longValue());
        }
    }

    /**
     * @see tyRuBa.engine.factbase.ValidatorManager#get(long)
     */
    public Validator get(long validatorHandle) {
        Long handle = new Long(validatorHandle);
        Validator result = (Validator) validators.get(handle);
        return result;
    }

    /**
     * @see tyRuBa.engine.factbase.ValidatorManager#get(java.lang.String)
     */
    public Validator get(String identifier) {
        Long handle = (Long) identifiers.get(identifier);
        if (handle != null) {
            return get(handle.longValue());
        } else {
            return null;
        }
    }

    /**
     * @see tyRuBa.engine.factbase.ValidatorManager#getIdentifier(long)
     */
    public String getIdentifier(long validatorHandle) {
        return (String) handles.get(new Long(validatorHandle));
    }

    /**
     * @see tyRuBa.engine.factbase.ValidatorManager#printOutValidators()
     */
    public void printOutValidators() {
        Iterator it = validators.values().iterator();
        while (it.hasNext()) {
            Validator v = (Validator) it.next();
            System.err.println("[Validator] " + v);
        }
    }

    /**
     * @see tyRuBa.engine.factbase.ValidatorManager#backup()
     */
    public void backup() {
        try {
            FileOutputStream fos = new FileOutputStream(new File(storagePath + "/validators.data"), false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            int size = 0;
            for (Iterator iter = identifiers.keySet().iterator(); iter.hasNext();) {
                String element = (String) iter.next();
                if (!element.startsWith("TMP")) {
                    size++;
                }
            }
            oos.writeInt(size);
            for (Iterator iter = identifiers.entrySet().iterator(); iter.hasNext();) {
                Map.Entry element = (Map.Entry) iter.next();
                String identifier = (String) element.getKey();
                if (!identifier.startsWith("TMP")) {
                    oos.writeObject(identifier);
                    oos.writeObject(validators.get(element.getValue()));
                }
            }
            oos.writeLong(lastInvalidateTime);
            oos.writeLong(validatorCounter);
            oos.close();
            fos.close();
        } catch (IOException e) {
            throw new Error("Could not backup validator manager because of IOException");
        }

    }

    /**
     * @see tyRuBa.engine.factbase.ValidatorManager#getLastInvalidatedTime()
     */
    public long getLastInvalidatedTime() {
        return lastInvalidateTime;
    }

}