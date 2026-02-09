<?php

// 抽象クラス
abstract class AbstractBaseClass
{
    // 抽象メソッド（public）
    abstract public function publicAbstractMethod(): void;

    // 抽象メソッド（protected）
    abstract protected function protectedAbstractMethod(string $param): string;
}

// インターフェース
interface MyInterface
{
    // publicメソッドのみ定義可能
    public function interfaceMethod(int $a): bool;
}

// トレイト
trait MyTrait
{
    // トレイト内のpublicメソッド
    public function traitMethod(string $name): string
    {
        return "Hello from trait, " . $name;
    }

    // トレイト内のprivateメソッド
    private function privateTraitMethod(): void
    {
        // 内部利用
    }
}

// メインのクラス
class MethodExamples extends AbstractBaseClass implements MyInterface
{
    use MyTrait;

    // --- 1. 基本的なメソッドの定義 ---

    // publicメソッド
    public function publicMethod(): void
    {
        echo "This is a public method.\n";
    }

    // protectedメソッド
    protected function protectedMethod(string $message): void
    {
        echo "This is a protected method: " . $message . "\n";
    }

    // privateメソッド
    private function privateMethod(): void
    {
        echo "This is a private method.\n";
    }

    // --- 2. 修飾子とスコープの組み合わせ ---

    // static publicメソッド
    public static function publicStaticMethod(): void
    {
        echo "This is a public static method.\n";
    }

    // static protectedメソッド
    protected static function protectedStaticMethod(): void
    {
        echo "This is a protected static method.\n";
    }

    // final publicメソッド
    final public function finalPublicMethod(): void
    {
        echo "This is a final public method.\n";
    }

    // final static publicメソッド
    final public static function finalPublicStaticMethod(): void
    {
        echo "This is a final public static method.\n";
    }

    // --- 3. 引数のバリエーション ---

    // 型ヒント付き引数
    public function methodWithTypedArgument(int $a, float $b): void
    {
        // ...
    }

    // デフォルト値付き引数
    public function methodWithDefaultValue(string $name = "Guest"): string
    {
        return "Hello, " . $name;
    }

    // 参照渡し（&）引数
    public function methodByReference(int &$number): void
    {
        $number++;
    }

    // 可変長引数（...）
    public function methodWithVariadicArguments(string ...$names): void
    {
        // ...
    }

    // 全ての引数の組み合わせ
    public function complexArguments(string $name, int $id = 0, array ...$data): void
    {
        // ...
    }

    // --- 4. 戻り値のバリエーション ---

    // 戻り値の型ヒント
    public function methodWithReturnType(): int
    {
        return 123;
    }

    // voidの戻り値
    public function methodWithVoidReturnType(): void
    {
        // ...
    }

    // nullableな戻り値
    public function methodWithNullableReturnType(): ?string
    {
        return null;
    }

    // --- 5. 親クラスのメソッドの実装とオーバーライド ---

    // 抽象クラスのメソッド実装
    public function publicAbstractMethod(): void
    {
        echo "Implementing public abstract method.\n";
    }

    protected function protectedAbstractMethod(string $param): string
    {
        return "Implementing protected abstract method: " . $param;
    }

    // インターフェースのメソッド実装
    public function interfaceMethod(int $a): bool
    {
        return true;
    }

    // --- 6. PHP 8.0以降の記法（コンストラクタプロパティ昇格） ---

    public function __construct(
        private string $name,
        public readonly int $id
    ) {
        // プロパティは自動的に定義される
    }

    // --- 7. 無名クラス ---

    public function createAnonymousClass(): object
    {
        return new class {
            public function anonymousMethod(): string
            {
                return "This is an anonymous method.";
            }
        };
    }
}

// --------------------------------------------------

// 外部関数（クラスの外にある通常の関数も解析対象となりうる）
function globalFunction(string $param): array
{
    return [$param];
}