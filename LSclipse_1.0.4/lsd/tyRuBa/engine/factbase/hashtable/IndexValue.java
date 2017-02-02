/*
 * Created on Jun 11, 2004
 */
package tyRuBa.engine.factbase.hashtable;

import java.io.Serializable;

import tyRuBa.engine.RBTuple;
import tyRuBa.engine.Validator;
import tyRuBa.engine.factbase.ValidatorManager;

/**
 * A part of a fact that resides in an Index. Has two parts, a RBTuple and a
 * validatorHandle
 * @category FactBase
 * @author riecken
 */
public class IndexValue implements Serializable {

    /** handle of the validator for the bucket that this fact resides in. */
    private long validatorHandle;

    /** the part of the fact that is in this IndexValue. */
    private RBTuple parts;

    /** Make an index value with the specified validator and tuple. */
    public static IndexValue make(Validator v, RBTuple parts) {
        if (v == null) {
            return new IndexValue(0, parts);
        } else {
            return new IndexValue(v.handle(), parts);
        }
    }

    /** Make an index value with the specified validator handle and tuple. */
    public static IndexValue make(long validatorHandle, RBTuple parts) {
        return new IndexValue(validatorHandle, parts);
    }

    /** Creates an index value with the specified validator handle and tuple. */
    private IndexValue(long validatorHandle, RBTuple parts) {
        this.validatorHandle = validatorHandle;
        this.parts = parts;
    }

    /** Gets the validator handle. */
    public long getValidatorHandle() {
        return validatorHandle;
    }

    /** Gets the tuple that is stored. */
    public RBTuple getParts() {
        return parts;
    }

    /**
     * Checks whether this value is valid.
     * @param vm ValidatorManager that to use to validate.
     */
    public boolean isValid(ValidatorManager vm) {
        if (validatorHandle == 0) {
            return true;
        } else {
            Validator v = vm.get(validatorHandle);
            if (v == null || !v.isValid()) {
                return false;
            }
            return true;
        }
    }

    /**
     * Prepends the specified tuple to the tuple that is stored in the
     * IndexValue.
     * @param tuple RBTuple to prepend.
     */
    public IndexValue prepend(RBTuple tuple) {
        return new IndexValue(validatorHandle, tuple.append(parts));
    }

    public String toString() {
    		return parts.toString();
    }
    
}