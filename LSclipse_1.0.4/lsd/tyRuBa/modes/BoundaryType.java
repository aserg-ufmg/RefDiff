/*
 * Created on May 28, 2004
 */
package tyRuBa.modes;

import java.util.HashMap;

public abstract class BoundaryType extends Type {

    abstract boolean isStrict();

    public boolean isSuperTypeOf(Type other) {
        return other.isSubTypeOf(this,new HashMap());
    }

}
