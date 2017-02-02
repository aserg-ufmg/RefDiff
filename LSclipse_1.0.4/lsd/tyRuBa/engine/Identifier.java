/*
 * Created on May 27, 2004
 */
package tyRuBa.engine;

import java.io.Serializable;


/**
 * Subclasses of Identifier stores a name and an arity. These objects are used to identify 
 * functors, predicates, etc.
 */
public abstract class Identifier implements Serializable {

    protected String name;
    protected int arity;

    public Identifier(String name,int arity) {
        this.name = name;
        this.arity = arity;
    }

    public boolean equals(Object arg) {
    	if (arg.getClass().equals(this.getClass())) {
    	    Identifier other = (Identifier) arg;
    		return name.equals(other.name) && arity == other.arity;
    	} else {
    		return false;
    	}
    }

    public int hashCode() {
    	return getClass().hashCode() * arity + name.hashCode();
    }

    public String toString() {
    	return name + "/" + arity;
    }

    public int getArity() {
    	return arity;
    }

    public String getName() {
    	return name;
    }

}
