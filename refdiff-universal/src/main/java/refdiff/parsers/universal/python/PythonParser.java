package refdiff.parsers.universal.python;

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
import org.treesitter.TreeSitterPython;

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
import java.util.stream.Collectors;
import java.util.ArrayList;

public class PythonParser implements LanguagePlugin {
  private int cstId = 0;

  public FilePathFilter getAllowedFilesFilter() {
    return new FilePathFilter(List.of(".py"));
  }
  
  public CstRoot parse(SourceFileSet folder) {
    TSParser parser = new TSParser();
    TSLanguage tsLang = new TreeSitterPython();
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

      TokenizedSource tokenizedSource = Tokenizer.tokenize(tree, tsLang, filePath);
      root.addTokenizedFile(tokenizedSource);
    }

    CallGraphGenerator callGraphGenerator = new CallGraphGenerator(PythonNodeTypes.FUNCTION);
    callGraphGenerator.generateCallGraph(root, sourceCodeMap);

    return root;
  }

  private void addNodes(TSTree tree, TSLanguage tsLang, CstRoot cstRoot, String filePath, byte[] sourceBytes) {
    TSNode fileTsNode = tree.getRootNode();

    String functionQuerySrc = """
        (function_definition
          name: (identifier) @function_name
          parameters: (parameters) @function_params
          body: (block) @function_body) @function_definition
        """;

    TSQuery functionTsQuery = new TSQuery(tsLang, functionQuerySrc);
    TSQueryCursor cursor = new TSQueryCursor();
    cursor.exec(functionTsQuery, fileTsNode);
    TSQueryMatch match = new TSQueryMatch();

    while (cursor.nextMatch(match)) {
      TSNode functionDefinitionNode = null;
      TSNode nameNode = null;
      TSNode paramsNode = null;
      TSNode bodyNode = null;

      for (TSQueryCapture capture : match.getCaptures()) {
        TSNode capturedNode = capture.getNode();
        String captureName = functionTsQuery.getCaptureNameForId(capture.getIndex());

        switch (captureName) {
          case "function_definition":
            functionDefinitionNode = capturedNode;
            break;
          case "function_name":
            nameNode = capturedNode;
            break;
          case "function_params":
            paramsNode = capturedNode;
            break;
          case "function_body":
            bodyNode = capturedNode;
            break;
        }
      }

      if (functionDefinitionNode == null || nameNode == null || bodyNode == null) {
        continue;
      }

      CstNode functionCstNode = new CstNode(cstId++);
      functionCstNode.setType(PythonNodeTypes.FUNCTION);
      
      String functionName = NodeUtils.getNodeText(nameNode, sourceBytes);
      functionCstNode.setSimpleName(functionName);

      functionCstNode.setNamespace(filePath + "/");

      functionCstNode.setLocation(NodeUtils.generateLocation(functionDefinitionNode, bodyNode, filePath));

      List<Parameter> cstParameters = new ArrayList<>();
      if (paramsNode != null) {
          cstParameters = extractParametersFromAst(paramsNode, sourceBytes);
      }
      functionCstNode.setParameters(cstParameters);

      StringBuilder localNameBuilder = new StringBuilder();
      localNameBuilder.append(functionName);
      localNameBuilder.append("(");
      localNameBuilder.append(
          cstParameters.stream()
              .map(Parameter::getName)
              .collect(Collectors.joining(", ")));
      localNameBuilder.append(")");
      functionCstNode.setLocalName(localNameBuilder.toString());

      cstRoot.addNode(functionCstNode);
    }
  }

  private List<Parameter> extractParametersFromAst(TSNode paramsContainerNode, byte[] sourceBytes) {
    List<Parameter> paramNames = new ArrayList<>();
    for (int i = 0; i < paramsContainerNode.getChildCount(); i++) {
        TSNode paramChildNode = paramsContainerNode.getChild(i);
        String nodeType = paramChildNode.getType();

        if (nodeType.equals("identifier") || nodeType.equals("list_splat_pattern") || nodeType.equals("dictionary_splat_pattern")) {
            paramNames.add(new Parameter(NodeUtils.getNodeText(paramChildNode, sourceBytes)));
        } else if (nodeType.equals("typed_parameter")) {
            TSNode identifierNode = paramChildNode.getChild(0);
            if (identifierNode.getType().equals("identifier")) {
                paramNames.add(new Parameter(NodeUtils.getNodeText(identifierNode, sourceBytes)));
            }
        } else if (nodeType.equals("default_parameter")) {
            TSNode identifierNode = paramChildNode.getChildByFieldName("name");
            if (identifierNode != null) {
                paramNames.add(new Parameter(NodeUtils.getNodeText(identifierNode, sourceBytes)));
            }
        } else if (nodeType.equals("typed_default_parameter")) {
            TSNode identifierNode = paramChildNode.getChild(0);
            if (identifierNode.getType().equals("identifier")) {
                paramNames.add(new Parameter(NodeUtils.getNodeText(identifierNode, sourceBytes)));
            }
        }
    }
    return paramNames;
  }
}
