package tyRuBa.util;

import java.util.ArrayList;

public class ArrayListSource extends ElementSource {

	int pos = 0;
	int sz;
	ArrayList els;
	
	public ArrayListSource(ArrayList els) {
		this.els = els;
		sz = els.size();
	}
	
	public int status() {
		return (pos < sz) ? ELEMENT_READY : NO_MORE_ELEMENTS;
	}
	public Object nextElement() {
		return els.get(pos++);
	}
	public void print(PrintingState p) {
		p.print("{");
		for (int i = pos; i < els.size(); i++) {
			if (i > 0)
				p.print(",");
			p.print(els.get(i).toString());
		}
		p.print("}");
	}
	
	public ElementSource first() {
		// An ArrayListSource is not lazy... so don;t bother being lazy either
		if (hasMoreElements())
			return ElementSource.singleton(nextElement());
		else
			return ElementSource.theEmpty;
	}

}
