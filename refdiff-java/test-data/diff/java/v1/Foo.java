public class Foo {
	
	public void m1(String arg) {
		m2();
	}
	
	public void m2() {
		System.out.println("Hello world");
	}
	
	public void m3(String arg) {
		m2();
	}
}