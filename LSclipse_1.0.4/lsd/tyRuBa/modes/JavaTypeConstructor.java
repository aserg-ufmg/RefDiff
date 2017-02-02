package tyRuBa.modes;

import java.io.Serializable;

public class JavaTypeConstructor extends TypeConstructor implements Serializable {

	private final Class javaClass;

	/** Constructor */
	public JavaTypeConstructor(Class javaclass) {
		if (javaclass.isInterface() || javaclass.isPrimitive()) {
			throw new Error("no interfaces or primitives types are allowed");
		} else {
			this.javaClass = javaclass;
		}
	}
	
	public String getName() {
		String name = javaClass.getName();
		if (name.startsWith("java.lang.")) {
			return name.substring("java.lang.".length());
		} else {
			return name;
		}
	}

	public boolean equals(Object other) {
		if (other != null && this.getClass().equals(other.getClass())) {
			return this.getName().equals(((TypeConstructor)other).getName());
		} else {
			return false;
		}
	}

	public int hashCode() {
		return getName().hashCode();
	}

	public void addSuperType(TypeConstructor superType) throws TypeModeError {
		throw new TypeModeError("Can not add super type for java types " + this);
	}

    public TypeConstructor getSuperTypeConstructor() {
        if (javaClass.getSuperclass() == null)
            return null;
        else
            return new JavaTypeConstructor(javaClass.getSuperclass());
    }

    public int getTypeArity() {
        return 0;
    }

    public String getParameterName(int i) {
        throw new Error("This is not a user defined type");
    }

    /* (non-Javadoc)
     * @see tyRuBa.modes.TypeConstructor#isInitialized()
     */
    public boolean isInitialized() {
        throw new Error("This is not a user defined type");
    }

    public ConstructorType getConstructorType() {
        return ConstructorType.makeJava(javaClass);
    }
    
    public boolean isJavaTypeConstructor() {
        return true;
    }

    public String toString() {
    		return "JavaTypeConstructor("+javaClass+")";
    }
    
	public Class javaEquivalent() {
		return javaClass;
	}
}
