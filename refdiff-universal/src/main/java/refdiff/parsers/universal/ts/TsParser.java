package refdiff.parsers.universal.ts;

import org.treesitter.TSLanguage;
import org.treesitter.TSTree;
import org.treesitter.TreeSitterTypescript;
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

public class TsParser extends BasePlugin {
  private int cstId = 0;

  @Override
  public FilePathFilter getAllowedFilesFilter() {
    return new FilePathFilter(List.of(".ts", ".tsx"));
  }

  @Override
  protected TSLanguage getLanguage() {
    return new TreeSitterTypescript();
  }

  @Override
  protected String[] getCallableNodeTypes() {
    return new String[] { TsNodeTypes.FUNCTION.name() };
  }

  @Override
  protected String[] getInheritableNodeTypes() {
    return new String[] {};
  }

  @Override
  protected void buildCst(TSTree tree, TSLanguage tsLang, String path, byte[] sourceBytes) {
    String namespace = FilePathUtils.extractDirectoryFromFilePath(path);
    String querySrc = """
      [
        (class_declaration
          name: (type_identifier) @name
          body: (class_body) @body
        ) @declaration
      
        (interface_declaration
          name: (type_identifier) @name
          body: (interface_body) @body
        ) @declaration
    
        (method_definition
          name: (property_identifier) @name (#eq? @name "constructor")
          parameters: (formal_parameters) @parameters
          body: (statement_block)? @body
        ) @declaration

        (method_definition
          name: (property_identifier) @name (#not-eq? @name "constructor")
          parameters: (formal_parameters) @parameters
          body: (statement_block)? @body
        ) @declaration

        (method_signature
          name: (property_identifier) @name
          parameters: (formal_parameters) @parameters
        ) @declaration

        (function_declaration
          name: (identifier) @name
          parameters: (formal_parameters) @parameters
          body: (statement_block)? @body
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
        case "constructor_declaration":
          cstNode.setType(TsNodeTypes.FUNCTION.name());
          String constructorName = "new";
          cstNode.setSimpleName(constructorName);
          // List<Parameter> constructor_params = extractSignatureParameters(parameters, sourceBytes);
          // cstNode.setParameters(constructor_params);
          // cstNode.setLocalName(constructorName + NodeUtils.generateParams(constructor_params));
          cstNode.addStereotypes(Stereotype.TYPE_CONSTRUCTOR);
          break;
        case "method_declaration":
          cstNode.setType(TsNodeTypes.FUNCTION.name());
          // List<Parameter> method_params = extractSignatureParameters(parameters, sourceBytes);
          // cstNode.setParameters(method_params);
          // cstNode.setLocalName(NodeUtils.getNodeText(name, sourceBytes) + NodeUtils.generateParams(method_params));
          cstNode.addStereotypes(Stereotype.TYPE_MEMBER);
          break;
        default:
          cstNode.setType(TsNodeTypes.FUNCTION.name());
          // List<Parameter> func_params = extractSignatureParameters(parameters, sourceBytes);
          // cstNode.setParameters(func_params);
          // cstNode.setLocalName(NodeUtils.getNodeText(name, sourceBytes) + NodeUtils.generateParams(func_params));
          cstNode.addStereotypes(Stereotype.TYPE_MEMBER);
          break;
        // default:
        //   System.out.println("Warning: Unhandled declaration type: " + declaration.getType() + " at "
        //       + declaration.getStartPoint().getRow() + "-" + declaration.getEndPoint().getRow() + " in source code: "
        //       + NodeUtils.getNodeText(declaration, sourceBytes));
        //   break;
      }
      addNodeToParent(cstNode, namespace);
    }
  }

  /**
   * Extracts parameter types from a parameters TSNode and builds a signature
   * string.
   * e.g., "(String, int[])"
   * 
   * @param parametersNode The TSNode representing the parameters list (e.g.,
   *                       content of formal_parameters).
   * @param sourceBytes    The source code string to extract type names.
   * @return A string representing the parameter signature.
   */
  // private List<Parameter> extractSignatureParameters(TSNode parametersNode, byte[] sourceBytes) {
    
  // }
}
