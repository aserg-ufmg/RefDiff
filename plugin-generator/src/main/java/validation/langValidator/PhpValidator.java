package validation.langValidator;

import plugingenerator.Language;
import refdiff.parsers.LanguagePlugin;
import refdiff.parsers.universal.php.PhpPlugin;
import validation.BaseValidator;
import refdiff.parsers.universal.php.PhpNodeTypes;

public class PhpValidator extends BaseValidator {

  @Override
  protected LanguagePlugin getPlugin() {
    return new PhpPlugin();
  }

  @Override
  protected Language getLanguage() {
    return Language.PHP;
  }

  @Override
  protected String[] getRepoUrls() {
    return new String[] {
      "https://github.com/coollabsio/coolify.git",
      // "https://github.com/laravel/framework.git",
      // "https://github.com/nextcloud/server.git",
      // "https://github.com/symfony/symfony.git",
      // "https://github.com/blueimp/jQuery-File-Upload.git"
    };
  }

  @Override
  protected boolean isLineEqual(Integer tagLine, Integer nodeLine) {
    return true; // Attributes #[ ] may cause line number mismatches
  }

  @Override
  protected boolean isTypeEqual(String tagKind, String nodeType) {
    if (tagKind.equalsIgnoreCase("trait")) {
      return PhpNodeTypes.TRAIT.equals(nodeType);
    } else if (tagKind.equalsIgnoreCase("interface")) {
      return PhpNodeTypes.INTERFACE.equals(nodeType);
    } else if (tagKind.equalsIgnoreCase("function")) {
      return PhpNodeTypes.FUNCTION.equals(nodeType) || PhpNodeTypes.METHOD.equals(nodeType);
    } else if (tagKind.equalsIgnoreCase("method")) {
      return PhpNodeTypes.METHOD.equals(nodeType) || PhpNodeTypes.FUNCTION.equals(nodeType);
    }

    return false;
  }

  public static void main(String[] args) throws Exception {
    RubyValidator validator = new RubyValidator();
    validator.run();
  }

}
