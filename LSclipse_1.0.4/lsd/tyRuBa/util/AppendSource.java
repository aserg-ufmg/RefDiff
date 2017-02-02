package tyRuBa.util;

class AppendSource extends ElementSource {
	private ElementSource s1, s2;
	AppendSource(ElementSource s1, ElementSource s2) {
		this.s1 = s1;
		this.s2 = s2;
	}
	public int status() {
		int stat = s1.status();
		if (stat == ELEMENT_READY)
			return stat;
		else if (stat == NO_MORE_ELEMENTS) {
			s1 = s2;
			s2 = theEmpty;
			return s1.status();
		} else
			return s2.status();
	}
	public Object nextElement() {
		if (s1.status() == ELEMENT_READY)
			return s1.nextElement();
		else
			return s2.nextElement();
	}
	public void print(PrintingState p) {
		p.print("Append(");
		p.indent();p.newline();
			s1.print(p);
			p.newline();p.print("++");
			s2.print(p);		
		p.outdent();
		p.print(")");
	}
	
//	public ElementSource first() {
//		return s1.first().append(s2.first()).first();
//	}

}
