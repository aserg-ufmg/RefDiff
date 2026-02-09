package validation.langValidator;

import plugingenerator.Language;
import refdiff.parsers.LanguagePlugin;
import refdiff.parsers.universal.go.GoParser;
import validation.BaseValidator;
import refdiff.parsers.universal.go.GoNodeTypes;

public class GoValidator extends BaseValidator {

  @Override
  protected LanguagePlugin getPlugin() {
    return new GoParser();
  }

  @Override
  protected Language getLanguage() {
    return Language.GO;
  }

  @Override
  protected String[] getRepoUrls() {
    return new String[] {
      "https://github.com/ollama/ollama.git",
      // "https://github.com/golang/go.git",
      // "https://github.com/kubernetes/kubernetes.git",
      // "https://github.com/fatedier/frp.git",
      // "https://github.com/gin-gonic/gin.git",
    };
  }

  @Override
  protected boolean isTypeEqual(String tagKind, String nodeType) {
    if (tagKind.equalsIgnoreCase("file")) {
      return GoNodeTypes.FILE.equals(nodeType);
    }
    if (tagKind.equalsIgnoreCase("func")) {
      return GoNodeTypes.FUNCTION.equals(nodeType) || GoNodeTypes.METHOD.equals(nodeType);
    }
    return false;
  }

  public static void main(String[] args) throws Exception {
    GoValidator validator = new GoValidator();
    validator.run();
  }

}
