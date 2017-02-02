/*
 * Created on Jun 14, 2004
 */
package tyRuBa.util;

import java.io.IOException;
import java.io.Serializable;


/**
 * @author riecken
 */
public class ObjectTuple implements Serializable {

    private boolean isSingleton;
    private Object[] objects;
    private Object singletonObj;
    
    public static ObjectTuple theEmpty = new ObjectTuple(new Object[0]);
    
    private ObjectTuple(Object[] objects) {
        this.objects = objects;
    }
    
    private ObjectTuple(Object object, boolean isSingleton) {
        singletonObj = object;
        this.isSingleton = isSingleton;
        System.err.println("MAKING A SINGLETON ObjectTuple, something probably isn't right");
    }
    
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeBoolean(isSingleton);
        if (isSingleton) {
            out.writeObject(singletonObj);
        } else {
            out.writeInt(objects.length);
            for (int i = 0; i < objects.length; i++) {
                out.writeObject(objects[i]);
            }
        }
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        isSingleton = in.readBoolean();
        if (isSingleton) {
            singletonObj = in.readObject();
            if (singletonObj instanceof String) {
                singletonObj = ((String)singletonObj).intern();
            }
        } else {
            objects = new Object[in.readInt()];
            for (int i = 0; i < objects.length; i++) {
                objects[i] = in.readObject();
                if (objects[i] instanceof String) {
                    objects[i] = ((String) objects[i]).intern();
                }
            }
        }
    }
    
    public static ObjectTuple make(Object[] objs) {
        if (objs.length == 0) {
            return theEmpty;
        } else if (objs.length == 1) {
            return new ObjectTuple(objs[0], true);
        } else {
        
            return new ObjectTuple(objs);
        }
    }
    
    public static ObjectTuple makeSingleton(Object o) {
        return new ObjectTuple(o, true);
    }
    
    public int size() {
        if (isSingleton) {
            return 1;
        } else {
            return objects.length;
        }
    }
    
    public Object get(int i) {
        if (isSingleton) {
            if (i != 0) {
                throw new Error("Index out of bounds");
            }
            return singletonObj;
        } else {
            return objects[i];
        }
    }
    
    public boolean equals(Object obj) {
        if (obj.getClass() == this.getClass()) {
            if (this == obj) {
                return true;
            }
            
            ObjectTuple other = (ObjectTuple) obj;
            
            if (this.isSingleton && other.isSingleton) {
                return this.singletonObj.equals(other.singletonObj);
            } else if (this.isSingleton != other.isSingleton) {
                return false;
            } else {
                for (int i = 0; i < objects.length; i++) {
                    if (!this.objects[i].equals(other.objects[i])) {
                        return false;
                    }
                }   
                return true;
            }
        } else {
            return false;
        }
    }
   
    public static ObjectTuple append(ObjectTuple first, ObjectTuple second) {
        Object[] result = new Object[first.size()+second.size()];
        for (int i = 0; i < first.size(); i++) {
            result[i] = first.get(i);
        }
        for (int i = 0; i < second.size(); i++) {
            result[first.size()+i] = second.get(i);
        }
        return ObjectTuple.make(result);
    }
    
    
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("<<");
        if (isSingleton) {
            result.append(singletonObj);
        } else {
            for (int i = 0; i < objects.length; i++) {
                if (i > 0) 
                    result.append(", ");
                result.append(objects[i].toString());
            }
        }
        result.append(">>");
        return result.toString();
    }
    
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        if (isSingleton) {
            int hash = 1;
            return hash * 83 + singletonObj.hashCode();
        } else {
            int hash = objects.length;
            for (int i = 0; i < objects.length; i++)
                hash = hash * 83 + objects[i].hashCode();
            return hash;
        }
    }
}
