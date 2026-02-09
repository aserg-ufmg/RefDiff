# Project Overview
ソースファイルを解析し、リファクタリングを検出するツールです。
多様な言語に対応するため、各言語のAST(Abstract Syntax Tree)を言語非依存な形式であるCST(Code Structure Tree)に変換する機能を持っています。
変更前後でCSTを比較することで、リファクタリングの検出を行います。
プラグインとコアの2つのモジュールで構成されており、プラグインは各言語のASTをCSTに変換する役割を担っています。
コアはrefdiff-coreモジュールで、プラグインを利用してCSTの比較を行います。
プラグインはrefdiff-universalモジュールに実装されており、各言語のASTをCSTに変換するためのコードが含まれています。

# プラグイン作成手順
## テストの生成
### Role
You are an expert at generating high-quality JUnit tests for a multi-language parser (UniversalPlugin) implemented in Java.
Your mission is to learn test implementation patterns from multiple languages and apply that knowledge to generate test cases for a new language.

### Context (Training Data)
To generate parser tests for the Go language, first, learn the language-independent common testing patterns from the following JavaScript and Ruby test implementations.

- Overview:
    - Test Target Code: Code that covers various function or method or class definitions
    - Test Target Ctags: Reference for Retrieving Line Numbers of Target Syntactic Elements
    - Test Implementation: A JUnit test that parses the aforementioned file and verifies that function names, line numbers, argument lists, etc., are correctly extracted as CST nodes.

- Training Data 1: JavaScript
    - Test Target Code
        - @refdiff-universal/src/test/resources/js/syntax/class/dir/class.js
        - @refdiff-universal/src/test/resources/js/syntax/file/dir/file.js
        - @refdiff-universal/src/test/resources/js/syntax/function/function.js
    - Test Target Ctags
        - @refdiff-universal/src/test/resources/js/syntax/class/tags
        - @refdiff-universal/src/test/resources/js/syntax/file/tags
        - @refdiff-universal/src/test/resources/js/syntax/function/tags
    - Test Implementation: @refdiff-universal/src/test/java/refdiff/parsers/universal/js/TestParser.java

- Training Data 2: Ruby
    - Test Target Code: @refdiff-universal/src/test/resources/ruby/syntax/method.rb
    - Test Target Ctags: @refdiff-universal/src/test/resources/ruby/syntax/tags
    - Test Implementation: @refdiff-universal/src/test/java/refdiff/parsers/universal/ruby/TestParser.java

### Output Requirements
- Generation Language: The output test code must be Java.
- Inference and Application:
- Infer the language's node types and use them, such as NodeTypes.FUNCTION and NodeTypes.STRUCT.
- Accurately map the code structure (packages, functions, type definitions, etc.) to ExpectedNode records.
- Naming Convention: The method name should clearly describe the feature of the language being tested, for example, shouldParseGoFunctionsAndStructsCorrectly.
- Structure Replication: Strictly reproduce the structure of the learned tests (use of SourceFolder, collection of CstNodes, creation of an ExpectedNode list, and the verification loop using assertThat).
- Format: Output the generated code as a complete Java method, suitable for being placed in a new refdiff/parsers/universal/go/TestParser.java file.


## プラグインの実装
### 役割
あなたは、Tree-sitterが出力したASTを解析し、指定された CstNode 形式に変換するJavaコードを生成する専門家です。

- 学習: 提供されたJavaScriptとRubyの変換実装例（ソースコード、AST、テストコード）を分析し、ASTノードから CstNode の各プロパティ（type, name, location など）を抽出する共通パターンを学習します。
- 分析: 新しい言語（Go）のAST (ast.txt) の構造を理解し、CSTにマッピングすべき構文要素（関数、メソッドなど）を特定します。
- 実装: 学習したパターンと分析結果を基に、Go言語のASTをCSTに変換するJavaコードを生成します。

### 前提
生成するコードは、以下の仕様を持つ CstNode オブジェクトを作成する必要があります。
- `CstNode`: 構文木のノードを表します。以下の重要なプロパティを持っています。
  - type: ノードの種類 (例: GoNodeTypes.FUNCTION_DECLARATION)
  - name: ノードの単純名 (例: myFunction)
  - localName: 修飾子や引数を含むローカル名 (例: myFunction(arg1, arg2))
  - namespace: ノードが属する名前空間 (例: main)
  - location: ファイルパス、開始/終了位置・行番号などの情報
  - parameters: 関数の引数リスト (例: ["arg1", "arg2"])

### コンテキスト (学習データ)
JavaScriptとRubyの例から、ASTからCSTへの変換ロジックを学習してください。特に、ASTの特定のノードタイプから CstNode のプロパティをどのように埋めているかに注目してください。

- 概要:
  - 元のソースファイル: Tree-sitterが解析したソースコードファイル
  - ASTファイル: Tree-sitterが出力したASTのテキストファイル
  - テストファイル: JUnitテストで、ASTからCSTへの変換を検証するコード
  - 変換処理ファイル: ASTをCSTに変換する処理が実装されたJavaクラス

#### 学習データ1: JavaScript
- ファイル参照:
    - 元のソースファイル:
      - @plugin-maker/context/js/src/class.txt
      - @plugin-maker/context/js/src/file.txt
      - @plugin-maker/context/js/src/function.txt
    - ASTファイル:
      - @plugin-maker/context/js/ast/ast-class.txt
      - @plugin-maker/context/js/ast/ast-file.txt
      - @plugin-maker/context/js/ast/ast-function.txt
    - テストファイル: @refdiff-universal/src/test/java/refdiff/parsers/universal/js/TestParser.java
    - 変換処理ファイル: @refdiff-universal/src/main/java/refdiff/parsers/universal/js/JsParser.java

#### 学習データ2: Ruby
- ファイル参照:
    - 元のソースファイル: @plugin-maker/context/ruby/src/method.rb
    - ASTファイル: @plugin-maker/context/ruby/ast/ast-method.txt
    - テストファイル: @refdiff-universal/src/test/java/refdiff/parsers/universal/ruby/TestParser.java
    - 変換処理ファイル: @refdiff-universal/src/main/java/refdiff/parsers/universal/ruby/RubyParser.java

# 出力要件
- 出力言語: Java
- 品質: 提供されたテストファイル (TestParser.java) を修正することなく、すべてのテストケースをパスすること。
- コーディングスタイル: refdiff-universal プロジェクト内の既存のJavaコードのスタイルに合わせてください。
- 依存関係: プロジェクトに既に存在するクラスやライブラリのみを使用してください。新たな外部依存を追加しないでください。
- 出力形式: 実装されたJavaメソッドのコードブロックのみを出力してください。解説や説明はコードの前に簡潔に記述するだけに留めてください。