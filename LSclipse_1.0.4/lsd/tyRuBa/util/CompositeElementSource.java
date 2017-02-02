package tyRuBa.util;

import java.util.Vector;

/** This composite class was introduced as a more efficient way of
    representing long recursive chains of appended small ElementSources.
    THese occur for example in unifying something with a long ComponentVector
    */
public class CompositeElementSource extends ElementSource {
    Vector children = new Vector();
    
    /** Composite: adding a child */
    public void add(ElementSource child) {
        children.addElement(child);
    }

    /** Composite: child access */
    public ElementSource get(int i) {
        return (ElementSource)children.elementAt(i);
    }
    
    /** Composite: child count */
    public int numberOfChildren() {
        return children.size();
    }

    public int i=-1;
        
    public int status() {
        for (i=0;i<numberOfChildren();) {
            int stat = get(i).status();
            if (stat==ELEMENT_READY)
                return stat;
            else if (stat==NO_MORE_ELEMENTS) {
                children.removeElementAt(i); 
		//		if (i>0) {
		//    System.err.println();
		//    System.err.print("Inefficient removeElement at "+i+" from "+numberOfChildren());
		//}
                //XXX Inefficient: removal O(n) actually it is not to bad because usually it
                // is the first element which is removed and that is fast in a Vector. 
                // Perhaps linked list may be better though (frees up memory gradually) and also
                // efficient when not the first element.
            }
            else 
                i++;
        }
        if (numberOfChildren()==0) 
            return NO_MORE_ELEMENTS;
        else
            return NO_ELEMENTS_READY;
    }
    
    public Object nextElement() {
        int stat;
        if (!((i>=0) && (i<numberOfChildren())))
            stat=this.status();
        else
            stat = ELEMENT_READY;
        if (stat == ELEMENT_READY) {
            return get(i).nextElement();
        }
        else
            throw new java.lang.Error("No nextElement found in CompositeElementSource");
    }
    
    public void print(PrintingState p) {
    	p.print("Composite(");
    	p.indent();p.newline();
    	for (int i = 0; i < numberOfChildren(); i++) {
			get(i).print(p);
		}
    	p.outdent();
    	p.print(")");
    }

	public ElementSource simplify() {
		if (children.size() == 0)
			return ElementSource.theEmpty;
		if (children.size() == 1)
			return get(0);
		return this;
	}
}
