package refdiff.parsers.universal.php;

import org.treesitter.TSLanguage;
import org.treesitter.TSTree;
import org.treesitter.TreeSitterPhp;
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
import refdiff.core.io.FilePathFilter;
import refdiff.parsers.universal.common.FilePathUtils;
import refdiff.parsers.universal.common.NodeUtils;
import refdiff.parsers.universal.base.BasePlugin;

public class PhpPlugin extends BasePlugin {

  private int cstId = 0;

  public FilePathFilter getAllowedFilesFilter() {
    return new FilePathFilter(List.of(".php"));
  }

  @Override
  protected TSLanguage getLanguage() {
    return new TreeSitterPhp();
  }

  @Override
  protected String[] getCallableNodeTypes() {
    return new String[]{PhpNodeTypes.METHOD, PhpNodeTypes.FUNCTION};
  }

  @Override
  protected String[] getInheritableNodeTypes() {
    return new String[]{PhpNodeTypes.CLASS, PhpNodeTypes.INTERFACE};
  }

  @Override
  protected void buildCst(TSTree tree, TSLanguage tsLang, String path, byte[] sourceBytes) {
    String namespace = FilePathUtils.extractDirectoryFromFilePath(path);
    String querySrc = """
    [
      (class_declaration
        name: (name) @name
        body: (declaration_list) @body
      ) @declaration
      (interface_declaration
          name: (name) @name
          body: (declaration_list) @body
      ) @declaration
      (trait_declaration
          name: (name) @name
          body: (declaration_list) @body
      ) @declaration
      (method_declaration
          name: (name) @name
          parameters: (formal_parameters)? @parameters
          body: (compound_statement)? @body
      ) @declaration
      (function_definition
          name: (name) @name
          parameters: (formal_parameters)? @parameters
          body: (compound_statement) @body
      ) @declaration
    ]
    """;
    TSQuery query = new TSQuery(tsLang, querySrc);
    TSQueryCursor cursor = new TSQueryCursor();
    cursor.exec(query, tree.getRootNode());
    TSQueryMatch match = new TSQueryMatch();

    while (cursor.nextMatch(match)) {
        TSNode name = null;
        TSNode parameters = null;
        TSNode body = null;
        TSNode declaration = null;

        for (TSQueryCapture capture : match.getCaptures()) {
            TSNode capturedNode = capture.getNode();
            String captureName = query.getCaptureNameForId(capture.getIndex());
            switch (captureName) {
                case "name" -> name = capturedNode;
                case "parameters" -> parameters = capturedNode;
                case "body" -> body = capturedNode;
                case "declaration" -> declaration = capturedNode;
            }
        }

        if (declaration == null || name == null) {
            continue;
        }

        CstNode cstNode = new CstNode(cstId++);
        cstNode.setLocation(NodeUtils.generateLocation(declaration, body, path));
        cstNode.setSimpleName(NodeUtils.getNodeText(name, sourceBytes));

        switch (declaration.getType()) {
            case "class_declaration":
                cstNode.setType(PhpNodeTypes.CLASS);
                cstNode.setLocalName(NodeUtils.getNodeText(name, sourceBytes));
                break;
            case "interface_declaration":
                cstNode.setType(PhpNodeTypes.INTERFACE);
                cstNode.setLocalName(NodeUtils.getNodeText(name, sourceBytes));
                break;
            case "trait_declaration":
                cstNode.setType(PhpNodeTypes.TRAIT);
                cstNode.setLocalName(NodeUtils.getNodeText(name, sourceBytes));
                break;
            case "method_declaration":
                cstNode.setType(PhpNodeTypes.METHOD);
                List<Parameter> method_params = extractSignatureParameters(parameters, sourceBytes);
                cstNode.setParameters(method_params);
                cstNode.setLocalName(NodeUtils.getNodeText(name, sourceBytes) + NodeUtils.generateParams(method_params));
                break;
            case "function_definition":
                cstNode.setType(PhpNodeTypes.FUNCTION);
                List<Parameter> func_params = extractSignatureParameters(parameters, sourceBytes);
                cstNode.setParameters(func_params);
                cstNode.setLocalName(NodeUtils.getNodeText(name, sourceBytes) + NodeUtils.generateParams(func_params));
                break;
            default:
                System.out.println("Warning: Unhandled declaration type: " + declaration.getType());
                continue;
        }
        addNodeToParent(cstNode, namespace);
    }
  }

  private List<Parameter> extractSignatureParameters(TSNode parametersNode, byte[] sourceBytes) {
    List<Parameter> parameters = new ArrayList<>();
    if (parametersNode == null) {
        return parameters;
    }

    for (int i = 0; i < parametersNode.getNamedChildCount(); i++) {
        TSNode paramNode = parametersNode.getNamedChild(i);
        String type = paramNode.getType();
        String prefix = "";
        TSNode nameNode = paramNode.getChildByFieldName("name");

        if (nameNode == null) {
            // fallback: try to find a descendant variable_name
            for (int j = 0; j < paramNode.getNamedChildCount() && nameNode == null; j++) {
                TSNode child = paramNode.getNamedChild(j);
                if ("variable_name".equals(child.getType())) {
                    nameNode = child;
                } else if ("name".equals(child.getType())) {
                    // some AST variants wrap variable_name inside a name node
                    nameNode = child;
                }
            }
        }

        if (nameNode != null) {
            // inspect raw text before the name to detect '...' or '&'
            int preStart = paramNode.getStartByte();
            int preEnd = nameNode.getStartByte();
            if (preEnd > preStart) {
                String preText = new String(sourceBytes, preStart, preEnd - preStart, java.nio.charset.StandardCharsets.UTF_8);
                if (preText.contains("...")) {
                    prefix = "...";
                } else if (preText.contains("&")) {
                    prefix = "&";
                }
            }

            // for explicit variadic node types, ensure '...' prefix
            if ("variadic_parameter".equals(type)) {
                prefix = "...";
            }

            // If nameNode itself isn't the variable_name (e.g. it's a wrapper), try to find descendant variable_name
            TSNode finalNameNode = nameNode;
            if (!"variable_name".equals(nameNode.getType())) {
                for (int j = 0; j < nameNode.getNamedChildCount(); j++) {
                    TSNode child = nameNode.getNamedChild(j);
                    if ("variable_name".equals(child.getType())) {
                        finalNameNode = child;
                        break;
                    }
                }
            }

            if ("variable_name".equals(finalNameNode.getType())) {
                String name = NodeUtils.getNodeText(finalNameNode, sourceBytes);
                parameters.add(new Parameter(prefix + name));
            }
        }
    }
    return parameters;
  }
}
