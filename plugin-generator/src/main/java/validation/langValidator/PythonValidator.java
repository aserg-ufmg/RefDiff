package validation.langValidator;

import plugingenerator.Language;
import refdiff.parsers.LanguagePlugin;
import refdiff.parsers.universal.python.PythonParser;
import validation.BaseValidator;
import refdiff.parsers.universal.python.PythonNodeTypes;

public class PythonValidator extends BaseValidator {

  @Override
  protected LanguagePlugin getPlugin() {
    return new PythonParser();
  }

  @Override
  protected Language getLanguage() {
    return Language.PYTHON;
  }

  @Override
  protected String[] getRepoUrls() {
    return new String[] {
      // "https://github.com/AUTOMATIC1111/stable-diffusion-webui.git", // rank 2
      "https://github.com/Significant-Gravitas/AutoGPT.git", // rank 1
      // "https://github.com/huggingface/transformers.git",
      // "https://github.com/langflow-ai/langflow.git",
      // "https://github.com/ytdl-org/youtube-dl.git"
    };
  }

  @Override
  protected boolean isTypeEqual(String tagKind, String nodeType) {
    if (tagKind.equalsIgnoreCase("function")) {
      return PythonNodeTypes.FUNCTION.equals(nodeType);
    } if (tagKind.equalsIgnoreCase("member")) {
      return PythonNodeTypes.FUNCTION.equals(nodeType);
    }
    return false;
  }

  public static void main(String[] args) throws Exception {
    PythonValidator validator = new PythonValidator();
    validator.run();
  }

}
