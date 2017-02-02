/*
 * Created on May 25, 2004
 */
package tyRuBa.modes;

import java.io.Serializable;


/**
 * @author riecken
 */
public abstract class TypeMapping implements Serializable {

    private ConstructorType functor;

    /** 
     * Returns the "special" Class that can be mapped onto a TyRuBa composite type 
     **/ 
    public abstract Class getMappedClass();
    
    /** 
     * When passed an object that is an instance of the mapped class, this method is responsible
     * for "disambling the object" into its parts and return them. These objects in 
     * turn will be converted and added as the subterms for the created term.
     * 
     * In general, if the contructor is expecting a tuple or list then this method
     * should return an Object[]. Otherwise it should return just an Object.
     **/ 
    public abstract Object toTyRuBa(Object obj);

    /** 
     * This is passed an array of objects that are the result of converting the 
     * subterms of a mapped RBCompoundTerm into java. The method should use these 
     * objects as parts and reasemble them into a single Java Object.
     * 
     * Similar to the toTyRuBa method the obj will actually be an Object[] if the
     * tyruba constructor for terms of this type take a tuple or a list as its
     * argument.
     * 
     * But if the constructor takes only a single term then this method will 
     * receive a single Object.
     **/ 
    public abstract Object toJava(Object obj);

    public ConstructorType getFunctor() {
        return this.functor;
    }
    
    /**
     * This should only be called by the TypeInfoBase when the Mapping is
     * inserted.
     */
    public void setFunctor(ConstructorType functor) {
        if (this.functor!=null) {
            throw new Error("Double mapping for "+this+": "+this.functor+" and "+functor);
        }
        this.functor = functor;
    }
}
