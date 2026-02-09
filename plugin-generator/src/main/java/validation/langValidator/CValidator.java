package validation.langValidator;

import plugingenerator.Language;
import refdiff.parsers.LanguagePlugin;
import refdiff.parsers.universal.c.CPlugin;
import validation.BaseValidator;
import refdiff.parsers.universal.c.CNodeTypes;

public class CValidator extends BaseValidator {

  @Override
  protected LanguagePlugin getPlugin() {
    return new CPlugin();
  }

  @Override
  protected Language getLanguage() {
    return Language.C;
  }

  @Override
  protected String[] getRepoUrls() {
    return new String[] {
      // "https://github.com/torvalds/linux.git",
      "https://github.com/Genymobile/scrcpy.git",
      // "https://github.com/netdata/netdata.git",
      // "https://github.com/ventoy/Ventoy.git",
      // "https://github.com/redis/redis.git",
    };
  }

  @Override
  protected boolean isTypeEqual(String tagKind, String nodeType) {
    if (tagKind.equalsIgnoreCase("file")) {
      return CNodeTypes.FILE.equals(nodeType);
    }
    if (tagKind.equalsIgnoreCase("function")) {
      return CNodeTypes.FUNCTION.equals(nodeType);
    }
    return false;
  }

  @Override
  protected boolean isLineEqual(Integer tagLine, Integer nodeLine) {
    if (Math.abs(tagLine - nodeLine) <= 1) {
      return true;
    }
    return false;
  }

  public static void main(String[] args) throws Exception {
    CValidator validator = new CValidator();
    validator.run();
  }

}
