/*
 * Created on Jun 11, 2004
 */
package tyRuBa.util;

import java.io.Serializable;

/**
 * @author riecken
 */
public class SimpleTwoLevelKey implements TwoLevelKey, Serializable {

    String first;
    Object second;
    
    public SimpleTwoLevelKey(String first, Object second) {
        this.first = first;
        if (this.first instanceof String) {
            this.first = ((String) this.first).intern();
        }
        this.second = second;
        if (this.second instanceof String) {
            this.second = ((String) this.second).intern();
        }
    }

    public String getFirst() {
        return first;
    }

    public Object getSecond() {
        return second;
    }

//    public String getFirstAsString() {
//        return first.toString();
//    }
}