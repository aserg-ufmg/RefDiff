package p1;

public class A {
	
	private int x;
	
	public String m1() {
		setX(3);
		return "" + getX();
	}
	
	public void m2() {
		return "Banana";
	}
	
	public void m3() {
		return "Apple";
	}
	
	public void m4() {
		return "Orange";
	}
	
	private int getX() {
		return x;
	}
	
	private int setX(int value) {
		x = value;
	}
}