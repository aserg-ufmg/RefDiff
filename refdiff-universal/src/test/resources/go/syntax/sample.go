package main

import "fmt"

// TestStruct はメソッドのレシーバとして使用する構造体です。
type TestStruct struct {
	Name string
}

// MyInt はメソッドを定義するために使用するカスタムのint型です。
type MyInt int

// --- 値レシーバのメソッド ---

// MethodWithValueReceiver は値レシーバを持つメソッドです。
// 通常の構造体メソッドの定義です。
func (t TestStruct) MethodWithValueReceiver() {
	fmt.Println("Value receiver method on TestStruct:", t.Name)
}

// CustomIntMethod はカスタム型に定義された値レシーバのメソッドです。
func (i MyInt) CustomIntMethod() {
	fmt.Println("Value receiver method on MyInt:", i)
}

// --- ポインタレシーバのメソッド ---

// MethodWithPointerReceiver はポインタレシーバを持つメソッドです。
func (t *TestStruct) MethodWithPointerReceiver() {
	fmt.Println("Pointer receiver method on TestStruct:", t.Name)
	t.Name = "ChangedName" // 変更は呼び出し元の構造体に反映されます。
}

// --- 可変長引数を持つメソッド ---

// MethodWithVariadicArgs は可変長引数を受け取るメソッドです。
// numbers は int のスライスとして扱われます。
func (t *TestStruct) MethodWithVariadicArgs(numbers ...int) {
	fmt.Printf("Variadic method on %s. Received %d numbers.\n", t.Name, len(numbers))
	total := 0
	for _, num := range numbers {
		total += num
	}
	fmt.Println("Total:", total)
}

// --- その他のバリエーション ---

// MethodWithMultipleArguments は複数の引数を持つメソッドです。
func (t TestStruct) MethodWithMultipleArguments(a, b int) {
	fmt.Printf("Multiple arguments: %d, %d\n", a, b)
}

// MethodWithReturnValue は戻り値を持つメソッドです。
func (t *TestStruct) MethodWithReturnValue() string {
	return "Hello from " + t.Name
}

// MethodWithNakedReturns は戻り値に名前を付けたメソッドです。
func (t TestStruct) MethodWithNakedReturns(s string) (result string, err error) {
	result = s + " and some text"
	return
}

func main() {
	// メソッド呼び出しのテスト
	ts := TestStruct{Name: "InitialName"}
	ts.MethodWithValueReceiver()
	ts.MethodWithPointerReceiver()
	ts.MethodWithValueReceiver() // Nameが変更されていることを確認

	fmt.Println("---")

	// 可変長引数を持つメソッドのテスト
	// 引数を直接渡すパターン
	ts.MethodWithVariadicArgs(1, 2, 3)
	// スライスを展開して渡すパターン
	numbers := []int{4, 5, 6}
	ts.MethodWithVariadicArgs(numbers...)

	fmt.Println("---")

	// カスタム型のメソッド呼び出し
	var i MyInt = 100
	i.CustomIntMethod()
}

// 関数 - 引数と戻り値がない、最もシンプルな形式
func simpleFunction() {
	fmt.Println("This is a simple function with no parameters or return values.")
}

// 関数 - 引数のみを持つ
func functionWithParameters(x int, y string) {
	fmt.Printf("Function with parameters: x=%d, y=%s\n", x, y)
}

// 関数 - 複数の引数をまとめて定義
func functionWithMultipleSameTypeParameters(a, b int, c, d float64) {
	fmt.Printf("Multiple same-type parameters: a=%d, b=%d, c=%f, d=%f\n", a, b, c, d)
}

// 関数 - 戻り値のみを持つ
func functionWithReturnValue() int {
	return 42
}

// 関数 - 引数と戻り値の両方を持つ
func functionWithBoth(x, y int) int {
	return x + y
}

// 関数 - 複数の戻り値を持つ（エラーハンドリングなどで多用される）
func functionWithMultipleReturnValues(a, b int) (int, error) {
	if b == 0 {
		return 0, fmt.Errorf("division by zero is not allowed")
	}
	return a / b, nil
}

// 関数 - 名前付き戻り値を持つ
func functionWithNamedReturnValues(a int, b int) (sum int, diff int) {
	sum = a + b
	diff = a - b
	return
}

// 関数 - 可変長引数を持つ
func functionWithVariadicParameters(s string, nums ...int) {
	fmt.Printf("Variadic function called with string: %s\n", s)
	total := 0
	for _, n := range nums {
		total += n
	}
	fmt.Printf("Sum of numbers: %d\n", total)
}
