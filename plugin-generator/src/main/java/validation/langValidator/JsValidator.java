package validation.langValidator;

import plugingenerator.Language;
import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstRoot;
import refdiff.parsers.LanguagePlugin;
import refdiff.parsers.universal.js.JsPlugin;
import validation.BaseValidator;
import validation.Tag;
import refdiff.parsers.universal.js.JsNodeTypes;

public class JsValidator extends BaseValidator {

  @Override
  protected LanguagePlugin getPlugin() {
    return new JsPlugin();
  }

  @Override
  protected Language getLanguage() {
    return Language.JS;
  }

  @Override
  protected String[] getRepoUrls() {
    return new String[] {
      "https://github.com/facebook/react.git",
      // "https://github.com/axios/axios.git",
      // "https://github.com/vercel/next.js.git",
      // "https://github.com/nodejs/node.git",
      // "https://github.com/mrdoob/three.js.git",
    };
  }

  @Override
  protected boolean shouldIgnore(Tag tag, CstRoot root) {
    if (super.shouldIgnore(tag, root)) {
      return true;
    }
    if (tag.getName().contains("anonymousFunction")) {
      return true;
    }
    if (tag.getPattern().contains("prototype.constructor")) {
      return true;
    }
    if (tag.getPattern().contains("prototype = ")) {
      return true;
    }
    if (tag.getPattern().contains("prototype.setAttribute")) {
      return true;
    }
    if (tag.getPattern().contains("prototype.key")) {
      return true;
    }
    if (tag.getName().equals("type")) {
      return true;
    }
    if (tag.getPattern().contains("?")) { // ts type annotation
      return true;
    }
    if (root.isFileParseFailed(tag.getPath())) {
      return true;
    }
    if (tag.getName().equals("delete")) { // remove method definition
      return true;
    }
    if (tag.getPattern().contains("delete ") && tag.getPattern().contains(".prototype")) {
      return true;
    }
    if (tag.getPattern().contains("prototype.render = ")) {
      return true;
    }
    if (tag.getPattern().contains("prototype.focus = ")) {
      return true;
    }
    if (tag.getPattern().contains("prototype.isReactComponent = {}")) {
      return true;
    }
    if (tag.getPattern().contains("export let BabelClass = \\/*#__PURE__*\\/ (function (_React$Component)")) { // IIFE
      return true;
    }
    if (tag.getPattern().contains("export let BabelClassWithFields = \\/*#__PURE__*\\/ (function (_React$Component2)")) { // IIFE
      return true;
    }
    return false;
  }

  @Override
  protected boolean isNameEqual(Tag tag, CstNode cstNode) {
    if (tag.getKind().equals("function") && tag.getName().equals("exports") && cstNode.getSimpleName().equals("module.exports")) {
      return true;
    } else if (tag.getKind().equals("function") && tag.getName().equals("module.exports") && cstNode.getSimpleName().equals("exports")) {
      return true;
    } else if (tag.getKind().equals("method") && tag.getName().equals("js") && cstNode.getSimpleName().endsWith(".js'")) {
      return true;
    } else if (tag.getKind().equals("function") && tag.getName().equals("type") && tag.getPattern().contains(cstNode.getSimpleName())) {
      return true;
    } else if (tag.getPattern().contains("prototype") && tag.getPattern().contains(cstNode.getSimpleName())) {
      return true;
    }
    return false;
  }

  @Override
  protected boolean isTypeEqual(String tagKind, String nodeType) {
    if (tagKind.equalsIgnoreCase("file")) {
      return JsNodeTypes.FILE.equals(nodeType);
    }
    if (tagKind.equalsIgnoreCase("function")) {
      return JsNodeTypes.FUNCTION.equals(nodeType) || JsNodeTypes.CLASS.equals(nodeType);
    }
    if (tagKind.equalsIgnoreCase("class")) {
      return JsNodeTypes.CLASS.equals(nodeType) || JsNodeTypes.FUNCTION.equals(nodeType);
    }
    if (tagKind.equalsIgnoreCase("method")) {
      return JsNodeTypes.FUNCTION.equals(nodeType);
    }
    return false;
  }

  public static void main(String[] args) throws Exception {
    JsValidator validator = new JsValidator();
    validator.run();
  }

}
