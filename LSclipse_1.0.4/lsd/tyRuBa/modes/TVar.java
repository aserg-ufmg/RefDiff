/*****************************************************************\
 * File:        TVar.java
 * Author:      TyRuBa
 * Meta author: Kris De Volder <kdvolder@cs.ubc.ca>
\*****************************************************************/
package tyRuBa.modes;

import java.util.Map;

/** 
 * A TVar is any unknown type.  Once the type of a TVar is known, the TVar is
 * treated like the type. 
 */
public class TVar extends Type {
	
	private Type content;
	static private int ctr = 1;
	private int id = ++ctr;
	private String name;

	/** Constructor */
	public TVar(String name) {
		this.name = name;
		content = null;
	}

	/** This gets what a typevariable is bound to, the type returned is guaranteed
	 * to be not a TVar! Returns null, if this variable is not bound.
	 */
	public Type getContents() {
		TVar me = derefTVar();
		if (me.content==null)
			return null;
		else
			return content;
	}

	public String toString() {
		TVar me = derefTVar();
		if (me.isFree()) {
			return "?" + me.getName() + "_" + me.id;
		} else { 
			return me.getContents().toString();
		}
	}

	public String getName() {
		TVar me = derefTVar();
		return me.name;
	}
	
	private void setContents(Type other) {
		content = other;
	}

	public void checkEqualTypes(Type other, boolean grow) throws TypeModeError {
		TVar me = derefTVar();
		if (me.equals(other)) {
			return;
		} else if (other instanceof TVar) {
			TVar otherVar = ((TVar)other).derefTVar();
			if (me.isFree())
				if (!(otherVar.isFreeFor(this))) {
					throw new TypeModeError("Recursion in inferred type " + this + " & " 
						+ otherVar);
				}
				else {
					me.setContents(otherVar);
				}
			else if (otherVar.isFree()){
				if (!(me.isFreeFor(otherVar))) {
					throw new TypeModeError("Recursion in inferred type " + this + " & " 
						+ otherVar);
				}
				else {
					otherVar.setContents(me);
				}
			}
			else {// both not free 
				me.content.checkEqualTypes(otherVar.content);
				me.setContents(otherVar);
//				otherVar.setContents(shrunkType);
			}
		} else if (me.isFree()) {
			if (!(other.isFreeFor(this))) {
				throw new TypeModeError("Recursion in inferred type " + this + " & " 
					+ other);
			}
			else
				me.setContents(other);
		} else { // At this point me is not free and other is a non tvar type
			me.content.checkEqualTypes(other);
		}
	}
	
	public boolean isSubTypeOf(Type declared, Map renamings) {
		TVar me = derefTVar();
		if (!me.isFree()) {
			return me.getContents().isSubTypeOf(declared, renamings);
		} else if (! (declared instanceof TVar)) {
			return false;
		} else {
			TVar vdeclared = ((TVar)declared).derefTVar();
			
			if (!vdeclared.isFree())
				return false;
			else {
				TVar renamed = (TVar)renamings.get(me);
				if (renamed == null) {
					renamings.put(me, vdeclared);
					return true;
				} else {
					return vdeclared.equals(renamed);
				}
			}
		}
	}

	private TVar derefTVar() {
		if (content!=null && content instanceof TVar) {
			return ((TVar)content).derefTVar();
		} else {
			return this;
		}
	}

	public boolean isFree() {
		return getContents() == null;
	}

	public boolean isFreeFor(TVar var) {
		TVar me = derefTVar();
		if (!me.isFree()) {
			return me.content.isFreeFor(var);
		} else {
			return var != me;
		}
	}

	public Type clone(Map varRenamings) {
		TVar me = derefTVar();
		TVar clone = (TVar)varRenamings.get(me);
		if (clone!=null)
			return clone;
		else {
			clone = new TVar(me.getName());
			clone.setContents(me.content==null ? 
			                    null : me.content.clone(varRenamings)); 
			varRenamings.put(me,clone);
			return clone;
		}
	}

	public Type union(Type other) throws TypeModeError {
		TVar me = derefTVar();
		if (!me.isFree()) {
			return me.getContents().union(other);
		} else if (me.equals(other)) {
			return me;
		} else {
			check(other.isFreeFor(me),me,other);
			me.setContents(other);
			return me.content;
		}
	}

//	Type lowerBound(Type other) throws TypeModeError {
//		TVar me = derefTVar();
//		if (! me.isFree()) {
//			return me.content.lowerBound(other);
//		} else {
//			me.setContents(other);
//			return me;
//		}
//	}

	
	public boolean equals(Object other) {
		if (!(other instanceof TVar))
			return false;
		else {
			return this.derefTVar()==((TVar)other).derefTVar();
		}
	}
	
	public int hashCode() {
		TVar aliasOfMe = this.derefTVar();
		if (aliasOfMe==this) 
			return super.hashCode();
		else
			return aliasOfMe.hashCode();		
	}

	public Type intersect(Type other) throws TypeModeError {
		TVar me = derefTVar();
		if (me.equals(other)) {
			return me;
		} else if (!me.isFree()) {
			me.setContents(me.content.intersect(other));
			return me.content.intersect(other);
		} else {
			check(other.isFreeFor(me),this,other);
			me.setContents(other);
			return other;
		}
	}
	
	public Type copyStrictPart() {
		if (isFree()) {
			return Factory.makeTVar(getName());
		} else {
			return getContents().copyStrictPart();
		}
	}

	public boolean hasOverlapWith(Type other) {
		TVar me = derefTVar();
		if (!me.isFree()) {
			return me.content.hasOverlapWith(other);
		} else {
			return true;
		}
	}

	public Type getParamType(String currName, Type repAs) {
		if (repAs instanceof TVar) {
			if (currName.equals(((TVar)repAs).getName())) {
				return this;
			} else {
				return null;
			}
		} else if (!isFree()) {
			return getContents().getParamType(currName, repAs);
		} else {
			return null;
		}
	}
	
	

    /* (non-Javadoc)
     * @see tyRuBa.modes.Type#javaEquivalent()
     */
    public Class javaEquivalent() throws TypeModeError {
                
        Type contents = getContents();
        if (contents != null) {
            return contents.javaEquivalent();
        } else {
            throw new TypeModeError("This type variable is empty, and therefore has no java equivalent");
        }
    }
}
