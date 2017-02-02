/*
 * Created on Aug 19, 2004
 */
package tyRuBa.engine;

import java.io.IOException;

import tyRuBa.engine.visitor.TermVisitor;
import tyRuBa.modes.BindingMode;
import tyRuBa.modes.ConstructorType;
import tyRuBa.modes.Factory;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.RepAsJavaConstructorType;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeConstructor;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeModeError;
import tyRuBa.util.TwoLevelKey;

/**
 * @author riecken
 */
public class RBRepAsJavaObjectCompoundTerm extends RBCompoundTerm {

    Object javaObject;
    RepAsJavaConstructorType typeTag;
    
    public RBRepAsJavaObjectCompoundTerm(RepAsJavaConstructorType type, Object obj) {
        typeTag = type;
        javaObject = obj;
    }

    public RBTerm getArg() {
        return RBCompoundTerm.makeJava(javaObject);
    }
    
    public RBTerm getArg(int i) {
        if (i==0)
            return this;
        else
            throw new Error("Argument not found "+i);
    }
    
    public int getNumArgs() {
        return 1;
    }
    
    boolean freefor(RBVariable v) {
        return true;
    }
    
    public boolean isGround() {
        return true;
    }
    
    protected boolean sameForm(RBTerm other, Frame lr, Frame rl) {
        return equals(other);
    }
    
    protected Type getType(TypeEnv env) throws TypeModeError {
        return typeTag.apply(Factory.makeSubAtomicType(typeTag.getTypeConst()));
    }
    
    public int formHashCode() {
        return 17 * typeTag.hashCode() + javaObject.hashCode();
    }
    public int hashCode() {
        return 17 * typeTag.hashCode() + javaObject.hashCode();
    }
    
    public BindingMode getBindingMode(ModeCheckContext context) {
        return Factory.makeBound();
    }
    
    public void makeAllBound(ModeCheckContext context) {
        //already is all bound by definition
    }
    
    public boolean equals(Object x) {
        if (x.getClass().equals(this.getClass())) {
            RBRepAsJavaObjectCompoundTerm cx = (RBRepAsJavaObjectCompoundTerm) x;
            return javaObject.equals(cx.javaObject) && typeTag.equals(cx.typeTag);
        } else {
            return false;
        }
    }
    
    public Object accept(TermVisitor v) {
        //TODO: should this call visit?
        return this;
    }
    
    public boolean isOfType(TypeConstructor t) {
        return t.isSuperTypeOf(getTypeConstructor());
    }
       
    public Frame unify(RBTerm other, Frame f) {
        if (other instanceof RBVariable || other instanceof RBGenericCompoundTerm)
            return other.unify(this, f);
        else if (equals(other))
            return f;
        else
            return null;
    }

    public ConstructorType getConstructorType() {
        return typeTag;
    }
    
    /**
     * @see tyRuBa.util.TwoLevelKey#getFirst()
     */
    public String getFirst() {
        if (javaObject instanceof String) {
            String str = (String) javaObject;
            int firstindexofhash = str.indexOf('#');
            if (firstindexofhash == -1) {
                return " ";
            } else {
                return str.substring(0, firstindexofhash).intern();
            }
        } else if (javaObject instanceof Number) {
            return ((Number)javaObject).toString();
        } else if (javaObject instanceof TwoLevelKey) {
            return ((TwoLevelKey) javaObject).getFirst();
        } else {
            throw new Error("This object does not support TwoLevelKey indexing: " + javaObject);
        }
    }

    /**
     * @see tyRuBa.util.TwoLevelKey#getSecond()
     */
    public Object getSecond() {
        if (javaObject instanceof String) {
            String str = (String) javaObject;
            int firstindexofhash = str.indexOf('#');
            if (firstindexofhash == -1) {
                return typeTag.getFunctorId().toString() + str;
            } else {
                return typeTag.getFunctorId().toString() + str.substring(firstindexofhash).intern();
            }
        } else if (javaObject instanceof Number) {
            return ((Number)javaObject).toString();
        } else if (javaObject instanceof TwoLevelKey) {
            return ((TwoLevelKey) javaObject).getSecond();
        } else {
            throw new Error("This object does not support TwoLevelKey indexing: " + javaObject);
        }
    }
    
    public String toString() {
    		if (javaObject instanceof String) {
    			String javaString = (String)javaObject;
    			return "\""+javaString+"\"" //TODO: properly make escape sequences for special chars.
				+ "::" + typeTag.getFunctorId().getName();
    		}
    		else
    			return javaObject.toString() + "::" + typeTag.getFunctorId().getName();
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (javaObject instanceof String) {
            javaObject = ((String)javaObject).intern();
        }
    }
    
    public int intValue() {
        if (javaObject instanceof Integer) {
            return ((Integer)javaObject).intValue();
        } else {
            return super.intValue();
        }
    }

    public Object getValue() {
        return javaObject;
    }

}
