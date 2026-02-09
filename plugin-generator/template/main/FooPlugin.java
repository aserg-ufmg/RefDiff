package refdiff.parsers.universal.foo;

import org.treesitter.TSLanguage;
import org.treesitter.TSTree;
import org.treesitter.TreeSitterFoo;
import org.treesitter.TSNode;
import org.treesitter.TSQuery;
import org.treesitter.TSQueryCapture;
import org.treesitter.TSQueryCursor;
import org.treesitter.TSQueryMatch;

import java.util.List;
import java.util.ArrayList;

import refdiff.core.cst.CstNode;
import refdiff.core.cst.Parameter;
import refdiff.core.cst.Stereotype;
import refdiff.parsers.universal.common.NodeUtils;
import refdiff.parsers.universal.base.BaseParser;

public class FooPlugin extends BasePlugin {
  private int cstId = 0;

  @Override
  protected TSLanguage getLanguage() {
    return new TreeSitterFoo();
  }

  @Override
  protected String[] getCallableNodeTypes() {
    return new String[] { FooNodeTypes.FUNCTION };
  }

  @Override
  protected String[] getInheritableNodeTypes() {
    return new String[] {};
  }

  @Override
  protected void buildCst(TSTree tree, TSLanguage tsLang, String path, byte[] sourceBytes) {
  }

  /**
   * Extracts parameter types from a parameters TSNode and builds a signature string.
   * e.g., "(String, int[])"
   * @param parametersNode The TSNode representing the parameters list (e.g., content of formal_parameters).
   * @param sourceBytes The source code string to extract type names.
   * @return A string representing the parameter signature.
   */
  private List<Parameter> extractSignatureParameters(TSNode parametersNode, byte[] sourceBytes) {
  }
}
