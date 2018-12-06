package p1;

import p2.Foo;

public class Bar extends Foo {
	
	public void m1(String arg) {
		m2();
	}
	
	public void m2() {
		System.out.println("Hello world");
	}
	
}