/*****************************************************************\
 * File:        BindingList.java
 * Author:      TyRuBa
 * Meta author: Kris De Volder <kdvolder@cs.ubc.ca>
\*****************************************************************/
package tyRuBa.modes;

import java.util.ArrayList;

/** 
 * A BindingList is a list of BindingModes 
 */

public class BindingList {
	
	private ArrayList parts;

	public BindingList() {
		parts = new ArrayList();
	}
	
	public BindingList(BindingMode bm) {
		this();
		parts.add(bm);
	}

	public int hashCode() {
		final int size = size();
		int hash = getClass().hashCode();
		for (int i = 0; i < size; i++)
			hash = hash * 19 + get(i).hashCode();
		return hash;
	}

	public boolean equals(Object other) {
		if (other instanceof BindingList) {
			BindingList cother = (BindingList) other;
			if (this.size() != cother.size()) {
				return false;
			} else {
				for (int i = 0; i < size(); i++) {
					if (!(get(i).equals(cother.get(i))))
						return false;
				}
				return true;
			}
		} else {
			return false;
		}
	}

	public String toString() {
		StringBuffer result = new StringBuffer("(");
		int size = this.size();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				result.append(",");
			}
			result.append(this.get(i).toString());
		}
		result.append(")");
		return result.toString();
	}
	
	public String getBFString() {
		StringBuffer result = new StringBuffer();
		int size = this.size();
		for(int i = 0; i < size; i++) {
			if (get(i).isBound()) {
				result.append("B");
			} else {
				result.append("F");
			}
		}
		return result.toString();
	}

	/** add newPart to the end of the list */
	public void add(BindingMode newPart) {
		parts.add(newPart);
	}

	public BindingMode get(int i) {
		return (BindingMode) parts.get(i);
	}

	public int size() {
		return parts.size();
	}

	/** return true if this BindingList has no Free at a position where other
	 *  has Bound */
	public boolean satisfyBinding(BindingList other) {
		for (int i = 0; i < size(); i++) {
			if (!(get(i).satisfyBinding(other.get(i)))) {
				return false;
			}
		}
		return true;
	}

	public boolean hasFree() {
		for (int i = 0; i < size(); i++) 
			if (!get(i).isBound()) return true;
		return false;
	}

	public boolean isAllBound() {
		for (int i = 0; i < size(); i++) {
			if (!get(i).isBound()) {
				return false;
			}
		}
		return true;
	}

	public int getNumBound() {
		int result = 0; 
		for (int i = 0; i < size(); i++) {
			if (get(i).isBound()) {
				result++;
			}
		}
		return result;
	}

    public int getNumFree() {
        return size() - getNumBound();
    }
	
}
