package evaluation;

import org.treesitter.*;
import java.util.*;

public class Sandbox {
  public static String getLargeASTSource() {
    // 501 classes, 10000 methods and 501 constructors
    StringBuilder sb = new StringBuilder();
    sb.append("class Parent {\n");
    sb.append("  Parent() {}\n");
    for (int i = 0; i < 500; i++) {
      for (int j = 0; j < 10; j++) {
        sb.append("  void method").append(i).append("_").append(j).append("() {\n");
        sb.append("    String message = \"Hello, World!\";\n");
        sb.append("    System.out.println(message);\n");
        sb.append("  }\n");
      }
      sb.append("  class Child").append(i).append(" {\n");
      sb.append("    Child").append(i).append("() {}\n");
      for (int j = 0; j < 10; j++) {
        sb.append("    void method").append(i).append("_").append(j).append("() {\n");
        sb.append("      String message = \"Hello, World!\";\n");
        sb.append("      System.out.println(message);\n");
        sb.append("    }\n");
      }
      sb.append("  }\n");
    }
    sb.append("}\n");
    String source = sb.toString();
    return source;
  }

  public static String getSmallASTSource() {
    return """
      class Parent {
        Parent() {
          System.out.println("Constructor");
        }

        void methodA(int x) {
          System.out.println("Method A");
        }
    
        class Child {
          Child() {
            System.out.println("Constructor");
          }
          
          class GrandChild {
            GrandChild() {
              System.out.println("GrandChild Constructor");
            }
          }

          void methodC() {
            System.out.println("Method C");
          }
        }

        void methodB() {
          System.out.println("Method B");
        }
      }
      class AnotherClass {
        AnotherClass() {
          System.out.println("AnotherClass Constructor");
        }

        class InnerClass {
          InnerClass() {
            System.out.println("InnerClass Constructor");
          }

          void innerMethod() {
            System.out.println("Inner Method");
          }
        }

        void anotherMethod() {
          System.out.println("Another Method");
        }
      }
      """;
  }

  public static String getMethodSource() {
    return """
    /**
         * A sample class demonstrating all types of method argument definitions in Java
         */
        public class ArgumentExamples {

            // 1. No Parameters
            // The most basic form
            public void noParameters() {
                System.out.println("No parameters");
            }

            // 2. Single Formal Parameter (Primitive)
            // This is a standard 'formal_parameter'
            public void singlePrimitive(int number) {
                System.out.println("Received number: " + number);
            }

            // 3. Single Formal Parameter (Object)
            // This is also a 'formal_parameter'
            public void singleObject(String text) {
                System.out.println("Received text: " + text);
            }

            // 4. Multiple Formal Parameters
            // A list of 'formal_parameter' nodes
            public void multipleParameters(String name, int age) {
                System.out.println(name + " is " + age + " years old");
            }

            // 5. Final Formal Parameter
            // A 'formal_parameter' with a 'final' modifier
            public void finalParameter(final String immutableMessage) {
                // immutableMessage = "Change"; // This line would cause a compile error
                System.out.println("Final parameter: " + immutableMessage);
            }

            // 6. Array Parameter
            // A 'formal_parameter' where the type is an array
            public void arrayParameter(String[] stringArray) {
                System.out.println("First element of array: " + (stringArray.length > 0 ? stringArray[0] : "None"));
            }

            // 7. Variable-Length Arguments (Varargs)
            // This maps to TreeSitter's 'varargs_parameter',
            // which is what you're referring to as 'spread_parameter'.
            public void variableArity(String... names) {
                System.out.println("--- Variable-Length Arguments ---");
                if (names.length == 0) {
                    System.out.println("No names provided");
                }
                for (String name : names) {
                    System.out.println("Name: " + name);
                }
            }

            // 8. Varargs (with other parameters)
            // The 'varargs_parameter' must be the last parameter
            public void mixedVarargs(int id, String... details) {
                System.out.println("ID: " + id);
                for (String detail : details) {
                    System.out.println("Detail: " + detail);
                }
            }

            // 9. Interface Parameter
            // A 'formal_parameter' whose type is an interface (polymorphism)
            public void interfaceParameter(Runnable task) {
                System.out.println("Executing task...");
                task.run();
            }

            // (Reference) Calling example for 9.
            public void callInterfaceParameter() {
                // Passing a lambda expression
                interfaceParameter(() -> System.out.println("Task execution via lambda"));

                // Passing an anonymous class
                interfaceParameter(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Task execution via anonymous class");
                    }
                });
            }

            // 10. Generic Method
            // A 'formal_parameter' with a generic type <T>
            public <T> void genericMethod(T item) {
                System.out.println("Received item's class: " + item.getClass().getName());
                System.out.println("Item: " + item);
            }

            // 11. Bounded Generics
            // A 'formal_parameter' with a bounded generic type <T extends Number>
            public <T extends Number> void boundedGeneric(T number) {
                System.out.println("Received number (double value): " + number.doubleValue());
            }

            // 12. Receiver Parameter (Explicit 'this')
            // This maps to TreeSitter's 'receiver_parameter'.
            // It must be the first parameter and names the 'this' instance.
            // It's often used to place annotations on the 'this' type.
            public void receiverParameter(/* @NonNull */ ArgumentExamples this, int someValue) {
                System.out.println("--- Receiver Parameter ---");
                System.out.println("Value: " + someValue);
                // 'this' refers to the instance of ArgumentExamples, as usual.
                System.out.println("Are this and the instance equal? " + (this == this));
            }

            // Main method (for execution check)
            public static void main(String[] args) {
                ArgumentExamples examples = new ArgumentExamples();

                System.out.println("--- 2, 3, 4 (Formal Parameters) ---");
                examples.singlePrimitive(100);
                examples.singleObject("Hello");
                examples.multipleParameters("Yamada", 30);

                System.out.println("\n--- 5 (Final Formal Parameter) ---");
                examples.finalParameter("This value cannot be changed");

                System.out.println("\n--- 6 (Array Parameter) ---");
                examples.arrayParameter(new String[]{"A", "B", "C"});

                System.out.println("\n--- 7 (Spread/Varargs Parameter) ---");
                examples.variableArity(); // 0 args
                examples.variableArity("Sato"); // 1 arg
                examples.variableArity("Suzuki", "Takahashi", "Tanaka"); // 3 args

                System.out.println("\n--- 8 (Mixed Varargs) ---");
                examples.mixedVarargs(1, "Detail A", "Detail B");

                System.out.println("\n--- 9 (Interface Parameter) ---");
                examples.callInterfaceParameter();

                System.out.println("\n--- 10 (Generic Parameter) ---");
                examples.genericMethod("A string"); // T is String
                examples.genericMethod(123);      // T is Integer

                System.out.println("\n--- 11 (Bounded Generic Parameter) ---");
                examples.boundedGeneric(123.45); // Double type
                examples.boundedGeneric(500);    // Integer type

                System.out.println("\n--- 12 (Receiver Parameter) ---");
                examples.receiverParameter(42);
            }
        }
    """;
  }

  public static void main(String[] args) {
    // ---------- Generate synthetic large Java code ----------
    // String source = getLargeASTSource();
    // String source = getSmallASTSource();
    String source = getMethodSource();
    System.out.println(source.substring(0, Math.min(1500, source.length())) + "...");
    System.out.printf("Source loc: %d lines, %d bytes%n",
        source.split("\n").length, source.getBytes().length);

    TSParser parser = new TSParser();
    parser.setLanguage(new TreeSitterJava());
    TSTree tree = parser.parseString(null, source);

    // ---------- Queries ----------
    // String preciseQuery = """
    //     [
    //       (class_declaration
    //         name: (identifier) @name
    //         body: (class_body) @body
    //       ) @decl
    //       (constructor_declaration
    //         name: (identifier) @name
    //         parameters: (_) @params
    //         body: (constructor_body) @body
    //       ) @decl
    //       (method_declaration
    //         name: (identifier) @name
    //         parameters: (_) @params
    //         body: (block) @body
    //       ) @decl
    //     ]
    //     """;
        String preciseQuery = """
        [
              (program
                (_) @body
              ) @declaration
              (class_declaration
                name: (identifier) @name
                body: (class_body) @body
              ) @declaration
               (interface_declaration
                name: (identifier) @name
                body: (interface_body) @body
              ) @declaration
              (enum_declaration
                  name: (identifier) @name
                  body: (enum_body) @body
              ) @declaration
              (constructor_declaration
                  name: (identifier) @name
                  parameters: (formal_parameters) @parameters
                  body: (constructor_body) @body
              ) @declaration
              (method_declaration
                  name: (identifier) @name
                  parameters: (formal_parameters) @parameters
                  body: ((block) @body)?
              ) @declaration
            ]
        """;
    // String preciseQuery = """
    //     [
    //       (class_declaration
    //         name: (identifier) @name
    //         body: (class_body) @body
    //       ) @decl
    //       (constructor_declaration
    //         name: (identifier) @name
    //         body: (constructor_body) @body
    //       ) @decl
    //       (method_declaration
    //         name: (identifier) @name
    //         body: (block) @body
    //       ) @decl
    //       (formal_parameters) @params
    //     ]
    //     """;

    String simpleQuery = """
        [
          (class_declaration)  @class
          (constructor_declaration) @constructor
          (method_declaration) @method
        ]
        """;

    // System.out.println("\n=== 精密クエリ版 ===");
    RootNode root = new RootNode();
    benchmark(Sandbox::addNodes, tree, root, source, preciseQuery);
    for (NodeInfo n : root.childs) {
      System.out.println(n.type + " " + n.name + " " + n.getParameter() + " [" + n.start + ", " + n.end + "] with " + n.childs.size() + " children");
      for (NodeInfo c : n.childs) {
        System.out.println("  - " + c.type + " " + c.name + " " + c.getParameter() + " [" + c.start + ", " + c.end + "]");
        for (NodeInfo cc : c.childs) {
          System.out.println("    - " + cc.type + " " + cc.name + " " + cc.getParameter() + " [" + cc.start + ", " + cc.end + "]");
          for (NodeInfo ccc : cc.childs) {
            System.out.println("      - " + ccc.type + " " + ccc.name + " " + ccc.getParameter() + " [" + ccc.start + ", " + ccc.end + "]");
          }
        }
      }
    }

    // System.out.println("\n=== 浅いクエリ + Stack構築版 ===");
    // benchmarkWithStack(tree, source, simpleQuery);
  }
  
  static void addNodes(TSTree tree, RootNode root, String source, String querySrc) {
    TSQuery query = new TSQuery(new TreeSitterJava(), querySrc);
    TSQueryCursor cursor = new TSQueryCursor();
    cursor.exec(query, tree.getRootNode());

    TSQueryMatch match = new TSQueryMatch();
    Deque<NodeInfo> stack = new ArrayDeque<>(); // スタックを利用して親子関係を構築
    while (cursor.nextMatch(match)) {
        TSNode name = null;
        TSNode body = null;
        TSNode decl = null;
        TSNode params = null;
        for (TSQueryCapture capture : match.getCaptures()) {
            TSNode capturedNode = capture.getNode();
            String captureName = query.getCaptureNameForId(capture.getIndex());
            switch (captureName) {
                case "name" -> name = capturedNode;
                case "body" -> body = capturedNode;
                case "params" -> params = capturedNode;
                case "declaration" -> decl = capturedNode;
                default -> {}
            }
        }

        NodeInfo node = null;
        switch (decl.getType()) {
          case "program":
            node = new NodeInfo(
                decl.getType(),
                "sample.php",
                decl.getStartByte(),
                decl.getEndByte()
            );
            break;
          case "class_declaration":
          case "interface_declaration":
          case "enum_declaration":
            node = new NodeInfo(
                decl.getType(),
                source.substring(name.getStartByte(), name.getEndByte()),
                body.getStartByte(),
                body.getEndByte()
            );
            break;
          case "constructor_declaration":
          case "method_declaration":
            node = new NodeInfo(
                decl.getType(),
                source.substring(name.getStartByte(), name.getEndByte()),
                body.isNull() ? decl.getStartByte() : body.getStartByte(),
                body.isNull() ? decl.getEndByte() : body.getEndByte()
            );
            if (params != null) {
              String paramStr = source.substring(params.getStartByte(), params.getEndByte());
              node.addParameter(paramStr);
            }
            break;
        }

        if (node == null) {
          continue;
        }

        // スタックを使って親子関係を構築
        while (!stack.isEmpty() && stack.peek().end < node.start) {
            stack.pop(); // スタックのトップが現在のノードの親でない場合、スタックから削除
        }
        if (!stack.isEmpty()) {
            stack.peek().addChild(node); // スタックのトップが親ノード
        } else {
            root.addChild(node); // スタックが空ならルートノードに追加
        }
        stack.push(node); // 現在のノードをスタックに追加
        // } else if (params != null && !stack.isEmpty()) {
        //     // パラメータノードの場合、現在のスタックトップにパラメータ情報を追加
        //     String paramStr = source.substring(params.getStartByte(), params.getEndByte());
        //     stack.peek().addParameter(paramStr);
        // }

    }
  }

  @FunctionalInterface
  interface TriConsumer<A, B, C, D> {
    void accept(A a, B b, C c, D d);
  }

  static void benchmark(TriConsumer<TSTree, RootNode, String, String> fn, TSTree tree, RootNode root, String source,
      String query) {
    long start = System.nanoTime();
    fn.accept(tree, root, source, query);
    long end = System.nanoTime();
    double ms = (end - start) / 1_000_000.0;
    System.out.printf("Execution Time: %.2f ms%n", ms);
  }

  static void benchmarkWithStack(TSTree tree, String source, String querySrc) {
    long start = System.nanoTime();

    TSQuery query = new TSQuery(new TreeSitterJava(), querySrc);
    TSQueryCursor cursor = new TSQueryCursor();
    cursor.exec(query, tree.getRootNode());

    List<NodeInfo> nodes = new ArrayList<>();
    TSQueryMatch match = new TSQueryMatch();
    while (cursor.nextMatch(match)) {
      TSNode name = null;
    }

    // // stackで親子関係を再構築
    // nodes.sort(Comparator.comparingInt(n -> n.start)); // start位置順に並び替え
    // Deque<NodeInfo> stack = new ArrayDeque<>();
    // Map<NodeInfo, List<NodeInfo>> treeMap = new HashMap<>();

    // for (NodeInfo n : nodes) {
    //   while (!stack.isEmpty() && stack.peek().end < n.start)
    //     stack.pop();
    //   if (!stack.isEmpty()) {
    //     treeMap.computeIfAbsent(stack.peek(), depth -> new ArrayList<>()).add(n);
    //   }
    //   stack.push(n);
    // }

    long end = System.nanoTime();
    double ms = (end - start) / 1_000_000.0;
    System.out.printf("Nodes: %d, Time: %.2f ms%n",
        nodes.size(), ms);
  }
}

class RootNode {
    List<NodeInfo> childs = new ArrayList<>();

    void addChild(NodeInfo child) {
      this.childs.add(child);
    }
  }

class NodeInfo {
    String type;
    String name;
    int start;
    int end;
    String parameter;
    List<NodeInfo> childs;

    NodeInfo(String type, String name, int start, int end) {
      this.type = type;
      this.name = name;
      this.start = start;
      this.end = end;
      this.childs = new ArrayList<>();
    }

    void addChild(NodeInfo child) {
      this.childs.add(child);
    }

    void addParameter(String param) {
      this.parameter = param;
    }

    String getParameter() {
      if (this.parameter == null) return "";
      return this.parameter;
    }
  }
