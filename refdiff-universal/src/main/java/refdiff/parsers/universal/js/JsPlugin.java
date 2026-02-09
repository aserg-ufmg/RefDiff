package refdiff.parsers.universal.js;

import java.util.ArrayList;
import java.util.List;

import org.treesitter.TSLanguage;
import org.treesitter.TSNode;
import org.treesitter.TSQuery;
import org.treesitter.TSQueryCapture;
import org.treesitter.TSQueryCursor;
import org.treesitter.TSQueryMatch;
import org.treesitter.TSTree;
import org.treesitter.TreeSitterJavascript;

import refdiff.core.cst.CstNode;
import refdiff.core.cst.Parameter;
import refdiff.core.io.FilePathFilter;
import refdiff.parsers.universal.base.BasePlugin;
import refdiff.parsers.universal.common.FilePathUtils;
import refdiff.parsers.universal.common.NodeUtils;

public class JsPlugin extends BasePlugin {

  private int cstId = 0;

  @Override
  public FilePathFilter getAllowedFilesFilter() {
    return new FilePathFilter(List.of(".js", ".jsx"), List.of(".min.js"));
  }

  @Override
  protected TSLanguage getLanguage() {
    return new TreeSitterJavascript();
  }

  @Override
  protected String[] getCallableNodeTypes() {
    return new String[] { JsNodeTypes.FUNCTION };
  }

  @Override
  protected String[] getInheritableNodeTypes() {
    return new String[] { JsNodeTypes.CLASS };
  }

  @Override
  protected void buildCst(TSTree tree, TSLanguage tsLang, String path, byte[] sourceBytes) {
    String namespace = FilePathUtils.extractDirectoryFromFilePath(path);
    String querySrc = """
      [
        (program) @file_declaration

        (class
          name: (identifier) @name
          body: (class_body) @body) @class_declaration
        (class_declaration
          name: (identifier) @name
          body: (class_body) @body) @class_declaration
        (assignment_expression
            left: (member_expression property: (property_identifier) @name)
            right: (class (class_body) @body)) @class_declaration
        (assignment_expression
            left: (identifier) @name
            right: (class (class_body) @body)) @class_declaration
        (variable_declarator
          name: (identifier) @name
          value: (class (class_body) @body)) @class_declaration

        (field_definition
          property: (property_identifier) @name
          value: (function_expression
            parameters: (formal_parameters) @parameters
            body: (_) @body)) @function_declaration
        (field_definition
            property: (property_identifier) @name
            value: (arrow_function
              parameters: (formal_parameters) @parameters
              body: (_) @body)) @function_declaration
        (field_definition
            property: (property_identifier) @name
            value: (arrow_function
              parameter: (identifier) @parameters
              body: (_) @body)) @function_declaration

        (function_declaration
          name: (identifier) @name
          parameters: (formal_parameters) @parameters
          body: (statement_block) @body) @function_declaration
        (generator_function_declaration
          name: (identifier) @name
          parameters: (formal_parameters) @parameters
          body: (statement_block) @body) @function_declaration
        (method_definition
          name: (_) @name
          parameters: (_) @parameters
          body: (_) @body) @function_declaration
        (function_expression
          name: (identifier) @name
          parameters: (formal_parameters) @parameters
          body: (_) @body) @function_declaration

        (variable_declarator
          name: (identifier) @name
          value: (function_expression
            parameters: (formal_parameters) @parameters
            body: (_) @body)) @function_declaration
        (variable_declarator
          name: (identifier) @name
          value: (arrow_function
            parameters: (formal_parameters) @parameters
            body: (_) @body)) @function_declaration
        (variable_declarator
          name: (identifier) @name
          value: (arrow_function
            parameter: (identifier) @parameters
            body: (_) @body)) @function_declaration
          
        (pair
          key: (property_identifier) @name
          value: (function_expression
            parameters: (formal_parameters) @parameters
            body: (_) @body)) @function_declaration
        (pair
          key: (property_identifier) @name
          value: (arrow_function
            parameters: (formal_parameters) @parameters
            body: (_) @body)) @function_declaration
        (pair
            key: (property_identifier) @name
            value: (arrow_function
              parameter: (identifier) @parameters
              body: (_) @body)) @function_declaration
        (pair
          key: (string) @name
          value: (function_expression
            parameters: (formal_parameters) @parameters
            body: (_) @body)) @function_declaration
        (pair
          key: (string) @name
          value: (arrow_function
            parameters: (formal_parameters) @parameters
            body: (_) @body)) @function_declaration
        (pair
            key: (string) @name
            value: (arrow_function
              parameter: (identifier) @parameters
              body: (_) @body)) @function_declaration
        (pair
            key: (string (string_fragment) @name)
            value: (function_expression
              parameters: (formal_parameters) @parameters
              body: (_) @body)) @function_declaration
        (pair
            key: (string (string_fragment) @name)
            value: (arrow_function
              parameters: (formal_parameters) @parameters
              body: (_) @body)) @function_declaration
        (pair
            key: (string (string_fragment) @name)
            value: (arrow_function
              parameter: (identifier) @parameters
              body: (_) @body)) @function_declaration

          (assignment_expression
            left: (identifier) @name
            right: (function_expression
              parameters: (formal_parameters) @parameters
              body: (_) @body)) @function_declaration
          (assignment_expression
            left: (identifier) @name
            right: (arrow_function
              parameters: (formal_parameters) @parameters
              body: (_) @body)) @function_declaration
          (assignment_expression
            left: (identifier) @name
            right: (arrow_function
              parameter: (identifier) @parameters
              body: (_) @body)) @function_declaration

        (assignment_expression
          left: (member_expression (property_identifier) @name)
          right: (function_expression
            parameters: (formal_parameters) @parameters
            body: (_) @body)) @function_declaration
        (assignment_expression
          left: (member_expression (property_identifier) @name)
          right: (arrow_function
            parameters: (formal_parameters) @parameters
            body: (_) @body)) @function_declaration
        (assignment_expression
          left: (member_expression (property_identifier) @name)
          right: (arrow_function
            parameter: (identifier) @parameters
            body: (_) @body)) @function_declaration
        
        (assignment_expression
          left: (member_expression (identifier) @name)
          right: (function_expression
            parameters: (formal_parameters) @parameters
            body: (_) @body)) @function_declaration      

        (assignment_expression
          left: (subscript_expression (identifier) @name)
          right: (function_expression
            parameters: (formal_parameters) @parameters
            body: (_) @body)) @function_declaration
        (assignment_expression
          left: (subscript_expression (identifier) @name)
          right: (arrow_function
            parameters: (formal_parameters) @parameters
            body: (_) @body)) @function_declaration
        (assignment_expression
          left: (subscript_expression (identifier) @name)
          right: (arrow_function
            parameter: (identifier) @parameters
            body: (_) @body)) @function_declaration 
        
        (assignment_expression
          left: (subscript_expression (member_expression (identifier) @name))
          right: (function_expression
            parameters: (formal_parameters) @parameters
            body: (_) @body)) @function_declaration
        (assignment_expression
          left: (subscript_expression (member_expression (identifier) @name))
          right: (arrow_function
            parameters: (formal_parameters) @parameters
            body: (_) @body)) @function_declaration
        (assignment_expression
          left: (subscript_expression (member_expression (identifier) @name))
          right: (arrow_function
            parameter: (identifier) @parameters
            body: (_) @body)) @function_declaration

        (object_assignment_pattern
          left: (shorthand_property_identifier_pattern) @name
          right: (arrow_function
            parameters: (formal_parameters) @parameters
            body: (_) @body)) @function_declaration
        (object_assignment_pattern
            left: (shorthand_property_identifier_pattern) @name
            right: (arrow_function
              parameter: (identifier) @parameters
              body: (_) @body)) @function_declaration
      ]""";

    TSQuery tsQuery = new TSQuery(tsLang, querySrc);
    TSQueryCursor cursor = new TSQueryCursor();
    cursor.exec(tsQuery, tree.getRootNode());

    TSQueryMatch match = new TSQueryMatch();
    while (cursor.nextMatch(match)) {
      TSNode name = null;
      TSNode parameters = null;
      TSNode body = null;
      TSNode declaration = null;
      String nodeType = null;

      for (TSQueryCapture capture : match.getCaptures()) {
        TSNode capturedNode = capture.getNode();
        String captureName = tsQuery.getCaptureNameForId(capture.getIndex());
        switch (captureName) {
          case "name" -> name = capturedNode;
          case "parameters" -> parameters = capturedNode;
          case "body" -> body = capturedNode;

          case "file_declaration" -> {
            declaration = capturedNode;
            nodeType = JsNodeTypes.FILE;
          }
          case "class_declaration" -> {
            declaration = capturedNode;
            nodeType = JsNodeTypes.CLASS;
          }
          case "function_declaration" -> {
            declaration = capturedNode;
            nodeType = JsNodeTypes.FUNCTION;
          }
        }
      }

      CstNode cstNode = new CstNode(cstId++);
      switch (nodeType) {
        case JsNodeTypes.FILE -> {
          cstNode.setType(JsNodeTypes.FILE);
          cstNode.setSimpleName(FilePathUtils.extractFileNameFromFilePath(path));
          cstNode.setLocalName(FilePathUtils.extractFileNameFromFilePath(path));
          cstNode.setLocation(NodeUtils.generateLocation(declaration, null, path));
        }
        case JsNodeTypes.CLASS -> {
          cstNode.setType(JsNodeTypes.CLASS);
          cstNode.setLocation(NodeUtils.generateLocation(declaration, body, path));
          String className = NodeUtils.getNodeText(name, sourceBytes);
          cstNode.setSimpleName(className);
          cstNode.setLocalName(className);
        }
        case JsNodeTypes.FUNCTION -> {
          cstNode.setType(JsNodeTypes.FUNCTION);
          cstNode.setLocation(NodeUtils.generateLocation(declaration, body, path));
          String funcName = NodeUtils.getNodeText(name, sourceBytes);
          if (funcName == null || funcName.isEmpty()) {
            funcName = "anonymousFunction";
          }
          cstNode.setSimpleName(funcName);
          
          List<Parameter> cstParameters = new ArrayList<>();
          if (parameters != null) {
            cstParameters = extractParameters(parameters, sourceBytes, tsLang);
          }
          cstNode.setParameters(cstParameters);
          cstNode.setLocalName(funcName + NodeUtils.generateParams(cstParameters));
        }
      }
      addNodeToParent(cstNode, namespace);
    }
  }

  private List<Parameter> extractParameters(TSNode parametersHostNode, byte[] sourceBytes, TSLanguage tsLang) {
    List<Parameter> cstParameters = new ArrayList<>();
    if (parametersHostNode == null) {
        return cstParameters;
    }

    String hostNodeType = parametersHostNode.getType();
    if ("identifier".equals(hostNodeType)) { // Single parameter for arrow function: param => ...
        cstParameters.add(new Parameter(NodeUtils.getNodeText(parametersHostNode, sourceBytes)));
        return cstParameters;
    }

    if (!"formal_parameters".equals(hostNodeType)) {
        return cstParameters;
    }

    for (int i = 0; i < parametersHostNode.getNamedChildCount(); i++) {
        TSNode paramChildNode = parametersHostNode.getNamedChild(i);
        String paramName = queryParameterName(paramChildNode, sourceBytes, tsLang);
        if (paramName != null) {
            cstParameters.add(new Parameter(paramName));
        }
    }
    return cstParameters;
  }

  private String queryParameterName(TSNode paramNode, byte[] sourceBytes, TSLanguage tsLang) {
    String nodeType = paramNode.getType();
    String querySrc;
    String nameCapture = "name";
    boolean isRest = false;

    switch (nodeType) {
        case "identifier":
            return NodeUtils.getNodeText(paramNode, sourceBytes);
        case "assignment_pattern":
            querySrc = "(assignment_pattern left: (identifier) @name)";
            break;
        case "rest_pattern":
            querySrc = "(rest_pattern (identifier) @name)";
            isRest = true;
            break;
        default:
            return null;
    }

    TSQuery query = new TSQuery(tsLang, querySrc);
    TSQueryCursor cursor = new TSQueryCursor();
    cursor.exec(query, paramNode);
    TSQueryMatch match = new TSQueryMatch();

    if (cursor.nextMatch(match)) {
        for (TSQueryCapture capture : match.getCaptures()) {
            if (nameCapture.equals(query.getCaptureNameForId(capture.getIndex()))) {
                TSNode nameNode = capture.getNode();
                String name = NodeUtils.getNodeText(nameNode, sourceBytes);
                return isRest ? "..." + name : name;
            }
        }
    }

    return null;
  }
}
