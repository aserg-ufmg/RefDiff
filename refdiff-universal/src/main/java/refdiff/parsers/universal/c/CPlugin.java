package refdiff.parsers.universal.c;

import org.treesitter.TSLanguage;
import org.treesitter.TSTree;
import org.treesitter.TreeSitterC;
import org.treesitter.TSNode;
import org.treesitter.TSQuery;
import org.treesitter.TSQueryCapture;
import org.treesitter.TSQueryCursor;
import org.treesitter.TSQueryMatch;

import java.util.ArrayList;
import java.util.List;

import refdiff.core.cst.CstNode;
import refdiff.core.cst.Parameter;
import refdiff.core.io.FilePathFilter;
import refdiff.parsers.universal.base.BasePlugin;
import refdiff.parsers.universal.common.NodeUtils;
import refdiff.parsers.universal.common.FilePathUtils;

public class CPlugin extends BasePlugin {

  private int cstId = 0;

  @Override
  public FilePathFilter getAllowedFilesFilter() {
    return new FilePathFilter(List.of(".c", ".h"));
  }

  @Override
  protected TSLanguage getLanguage() {
    return new TreeSitterC();
  }

  @Override
  protected String[] getCallableNodeTypes() {
    return new String[] { CNodeTypes.FUNCTION };
  }

  @Override
  protected String[] getInheritableNodeTypes() {
    return new String[] {};
  }

  @Override
  protected void buildCst(TSTree tree, TSLanguage tsLang, String path, byte[] sourceBytes) {
    String namespace = FilePathUtils.extractDirectoryFromFilePath(path);
    String querySrc = """
        (translation_unit) @declaration

        (function_definition
          declarator: [
            (function_declarator
                declarator: (identifier) @name
                parameters: (parameter_list)? @parameters)
            (pointer_declarator
                declarator: (function_declarator
                    declarator: (identifier) @name
                    parameters: (parameter_list)? @parameters))
          ]
          body: (compound_statement)? @body
        ) @declaration

        (declaration
          declarator: [
            (function_declarator
                declarator: (identifier) @name
                parameters: (parameter_list)? @parameters)
            (pointer_declarator
                declarator: (function_declarator
                    declarator: (identifier) @name
                    parameters: (parameter_list)? @parameters))
          ]
        ) @declaration
        """;

    TSQuery tsQuery = new TSQuery(tsLang, querySrc);
    TSQueryCursor cursor = new TSQueryCursor();
    cursor.exec(tsQuery, tree.getRootNode());

    TSQueryMatch match = new TSQueryMatch();
    while (cursor.nextMatch(match)) {
        TSNode name = null;
        TSNode parameters = null;
        TSNode body = null;
        TSNode declaration = null;
        for (TSQueryCapture capture : match.getCaptures()) {
            TSNode capturedNode = capture.getNode();
            String captureName = tsQuery.getCaptureNameForId(capture.getIndex());
            switch (captureName) {
                case "name" -> name = capturedNode;
                case "parameters" -> parameters = capturedNode;
                case "body" -> body = capturedNode;
                case "declaration" -> declaration = capturedNode;
            }
        }

        CstNode cstNode = new CstNode(cstId++);
        switch (match.getPatternIndex()) {
            case 0: // File match
                cstNode.setType(CNodeTypes.FILE);
                cstNode.setSimpleName(FilePathUtils.extractFileNameFromFilePath(path));
                cstNode.setLocalName(FilePathUtils.extractFileNameFromFilePath(path));
                cstNode.setLocation(NodeUtils.generateLocation(declaration, null, path));
                break;
            case 1: // Function match
            case 2: // Function match
                cstNode.setType(CNodeTypes.FUNCTION);
                cstNode.setLocation(NodeUtils.generateLocation(declaration, body, path));
                String functionName = NodeUtils.getNodeText(name, sourceBytes);
                cstNode.setSimpleName(functionName);
                List<Parameter> params = new ArrayList<>();
                List<String> paramTypes = new ArrayList<>();
                extractParams(parameters, sourceBytes, params, paramTypes, tsLang);
                cstNode.setParameters(params);
                String localName = functionName + "(" + String.join(", ", paramTypes) + ")";
                cstNode.setLocalName(localName);
                break;
            default:
                System.err.println("Warning: Unhandled declaration type: " + declaration.getType() + " at " + declaration.getStartPoint().getRow() + "-" + declaration.getEndPoint().getRow() + " in source code: " + NodeUtils.getNodeText(declaration, sourceBytes));
                continue;
        }
        addNodeToParent(cstNode, namespace);
    }
  }

  private void extractParams(TSNode paramsNode, byte[] sourceBytes, List<Parameter> cstParameters, List<String> paramTypes, TSLanguage tsLang) {
    if (paramsNode == null) {
        return;
    }

    String paramQuerySrc = """
        [
            (parameter_declaration
                type: (_) @param_type
                declarator: (_) @param_declarator
            )
            (variadic_parameter) @variadic
        ]
        """;
    
    TSQuery paramQuery = new TSQuery(tsLang, paramQuerySrc);
    TSQueryCursor paramCursor = new TSQueryCursor();
    paramCursor.exec(paramQuery, paramsNode);
    TSQueryMatch paramMatch = new TSQueryMatch();

    while (paramCursor.nextMatch(paramMatch)) {
        TSNode typeNode = null;
        TSNode declaratorNode = null;
        boolean isVariadic = false;

        for (TSQueryCapture capture : paramMatch.getCaptures()) {
            String captureName = paramQuery.getCaptureNameForId(capture.getIndex());
            switch (captureName) {
                case "param_type": typeNode = capture.getNode(); break;
                case "param_declarator": declaratorNode = capture.getNode(); break;
                case "variadic": isVariadic = true; break;
            }
        }

        if (isVariadic) {
            paramTypes.add("...");
            cstParameters.add(new Parameter("..."));
        } else if (typeNode != null) {
            String typeStr = NodeUtils.getNodeText(typeNode, sourceBytes);
            if (typeStr.startsWith("struct ")) {
                typeStr = typeStr.substring(7);
            }
            if (typeStr.equals("void") && paramsNode.getNamedChildCount() == 1) {
                continue;
            }
            paramTypes.add(typeStr);

            String paramName = null;
            if (declaratorNode != null) {
                paramName = NodeUtils.getNodeText(declaratorNode, sourceBytes);
            }
            
            if (paramName != null) {
                cstParameters.add(new Parameter(paramName));
            } else {
                cstParameters.add(new Parameter(""));
            }
        }
    }
  }


}