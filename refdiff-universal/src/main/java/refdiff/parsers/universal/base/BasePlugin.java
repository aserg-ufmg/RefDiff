package refdiff.parsers.universal.base;

import org.treesitter.TSLanguage;
import org.treesitter.TSParser;
import org.treesitter.TSTree;

import java.util.Map;
import java.util.Stack;
import java.util.HashMap;
import java.nio.charset.StandardCharsets;

import refdiff.core.io.FilePathFilter;
import refdiff.core.io.SourceFileSet;
import refdiff.parsers.universal.common.Tokenizer;
import refdiff.parsers.universal.common.SourceFileReader;
import refdiff.core.cst.CstNode;
import refdiff.core.cst.CstRoot;
import refdiff.core.cst.TokenizedSource;
import refdiff.parsers.LanguagePlugin;
import refdiff.parsers.universal.common.CallGraphGenerator;
import refdiff.parsers.universal.common.InheritanceTreeGenerator;
import refdiff.parsers.universal.common.FilePathUtils;

public abstract class BasePlugin implements LanguagePlugin {

  CstRoot root;
  Stack<CstNode> parentStack;

  /**
   * Abstract method to be implemented by subclasses to provide the file extensions associated with the language.
   * 
   * @return A list of file extensions.
   */
  public abstract FilePathFilter getAllowedFilesFilter();

  /**
   * Abstract method to be implemented by subclasses to provide the TSLanguage.
   * 
   * @return The TSLanguage instance.
   */
  protected abstract TSLanguage getLanguage();

  /**
   * Abstract method to be implemented by subclasses to provide the callable node types for call graph generation.
   * 
   * @return A string representing the callable node types.
   */
  protected abstract String[] getCallableNodeTypes();

  /**
   * Abstract method to be implemented by subclasses to provide the inheritable node types for inheritance tree generation.
   * 
   * @return A string representing the inheritable node types.
   */
  protected abstract String[] getInheritableNodeTypes();

  /**
   * Abstract method to be implemented by subclasses to build the CST from the TreeSitter AST.
   * 
   * @param tree        The parsed syntax tree.
   * @param tsLang      The Tree-sitter language instance.
   * @param path        The file path of the source code.
   * @param sourceBytes The source code as a byte array.
   */
  protected abstract void buildCst(TSTree tree, TSLanguage tsLang, String path, byte[] sourceBytes);

  public CstRoot parse(SourceFileSet folder) {
    initRoot(); // Initialize the CST root each time parse is called

    TSParser parser = new TSParser();
    TSLanguage tsLang = getLanguage(); // Keep tsLang instance for reuse
    parser.setLanguage(tsLang);

    Map<String, String> sourceCodeMap = SourceFileReader.readAllSourceFiles(folder);

    // Parse all files once and store TSTrees
    Map<String, TSTree> parsedTreeMap = new HashMap<>();
    for (Map.Entry<String, String> sourceEntry : sourceCodeMap.entrySet()) {
      TSTree tree = parser.parseString(null, sourceEntry.getValue());
      parsedTreeMap.put(sourceEntry.getKey(), tree);
    }

    for (Map.Entry<String, TSTree> treeEntry : parsedTreeMap.entrySet()) {
      this.parentStack = new Stack<>();
      String filePath = treeEntry.getKey();
      TSTree tree = treeEntry.getValue();
      String sourceCode = sourceCodeMap.get(filePath);
      byte[] sourceBytes = sourceCode.getBytes(StandardCharsets.UTF_8);
      buildCst(tree, tsLang, filePath, sourceBytes);

      if (tree.getRootNode().hasError()) {
        root.addParseFailedFile(filePath);
      }

      TokenizedSource tokenizedSource = Tokenizer.tokenize(tree, tsLang, filePath); // TODO: Should the argument for tokenize be a relative path?
      root.addTokenizedFile(tokenizedSource);
    }

    if (getInheritableNodeTypes().length > 0) { // Only generate inheritance tree for OOP languages
      InheritanceTreeGenerator.generate(root, sourceCodeMap, getInheritableNodeTypes());
    }

    CallGraphGenerator callGraphGenerator = new CallGraphGenerator(getCallableNodeTypes());
    callGraphGenerator.generateCallGraph(root, sourceCodeMap);

    return root;
  }

  /**
   * Adds a CST node to its appropriate parent based on location.
   * 
   * @param cstNode The CST node to be added.
   * @param namespace The namespace to set for top-level nodes.
   */
  protected void addNodeToParent(CstNode cstNode, String namespace) {
    while (!parentStack.isEmpty() && parentStack.peek().getLocation().getEnd() < cstNode.getLocation().getBegin()) {
      parentStack.pop();
    }
    if (parentStack.isEmpty()) {
      cstNode.setNamespace(namespace); // Set namespace for top-level nodes
      root.addNode(cstNode);
    } else {
      CstNode parentNode = parentStack.peek();
      parentNode.addNode(cstNode);
    }
    parentStack.push(cstNode);
  }

  private void initRoot() {
    this.root = new CstRoot();
  }
}