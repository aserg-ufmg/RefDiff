package pkg;

public class Foo {
  public static void main(String[] args) {
    hello(args);
  }

  public Foo() {
    hello();
  }

  public static int hello() {
    return 0;
  }

  public static int hello(int x, String... chars) {
    return 0;
  }

  public static int hello(@Hoge int x) {
    return 0;
  }

  public static void hello(String[] args) {
    System.out.println("Hello, World!");
  }
}
