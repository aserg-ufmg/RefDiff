package refdiff.parsers.universal.ruby;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;

import org.treesitter.TSLanguage;
import org.treesitter.TSNode;
import org.treesitter.TSParser;
import org.treesitter.TSQuery;
import org.treesitter.TSQueryCapture;
import org.treesitter.TSQueryCursor;
import org.treesitter.TSQueryMatch;
import org.treesitter.TSTree;
import org.treesitter.TreeSitterRuby;

import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstRoot;
import refdiff.core.cst.Location;
import refdiff.core.cst.Parameter;
import refdiff.core.cst.TokenizedSource;
import refdiff.core.io.FilePathFilter;
import refdiff.core.io.SourceFileSet;
import refdiff.parsers.LanguagePlugin;
import refdiff.parsers.universal.common.CallGraphGenerator;
import refdiff.parsers.universal.common.NodeUtils;
import refdiff.parsers.universal.common.SourceFileReader;
import refdiff.parsers.universal.common.Tokenizer;

import java.util.ArrayList;

public class RubyParser implements LanguagePlugin {
  private int cstId = 0;

  public FilePathFilter getAllowedFilesFilter() {
    return new FilePathFilter(List.of(".rb"));
  }
  
  public CstRoot parse(SourceFileSet folder) {
    TSParser parser = new TSParser();
    TSLanguage tsLang = new TreeSitterRuby();
    parser.setLanguage(tsLang);

    CstRoot root = new CstRoot();

    Map<String, String> sourceCodeMap = SourceFileReader.readAllSourceFiles(folder);
    Map<String, TSTree> parsedTreeMap = new HashMap<>();
    for (Map.Entry<String, String> sourceEntry : sourceCodeMap.entrySet()) {
      TSTree tree = parser.parseString(null, sourceEntry.getValue());
      parsedTreeMap.put(sourceEntry.getKey(), tree);
    }

    for (Map.Entry<String, TSTree> treeEntry : parsedTreeMap.entrySet()) {
      String filePath = treeEntry.getKey();
      TSTree tree = treeEntry.getValue();
      String sourceCode = sourceCodeMap.get(filePath);
      byte[] sourceBytes = sourceCode.getBytes(StandardCharsets.UTF_8);
      addNodes(tree, tsLang, root, filePath, sourceBytes);

      TokenizedSource tokenizedSource = Tokenizer.tokenize(tree, tsLang, filePath); // TODO: Should the argument for tokenize be a relative path?
      root.addTokenizedFile(tokenizedSource);
    }

    CallGraphGenerator callGraphGenerator = new CallGraphGenerator(RubyNodeTypes.METHOD);
    callGraphGenerator.generateCallGraph(root, sourceCodeMap);

    return root;
  }

  private void addNodes(TSTree tree, TSLanguage tsLang, CstRoot cstRoot, String filePath, byte[] sourceBytes) {
    TSNode fileTsNode = tree.getRootNode(); // This is the (program) node for Ruby

    String methodQuerySrc = String.join("\n",
        "[",
        "  (method",
        "    name: (_) @method_name", // Captures identifier, constant, operator, etc. for method name
        "    parameters: (method_parameters)? @method_params",
        "    body: ((_) @method_body)?) @method_definition", // The body node itself, now optional
        "",
        "  (singleton_method", // Covers `def self.foo`, `def Class.foo`, `def obj.foo`
        "    name: (_) @method_name",
        "    parameters: (method_parameters)? @method_params",
        "    body: ((_) @method_body)?) @method_definition",
        "]"
    );

    TSQuery methodTsQuery = new TSQuery(tsLang, methodQuerySrc);
    TSQueryCursor cursor = new TSQueryCursor();
    cursor.exec(methodTsQuery, fileTsNode);
    TSQueryMatch match = new TSQueryMatch();

    while (cursor.nextMatch(match)) {
      TSNode methodDefinitionNode = null;
      TSNode nameNode = null;
      TSNode paramsNode = null;
      TSNode bodyNode = null;

      for (TSQueryCapture capture : match.getCaptures()) {
        TSNode capturedNode = capture.getNode();
        String captureName = methodTsQuery.getCaptureNameForId(capture.getIndex());

        switch (captureName) {
          case "method_definition":
            methodDefinitionNode = capturedNode;
            break;
          case "method_name":
            nameNode = capturedNode;
            break;
          case "method_params":
            paramsNode = capturedNode; // Can be null if optional and not present
            break;
          case "method_body":
            bodyNode = capturedNode; 
            break;
        }
      }

      if (methodDefinitionNode == null || nameNode == null) {
        System.err.println("Warning: Could not capture all required parts (definition, name) for a method in " + filePath + " at offset " + match.getId());
        if (methodDefinitionNode == null) System.err.println("  Missing @method_definition");
        if (nameNode == null) System.err.println("  Missing @method_name");
        continue;
      }

      CstNode methodCstNode = new CstNode(cstId++);
      methodCstNode.setType(RubyNodeTypes.METHOD);
      String methodName = NodeUtils.getNodeText(nameNode, sourceBytes);
      methodCstNode.setSimpleName(methodName);
      methodCstNode.setNamespace(filePath + "/");
      methodCstNode.setLocation(NodeUtils.generateLocation(methodDefinitionNode, bodyNode, filePath));
      
      List<String> paramNames = new ArrayList<>();
      if (paramsNode != null) {
        paramNames = extractParametersFromAst(paramsNode, sourceBytes);
        List<Parameter> cstParameters = new ArrayList<>();
        for (String paramName : paramNames) {
          cstParameters.add(new Parameter(paramName));
        }
        methodCstNode.setParameters(cstParameters);
      }
      methodCstNode.setLocalName(methodName + "(" + String.join(", ", paramNames) + ")");

      cstRoot.addNode(methodCstNode); // Add CstNode directly to CstRoot
    }
  }

  // Helper to iterate over parameter definition nodes within (method_parameters)
  private List<String> extractParametersFromAst(TSNode paramsContainerNode, byte[] sourceBytes) {
    List<String> paramNames = new ArrayList<>();
    for (int i = 0; i < paramsContainerNode.getNamedChildCount(); i++) {
        TSNode paramChildNode = paramsContainerNode.getNamedChild(i); // Each child is a potential parameter definition node
        String paramName = extractActualParameterName(paramChildNode, sourceBytes);
        if (paramName != null && !paramName.isEmpty()) {
          paramNames.add(paramName);
        }
    }
    return paramNames;
  }

  // Helper to get the name from a single parameter definition node
  private String extractActualParameterName(TSNode paramNode, byte[] sourceBytes) {
    String nodeType = paramNode.getType();

    switch (nodeType) {
        case "identifier": // e.g., def m(a)
            return NodeUtils.getNodeText(paramNode, sourceBytes);

        case "splat_parameter": // e.g., def m(*a)
        case "hash_splat_parameter": // e.g., def m(**a)
        case "block_parameter": // e.g., def m(&a)
            // The full text of the node gives the desired representation (e.g., "*args", "&block").
            return NodeUtils.getNodeText(paramNode, sourceBytes);

        case "keyword_parameter": // e.g., def m(a: val)
        case "optional_parameter": { // e.g., def m(a = val)
            TSNode nameField = paramNode.getChildByFieldName("name"); // tree-sitter-ruby uses 'name' for the identifier part
            if (nameField != null) {
                return NodeUtils.getNodeText(nameField, sourceBytes);
            }
            break;
        }
    }
    return null; // Parameter type not handled or no name found
  }
}
