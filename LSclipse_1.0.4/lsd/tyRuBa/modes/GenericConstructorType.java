/*
 * Created on Aug 23, 2003
 *
 */
package tyRuBa.modes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tyRuBa.engine.FunctorIdentifier;
import tyRuBa.engine.RBCompoundTerm;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBTuple;

/**
 * A ConstructorType represents the type of a constructor for making composite terms
 * out of there parts. It can be applied to a number of argument types and returns
 * the type of the corresponding compoundterm.  
 */
public class GenericConstructorType extends ConstructorType implements Serializable {

	FunctorIdentifier identifier;
	Type     args;
	CompositeType result;

	public GenericConstructorType(FunctorIdentifier identifier,Type args, CompositeType result) {
		this.identifier = identifier;
        this.args = args;
		this.result = result;
	}
	
	public FunctorIdentifier getFunctorId() {
		return identifier;
	}

    public TypeConstructor getTypeConst() {
        return result.getTypeConstructor();
    }
    
	public int getArity() {
        if (args instanceof TupleType)
            return ((TupleType)args).size();
        else
            return 1;
	}

	public RBTerm apply(RBTerm tuple) {
		return RBCompoundTerm.make(this,tuple);
	}
	
    public RBTerm apply(ArrayList terms) {
        return apply(RBTuple.make(terms));
    }
    
	public Type apply(Type argType) throws TypeModeError {          
		Map renamings = new HashMap();
		Type iargs = args.clone(renamings);            
		CompositeType iresult = (CompositeType)result.clone(renamings);
		//TODO: This is not quite correct but just for now...                      
        argType.checkEqualTypes(iargs);
		return iresult.getTypeConstructor().apply(iresult.getArgs(), true); 
	}

    public boolean equals(Object other) {
        if (other.getClass() != this.getClass()) {
            return false;
        } else {
            GenericConstructorType ctOther = (GenericConstructorType) other;
            return args.equals(ctOther.args) && identifier.equals(ctOther.identifier) && result.equals(ctOther.result);
        }
    }
    
    public int hashCode() {
    		return args.hashCode() + identifier.hashCode()*13 + result.hashCode()*31;
    }
}

