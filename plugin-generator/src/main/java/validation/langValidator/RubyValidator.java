package validation.langValidator;

import plugingenerator.Language;
import refdiff.parsers.LanguagePlugin;
import refdiff.parsers.universal.ruby.RubyParser;
import validation.BaseValidator;
import refdiff.parsers.universal.ruby.RubyNodeTypes;

public class RubyValidator extends BaseValidator {

  @Override
  protected LanguagePlugin getPlugin() {
    return new RubyParser();
  }

  @Override
  protected Language getLanguage() {
    return Language.RUBY;
  }

  @Override
  protected String[] getRepoUrls() {
    return new String[] {
      "https://github.com/rails/rails.git",
      // "https://github.com/maybe-finance/maybe.git",
      // "https://github.com/jekyll/jekyll.git",
      // "https://github.com/mastodon/mastodon.git",
      // "https://github.com/huginn/huginn.git"
    };
  }

  @Override
  protected boolean isTypeEqual(String tagKind, String nodeType) {
    if (tagKind.equalsIgnoreCase("method")) {
      return RubyNodeTypes.METHOD.equals(nodeType);
    }
    return false;
  }

  public static void main(String[] args) throws Exception {
    RubyValidator validator = new RubyValidator();
    validator.run();
  }

}
