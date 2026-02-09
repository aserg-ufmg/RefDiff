# Role
You are an expert in syntax analysis and a skilled Java programmer.
Your mission is to implement a Java method that converts an AST (Abstract Syntax Tree) into a CST (Code Structure Tree) as part of a multi-language parser (UniversalPlugin), and to ensure that all test cases pass.

- Analysis: Understand the structure of the new language's AST (`ast-filename.txt`) and identify the syntactic elements including variety of function definitions that should be mapped to the CST.
- Implementation: Generate Java code to convert the AST to a CST, based on the learned patterns and analysis results.

# Premise
The code you generate must create `CstNode` objects with the following specifications:
- `CstNode`: Represents a node in the syntax tree. It has the following important properties:
  - `type`: The node type (e.g., `NodeTypes.FUNCTION`)
  - `name`: The simple name of the node (e.g., `my_function`)
  - `localName`: The local name including modifiers and arguments (e.g., `my_function(arg1, arg2)`)
  - `location`: Information such as file path, start/end position, and line numbers
  - `parameters`: A list of function arguments (e.g., `["arg1", "arg2"]`)


# Context (Learning Data)
Learn the AST to CST conversion logic from the Java examples. Pay special attention to how the properties of `CstNode` are populated from specific AST node types.

## Learning Data : Java
- File references:
  - Original source files:
    - @plugin-maker/context/java/src/BasicPublicClass.java
  - AST files:
    - @plugin-maker/context/java/ast/ast-BasicPublicClass.txt
  - Test file: @refdiff-universal/src/test/java/refdiff/parsers/universal/java/TestJavaParser.java
  - Conversion processing file:
    - @refdiff-universal/src/main/java/refdiff/parsers/universal/java/JavaParser.java

Explanation:
  - inherits BaseParser
  - overrides getLanguage():
    - return TreeSitterLanguages (e.g., TreeSitterJava).
  - overrides  getCallableNodeTypes():
    - return a list of node types that represent callable entities (e.g., methods, constructors).
  - overrides getInheritableNodeTypes():
    - return a list of node types that represent inheritance related definitions. (e.g., class, interface).
  - overrides buildCst():
    - Main method to build the CST from the AST extracting necessary information.
    - Define the multi-line querySrc string to match constructs.
    - Use the tree-sitter library for PHP to execute the query.
    - Iterate through every TSQueryMatch.
    - Inside the loop, extract the captured nodes for @declaration, @name, @body, and @parameters.
    - Use a switch statement (or its equivalent) on the declaration node's type.
    - For each type, create a new CstNode (or equivalent object).
    - Call a function like extractSignatureParameters for methods/constructors.
    - Call a function addNodeToParent that is defined in the BaseParser class to add the created CstNode to the CST.
  
# Task (Target for Generation)
Apply the learned patterns from the above examples to generate a new method for converting a PHP AST to a CST.
Generate the code for the `addNodes` method or a similar helper method defined in `PhpParser.java`.
This method is responsible for taking a PHP AST and building a list of `CstNode` objects.
  
Target for Generation: PHP
- File references:
  - Original source files:
    - @plugin-maker/context/php/src/sample.php
  - AST files:
    - @plugin-maker/context/php/ast/ast-sample.txt
  - Test file: @refdiff-universal/src/test/java/refdiff/parsers/universal/php/TestParser.java
  - Conversion processing file: @refdiff-universal/src/main/java/refdiff/parsers/universal/php/PhpParser.java

# Output Requirements
- Output Language: Java
- Quality: The generated code must pass all test cases in the provided test file (`TestParser.java`) without any modifications to it.
- Coding Style: Match the style of existing Java code within the `refdiff-universal` project.
- Dependencies: Use only classes and libraries that already exist in the project. Do not add any new external dependencies.
- Output Format: Output only the code block for the implemented Java method. Provide a brief explanation or description before the code.