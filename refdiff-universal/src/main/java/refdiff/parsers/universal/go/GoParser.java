package refdiff.parsers.universal.go;

import java.util.ArrayList;
import java.util.List;
import org.treesitter.TSLanguage;
import org.treesitter.TSNode;
import org.treesitter.TSQuery;
import org.treesitter.TSQueryCapture;
import org.treesitter.TSQueryCursor;
import org.treesitter.TSQueryMatch;
import org.treesitter.TSTree;
import org.treesitter.TreeSitterGo;
import refdiff.core.cst.CstNode;
import refdiff.core.cst.Parameter;
import refdiff.core.io.FilePathFilter;
import refdiff.parsers.universal.base.BasePlugin;
import refdiff.parsers.universal.common.NodeUtils;

public class GoParser extends BasePlugin {
  private int cstId = 0;

  @Override
  public FilePathFilter getAllowedFilesFilter() {
    return new FilePathFilter(List.of(".go"));
  }

  @Override
  protected TSLanguage getLanguage() {
    return new TreeSitterGo();
  }

  @Override
  protected String[] getCallableNodeTypes() {
    return new String[] { GoNodeTypes.METHOD, GoNodeTypes.FUNCTION };
  }

  @Override
  protected String[] getInheritableNodeTypes() {
    return new String[] {};
  }

  @Override
  protected void buildCst(TSTree tree, TSLanguage tsLang, String filePath, byte[] sourceCode) {
    String namespace = extractNamespace(tree, tsLang, sourceCode) + ".";

    String querySrc = """
    [
        (function_declaration
            name: (identifier) @name
            parameters: (parameter_list) @params
            body: (block) @body) @function
        (method_declaration
            receiver: (parameter_list) @receiver
            name: (field_identifier) @name
            parameters: (parameter_list) @params
            body: (block) @body) @method
        (function_declaration
            name: (identifier) @name
            parameters: (parameter_list) @params
            result: (_) @result
            body: (block) @body) @function
        (method_declaration
            receiver: (parameter_list) @receiver
            name: (field_identifier) @name
            parameters: (parameter_list) @params
            result: (_) @result
            body: (block) @body) @method
    ]
    """;

    TSQuery query = new TSQuery(tsLang, querySrc);
    TSQueryCursor cursor = new TSQueryCursor();
    cursor.exec(query, tree.getRootNode());

    TSQueryMatch match = new TSQueryMatch();
    while (cursor.nextMatch(match)) {
      TSNode nameNode = null;
      TSNode paramsNode = null;
      TSNode bodyNode = null;
      TSNode declaration = null;
      String nodeType = null;

      for (TSQueryCapture capture : match.getCaptures()) {
        TSNode capturedNode = capture.getNode();
        String captureName = query.getCaptureNameForId(capture.getIndex());

        switch (captureName) {
            case "function":
                declaration = capturedNode;
                nodeType = GoNodeTypes.FUNCTION;
            break;
            case "method":
                declaration = capturedNode;
                nodeType = GoNodeTypes.METHOD;
            break;
            case "name":
                nameNode = capturedNode;
                break;
            case "params":
                paramsNode = capturedNode;
                break;
            case "body":
                bodyNode = capturedNode;
                break;
        }
      }

      if (declaration != null && nameNode != null && bodyNode != null) {
        CstNode cstNode = new CstNode(cstId++);
        cstNode.setType(nodeType);
        
        String simpleName = NodeUtils.getNodeText(nameNode, sourceCode);
        cstNode.setSimpleName(simpleName);
        cstNode.setNamespace(namespace + ".");
        
        cstNode.setLocation(NodeUtils.generateLocation(declaration, bodyNode, filePath));

        String paramsString = "()";
        if (paramsNode != null) {
          paramsString = NodeUtils.getNodeText(paramsNode, sourceCode);
        }
        cstNode.setLocalName(simpleName + paramsString);
        cstNode.setParameters(extractParameters(paramsNode, sourceCode));

        addNodeToParent(cstNode, namespace);
      }
    }
  }

  private String extractNamespace(TSTree tree, TSLanguage tsLang, byte[] sourceCode) {
    String querySrc = "(package_clause (package_identifier) @package_name)";
    TSQuery query = new TSQuery(tsLang, querySrc);
    TSQueryCursor cursor = new TSQueryCursor();
    cursor.exec(query, tree.getRootNode());

    TSQueryMatch match = new TSQueryMatch();
    if (cursor.nextMatch(match)) {
      for (TSQueryCapture capture : match.getCaptures()) {
        TSNode capturedNode = capture.getNode();
        return NodeUtils.getNodeText(capturedNode, sourceCode);
      }
    }
    return "";
  }

  private List<Parameter> extractParameters(TSNode paramsNode, byte[] sourceCode) {
    List<Parameter> parameters = new ArrayList<>();
    if (paramsNode == null) {
      return parameters;
    }

    for (int i = 0; i < paramsNode.getNamedChildCount(); i++) {
      TSNode paramDecl = paramsNode.getNamedChild(i);
      String nodeType = paramDecl.getType();

      if (nodeType.equals("parameter_declaration")) {
        int namedChildCount = paramDecl.getNamedChildCount();
        if (namedChildCount > 0) {
          for (int j = 0; j < namedChildCount - 1; j++) {
            TSNode nameNode = paramDecl.getNamedChild(j);
            if (nameNode.getType().equals("identifier")) {
              String paramName = NodeUtils.getNodeText(nameNode, sourceCode);
              parameters.add(new Parameter(paramName));
            }
          }
        }
      } else if (nodeType.equals("variadic_parameter_declaration")) {
        if (paramDecl.getNamedChildCount() > 0) {
          TSNode nameNode = paramDecl.getNamedChild(0);
          String paramName = NodeUtils.getNodeText(nameNode, sourceCode);
          parameters.add(new Parameter(paramName));
        }
      }
    }
    return parameters;
  }
}