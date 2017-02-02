/*
 * Created on Aug 19, 2004
 */
package tyRuBa.modes;

import java.util.ArrayList;

import tyRuBa.engine.FunctorIdentifier;
import tyRuBa.engine.RBTerm;

/**
 * @author riecken
 */
public abstract class ConstructorType {
    public abstract FunctorIdentifier getFunctorId();

    public abstract TypeConstructor getTypeConst();

    public abstract int getArity();

    public abstract RBTerm apply(RBTerm tuple);

    public abstract RBTerm apply(ArrayList terms);

    public abstract Type apply(Type argType) throws TypeModeError;

    public abstract boolean equals(Object other);
    
    public abstract int hashCode();
    
    public static ConstructorType makeUserDefined(FunctorIdentifier functorId, Type repAs, CompositeType type) {
        if (repAs.isJavaType()) {
            return new RepAsJavaConstructorType(functorId, repAs, type);
        }
        else {
            return new GenericConstructorType(functorId, repAs, type);
        }
    }

    public static ConstructorType makeJava(Class javaClass) {
        return new JavaConstructorType(javaClass);
    }
}