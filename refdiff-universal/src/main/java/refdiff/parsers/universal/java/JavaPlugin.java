package refdiff.parsers.universal.java;

import org.treesitter.TSLanguage;
import org.treesitter.TSTree;
import org.treesitter.TreeSitterJava;
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

public class JavaPlugin extends BasePlugin {

  private int cstId = 0;

  @Override
  public FilePathFilter getAllowedFilesFilter() {
    return new FilePathFilter(List.of(".java"));
  }

  @Override
  protected TSLanguage getLanguage() {
    return new TreeSitterJava();
  }

  @Override
  protected String[] getCallableNodeTypes() {
    return new String[] { JavaNodeTypes.METHOD };
  }

  @Override
  protected String[] getInheritableNodeTypes() {
    return new String[] { JavaNodeTypes.CLASS, JavaNodeTypes.INTERFACE };
  }

  @Override
  protected void buildCst(TSTree tree, TSLanguage tsLang, String path, byte[] sourceBytes) {
    String namespace = extractPackageName(tree, tsLang, sourceBytes) + ".";
    String querySrc = """
    [
      (class_declaration
        name: (identifier) @name
        body: (class_body) @body
      ) @declaration
      (interface_declaration
          name: (identifier) @name
          body: (interface_body) @body
      ) @declaration
      (enum_declaration
          name: (identifier) @name
          body: (enum_body) @body
      ) @declaration
      (constructor_declaration
          name: (identifier) @name
          parameters: (formal_parameters) @parameters
          body: (constructor_body) @body
      ) @declaration
      (method_declaration
          name: (identifier) @name
          parameters: (formal_parameters) @parameters
          body: ((block) @body)?
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

      CstNode cstNode = new CstNode(cstId++);
      cstNode.setLocation(NodeUtils.generateLocation(declaration, body, path));
      cstNode.setSimpleName(NodeUtils.getNodeText(name, sourceBytes));
      cstNode.setLocalName(NodeUtils.getNodeText(name, sourceBytes));
      switch (declaration.getType()) {
        case "class_declaration":
          cstNode.setType(JavaNodeTypes.CLASS);
          break;
        case "interface_declaration":
          cstNode.setType(JavaNodeTypes.INTERFACE);
          cstNode.addStereotypes(Stereotype.ABSTRACT);
          break;
        case "enum_declaration":
          cstNode.setType(JavaNodeTypes.ENUM);
          break;
        case "constructor_declaration":
          cstNode.setType(JavaNodeTypes.METHOD);
          String constructorName = "new";
          cstNode.setSimpleName(constructorName);
          List<Parameter> constructor_params = extractSignatureParameters(parameters, sourceBytes);
          cstNode.setParameters(constructor_params);
          cstNode.setLocalName(constructorName + NodeUtils.generateParams(constructor_params));
          cstNode.addStereotypes(Stereotype.TYPE_CONSTRUCTOR);
          break;
        case "method_declaration":
          cstNode.setType(JavaNodeTypes.METHOD);
          try {
            List<Parameter> method_params = extractSignatureParameters(parameters, sourceBytes);
            cstNode.setParameters(method_params);
            cstNode.setLocalName(NodeUtils.getNodeText(name, sourceBytes) + NodeUtils.generateParams(method_params));
          } catch (RuntimeException e) {
            System.out.println("Error extracting method parameters: " + path + ": " + e.getMessage());
            cstNode.setLocalName(NodeUtils.getNodeText(name, sourceBytes) + "()");
          }
          cstNode.addStereotypes(Stereotype.TYPE_MEMBER);
          break;
        default:
          System.out.println("Warning: Unhandled declaration type: " + declaration.getType() + " at " + path + ":" + declaration.getStartPoint().getRow() + "-" + declaration.getEndPoint().getRow() + " in source code: " + NodeUtils.getNodeText(declaration, sourceBytes));
          break;
      }
      addNodeToParent(cstNode, namespace);
    }
  }

  /**
   * Extracts parameter types from a parameters TSNode and builds a signature string.
   * e.g., "(String, int[])"
   * @param parametersNode The TSNode representing the parameters list (e.g., content of formal_parameters).
   * @param sourceBytes The source code string to extract type names.
   * @return A string representing the parameter signature.
   */
  private List<Parameter> extractSignatureParameters(TSNode parametersNode, byte[] sourceBytes) throws RuntimeException {
    List<Parameter> parameters = new ArrayList<>();
    for (int i = 0; i < parametersNode.getNamedChildCount(); i++) {
        TSNode parameter = parametersNode.getNamedChild(i);
        String paramTypeString = null;

        if (parameter.getType().equals("formal_parameter")) {
            TSNode typeNode = parameter.getChildByFieldName("type");
            if (typeNode != null && !typeNode.isNull()) {
                paramTypeString = NodeUtils.getNodeText(typeNode, sourceBytes).split("<")[0]; // Remove generic type parameters if any, e.g., List<String> -> List
            }
        } else if (parameter.getType().equals("spread_parameter")) {
            TSNode typeNode = parameter.getChild(0); // The first child is the type for spread parameters
            if (typeNode != null && !typeNode.isNull()) {
                paramTypeString = NodeUtils.getNodeText(typeNode, sourceBytes).split("<")[0] + "..."; // For spread parameters, the type is followed by "..."
            }
        } else if (parameter.getType().equals("receiver_parameter")) {
            // Receiver parameters (e.g., `Outer.this`) are generally not included in RefDiff's localName.
            // If they need to be included, this part can be adjusted.
            continue; // Skipping receiver parameters for localName.
        } else if (parameter.getType().equals("block_comment") || parameter.getType().equals("line_comment")) {
            continue; // Skip comments within parameters
        } else if (parameter.getType().equals("ERROR")) {
            continue; // Skip error nodes
        }
        if (paramTypeString != null) {
          parameters.add(new Parameter(paramTypeString));
        } else {
          throw new RuntimeException("Could not determine type for parameter: " + parameter.getType() +
                " at " + parameter.getStartPoint().getRow() + "-" + parameter.getEndPoint().getRow() +
                " in source code: " + NodeUtils.getNodeText(parametersNode, sourceBytes));
        }
    }
    return parameters;
  }

  private String extractPackageName(TSTree tree, TSLanguage tsLang, byte[] sourceBytes) {
    String packageQuerySrc = "(package_declaration  [(identifier) (scoped_identifier)] @package_name)";
    TSQuery packageQuery = new TSQuery(tsLang, packageQuerySrc);
    TSQueryCursor packageCursor = new TSQueryCursor();
    packageCursor.exec(packageQuery, tree.getRootNode());
    String packageName = "";
    TSQueryMatch packageMatch = new TSQueryMatch();
    if (packageCursor.nextMatch(packageMatch)) {
      for (TSQueryCapture capture : packageMatch.getCaptures()) {
        TSNode capturedNode = capture.getNode();
        packageName = NodeUtils.getNodeText(capturedNode, sourceBytes);

        break;
      }
    }
    return packageName;
  }
}