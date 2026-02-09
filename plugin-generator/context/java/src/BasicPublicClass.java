/**
 * このファイルは、Javaのソースコードパーサーのテストを目的として、
 * 様々なクラス定義のパターンを網羅しています。
 *
 * @author Your Parser Test Suite
 * @version 1.0
 */
package com.example.parser.test;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

// =========================================================================
// 1. 基本的なクラス定義 (Basic Class Definitions)
// =========================================================================

/**
 * 最も基本的なpublicクラス。フィールド、コンストラクタ、メソッドを含みます。
 */
public class BasicPublicClass {

  public String publicField;
  private int privateField;
  protected static final double CONSTANT = 3.14;

  public BasicPublicClass(String value) {
    this.publicField = value;
  }

  public void publicMethod() {
    // no-op
  }

  private String privateMethod(int i) {
    return "value: " + i;
  }
}

/**
 * アクセス修飾子なし (package-private) のクラス。
 * 同一ファイル内に複数のトップレベルクラスを定義する場合、publicは1つまでです。
 */
class PackagePrivateClass {
  // package-private field
  String name;
}

// =========================================================================
// 2. 修飾子を持つクラス (Classes with Modifiers)
// =========================================================================

/**
 * abstractクラス。インスタンス化できず、抽象メソッドを持つことができます。
 */
abstract class AbstractVehicle {
  public abstract void move();

  public String getVehicleType() {
    return "Generic Vehicle";
  }
}

/**
 * finalクラス。これ以上継承することはできません。
 */
final class FinalImmutableData {
  private final String data;

  public FinalImmutableData(String data) {
    this.data = data;
  }

  public String getData() {
    return data;
  }
}

// =========================================================================
// 3. 継承とインターフェース (Inheritance and Interfaces)
// =========================================================================

/**
 * 抽象クラスを継承するクラス。
 */
class Car extends AbstractVehicle {
  @Override
  public void move() {
    System.out.println("The car is driving.");
  }
}

/**
 * 複数のインターフェースを実装するクラス。
 */
class MultiImplementer implements Runnable, Serializable, Cloneable {
  @Override
  public void run() {
    // implementação
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}

/**
 * 他のクラスを継承し、かつインターフェースを実装するクラス。
 */
class ComplexHierarchy extends Car implements Comparable<ComplexHierarchy> {
  private int speed;

  @Override
  public int compareTo(ComplexHierarchy other) {
    return Integer.compare(this.speed, other.speed);
  }
}

// =========================================================================
// 4. ジェネリクス (Generics)
// =========================================================================

/**
 * 単純なジェネリクス型パラメータを持つクラス。
 * 
 * @param <T> 型パラメータ
 */
class Box<T> {
  private T item;

  public void set(T item) {
    this.item = item;
  }

  public T get() {
    return item;
  }
}

/**
 * 複数のジェネリクス型パラメータと境界を持つクラス。
 * 
 * @param <K> Key。Stringを継承している必要がある。
 * @param <V> Value。Mapを実装している必要がある。
 */
class BoundedGenericCache<K extends String, V extends Map<String, ?>> {
  private V cache;

  public BoundedGenericCache(V cache) {
    this.cache = cache;
  }
}

/**
 * ワイルドカードを使用したジェネリクス。
 * 
 * @param <N> Numberとそのサブクラスに限定
 */
class NumberProcessor<N extends Number> {
  private List<? super N> list;

  public void process(List<? extends N> numbers) {
    // ...
  }
}

// =========================================================================
// 5. ネストしたクラス (Nested Classes)
// =========================================================================

/**
 * 様々な種類のネストクラスを含む外側のクラス。
 */
class OuterShell {
  private final int outerPrivateVar = 10;
  private static final int outerStaticVar = 20;

  /**
   * 静的ネストクラス (Static Nested Class)。
   * 外側のクラスのインスタンスに依存しません。
   */
  static class StaticNested {
    void display() {
      // System.out.println(outerPrivateVar); // コンパイルエラー
      System.out.println(outerStaticVar);
    }
  }

  /**
   * 内部クラス (Inner Class)。
   * 外側のクラスのインスタンスに束縛されます。
   */
  class Inner {
    void display() {
      System.out.println(outerPrivateVar); // アクセス可能
      System.out.println(outerStaticVar);
    }
  }

  public Runnable createRunnable() {
    final int methodLocalVar = 30;

    /**
     * ローカル内部クラス (Local Inner Class)。
     * メソッドスコープ内で定義されます。
     */
    class MethodLocalRunnable implements Runnable {
      @Override
      public void run() {
        System.out.println(methodLocalVar);
        System.out.println(outerPrivateVar);
      }
    }
    return new MethodLocalRunnable();
  }

  public Comparable<String> createAnonymousClass() {
    /**
     * 匿名内部クラス (Anonymous Inner Class)。
     * インターフェースや抽象クラスをその場で実装・継承します。
     */
    return new Comparable<String>() {
      @Override
      public int compareTo(String o) {
        // 匿名クラスの本体
        System.out.println(outerPrivateVar);
        return 0;
      }
    };
  }
}

// =========================================================================
// 6. 特殊な種類のクラス (Special Kinds of Classes)
// =========================================================================

/**
 * 列挙型 (enum)。定数のグループを定義します。
 * 内部的には java.lang.Enum を継承した特殊なクラスです。
 */
enum Signal {
  RED("STOP"),
  YELLOW("WAIT"),
  GREEN("GO");

  private final String action;

  Signal(String action) {
    this.action = action;
  }

  public String getAction() {
    return this.action;
  }
}

/**
 * レコード型 (record)。Java 16から導入されました。
 * 不変なデータキャリアを簡潔に記述するための特殊なクラスです。
 */
record Point(int x, int y) implements Serializable {
  // コンパクトコンストラクタ (Compact Constructor)
  public Point {
    if (x < 0 || y < 0) {
      throw new IllegalArgumentException("Coordinates must be non-negative");
    }
  }

  // 静的メソッド
  public static Point origin() {
    return new Point(0, 0);
  }
}

/**
 * インターフェース定義。これも型定義の一種です。
 * defaultメソッドやstaticメソッドを持つことができます。
 */
interface Drawable<T> {
  void draw(T context);

  default void clear() {
    System.out.println("Clearing the canvas.");
  }

  static int getVersion() {
    return 2;
  }
}

/**
 * アノテーション定義 (@interface)。特殊なインターフェースです。
 */
@Target(ElementType.TYPE)
@interface ClassMarker {
  String value();

  int priority() default 0;
}

// =========================================================================
// 7. その他の構文 (Miscellaneous Syntaxes)
// =========================================================================

/**
 * アノテーションが付与されたクラス。
 */
@Deprecated
@ClassMarker(value = "Important", priority = 1)
class AnnotatedClass {
  // 静的初期化ブロック
  static {
    System.out.println("Static initializer block");
  }

  // インスタンス初期化ブロック
  {
    System.out.println("Instance initializer block");
  }
}

/**
 * 中身が何もない、空のクラス。
 * マーカーとして使われることがあります。
 */
class EmptyClass {
}

/*
 * このファイルはここまでです。
 * These patterns should cover a wide range of parsing scenarios.
 */