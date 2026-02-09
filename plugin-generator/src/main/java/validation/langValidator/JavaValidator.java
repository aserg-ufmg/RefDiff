package validation.langValidator;

import plugingenerator.Language;
import refdiff.core.cst.CstNode;
import refdiff.parsers.LanguagePlugin;
import refdiff.parsers.universal.java.JavaPlugin;
import validation.BaseValidator;
import validation.Tag;
import refdiff.parsers.universal.java.JavaNodeTypes;

public class JavaValidator extends BaseValidator {

  @Override
  protected LanguagePlugin getPlugin() {
    return new JavaPlugin();
  }

  @Override
  protected Language getLanguage() {
    return Language.JAVA;
  }

  @Override
  protected String[] getRepoUrls() {
    return new String[] {
      // "https://github.com/Snailclimb/JavaGuide.git",
      "https://github.com/macrozheng/mall.git",
      // "https://github.com/spring-projects/spring-boot.git",
      // "https://github.com/elastic/elasticsearch.git",
      // "https://github.com/NationalSecurityAgency/ghidra.git",
      // "https://github.com/spring-projects/spring-framework.git",
    };
  }

  @Override
  protected boolean isTypeEqual(String tagKind, String nodeType) {
    if (tagKind.equalsIgnoreCase("class")) {
      return JavaNodeTypes.CLASS.equals(nodeType);
    }
    if (tagKind.equalsIgnoreCase("interface")) {
      return JavaNodeTypes.INTERFACE.equals(nodeType);
    }
    if (tagKind.equalsIgnoreCase("enum")) {
      return JavaNodeTypes.ENUM.equals(nodeType);
    }
    if (tagKind.equalsIgnoreCase("method")) {
      return JavaNodeTypes.METHOD.equals(nodeType);
    }
    return false;
  }

  @Override
  protected boolean isLineEqual(Integer tagLine, Integer nodeLine) {
    return true;
  }

  @Override
  protected boolean isNameEqual(Tag tag, CstNode node) {
    if (tag.getKind().equals("method") && node.getSimpleName().equals("new")) { // constructor special case
      return true;
    }
    return false;
  }

  public static void main(String[] args) throws Exception {
    JavaValidator validator = new JavaValidator();
    validator.run();
  }

}
