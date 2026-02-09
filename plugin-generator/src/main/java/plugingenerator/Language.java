package plugingenerator;

import java.util.function.Supplier;

import org.treesitter.*;

public enum Language {
  // JAVA("java", ".java", "--kinds-Java=pigcm", () -> new TreeSitterJava()),
  JAVA("java", new String[]{".java"}, "--kinds-Java=igcm", () -> new TreeSitterJava()), // TODO package無視したけど後で対応する
  C("c", new String[]{".c"}, "--kinds-C=cf", () -> new TreeSitterC()),
  JS("javascript", new String[]{".js", ".jsx"}, "--kinds-JavaScript=fcm", () -> new TreeSitterJavascript()),
  PYTHON("python", new String[] { ".py" }, "--kinds-Python=fm", () -> new TreeSitterPython()),
  GO("go", new String[]{".go"}, "--kinds-Go=f", () -> new TreeSitterGo()),
  PHP("php", new String[]{".php"}, "--php-kinds=f", () -> new TreeSitterPhp()),
  RUBY("ruby", new String[]{".rb"}, "--kinds-Ruby=f", () -> new TreeSitterRuby());
  // TS("ts", ".ts", "--kinds-TypeScript=fmGa", () -> new TreeSitterTypescript());
  // CS("cs", ".cs", "--kinds-CSharp=cfm", () -> new TreeSitterCSharp()),
  // CPP("cpp", ".cpp", "--kinds-C++=cfm", () -> new TreeSitterCpp());

  private final String name;
  private final String[] extensions;
  private final String ctagsOption;
  private final Supplier<TSLanguage> tsSupplier;

  Language(String name, String[] extensions, String ctagsOption, Supplier<TSLanguage> tsSupplier) {
    this.name = name;
    this.extensions = extensions;
    this.ctagsOption = ctagsOption;
    this.tsSupplier = tsSupplier;
  }

  public String getName() {
    return name;
  }

  public String[] getExtensions() {
    return extensions;
  }

  public String getCtagsOption() {
    return ctagsOption;
  }

  public TSLanguage getTSLanguage() {
    return tsSupplier.get();
  }

  public static Language fromName(String n) {
    for (Language l : values()) {
      if (l.name.equalsIgnoreCase(n))
        return l;
    }
    throw new IllegalArgumentException("Unknown language: " + n);
  }
}