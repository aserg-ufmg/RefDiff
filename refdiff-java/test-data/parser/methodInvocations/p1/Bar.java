package p1;

import p2.Foo;

public class Bar {
	
	public void m1(String arg) {
		m2(3);
	}
	
	public void m2() {
		System.out.println("M1");
	}
	
	public void m2(int x) {
		System.out.println("M2");
	}
	
}