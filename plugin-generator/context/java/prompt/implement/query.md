# Role
You are an expert in the `tree-sitter` query language (S-expressions).
Your mission is to create accurate and efficient TreeSitter queries to extract specific syntax information from given source code.

# Premise
- **Target Grammar:** `tree-sitter-java`
- **Query Purpose:** To identify specific nodes (classes, interfaces, enum, constructors, and methods) and capture their declaration, body, and parameter nodes.
- **Usage:** The generated query string will be used directly in a Java binding (e.g., in `Query.create(language, queryString)`).

# Input (Illustrative Example)
The query will be applied to the following example source code file and its corresponding parsed output AST.
You must refer to the provided AST S-expressions to determine the exact node names (e.g., `class_body`, `formal_parameters`).

example source code file:
- @plugin-maker/context/java/src/BasicPublicClass.java
corresponding parsed output AST:
- @plugin-maker/context/java/ast/ast-BasicPublicClass.txt
example query:
```scm
[
  (class_declaration
    name: (identifier) @name
    body: (class_body) @body
  ) @decl
  (constructor_declaration
    name: (identifier) @name
    parameters: (_) @params
    body: (constructor_body) @body
  ) @decl
  (method_declaration
    name: (identifier) @name
    parameters: (_) @params
    body: (block) @body
  ) @decl
]
```

# Query Generation Task
Using the input example as a guide, generate a TreeSitter query (S-expression) that captures the following four types of declarations.
Refer explicitly to the provided AST S-expressions to determine the exact node names.

- Class Declarations
  - Capture the entire declaration node as @declaration.
  - Capture the name of the class as @name.
  - Capture the class body (e.g., { ... }) as @body.
- Interface Declarations
  - Capture the entire declaration node as @declaration.
  - Capture the name of the interface as @name.
  - Capture the interface body (e.g., { ... }) as @body.
- Constructor Declarations
  - Capture the entire declaration node as @declaration.
  - Capture the name of the constructor as @name.
  - Capture the parameter list (e.g., (String field)) as @parameters.
  - Capture the constructor body (e.g., { ... }) as @body.
- Method Declarations
  - Capture the entire declaration node as @declaration.
  - Capture the name of the method as @name.
  - Capture the parameter list (e.g., (String arg1, int arg2)) as @parameters.
  - Capture the method body (e.g., { ... } or a semicolon) as @body. (Note: The body of an abstract or interface method might not be a block).

# Output Requirements
- Output Format: Output only the query string itself, formatted in a code block designated for scm (scm ... ).
- For Java Binding: The generated output must be plain text, ready to be passed directly into a Java method like Query.create(). (Do NOT escape it for a Java string literal, e.g., "...").
- Accuracy: You must use the precise node names from the tree-sitter-java grammar (e.g., class_declaration, formal_parameters, block).
