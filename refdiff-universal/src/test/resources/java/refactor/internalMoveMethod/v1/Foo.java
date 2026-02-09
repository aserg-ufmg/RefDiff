package pkg;

public class Foo {
  public static void main(String[] args) {
    hello(args);
  }

  private class Bar {
  }

  public static void hello(String[] args) {
    System.out.println("Hello, World!");
  }
}