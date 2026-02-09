#include <stdarg.h>  // 可変長引数リストを扱うために必要
#include <stdio.h>
#include <string.h>  // strcpy を使うために必要

/*
================================================================================
 0. 関数プロトタイプ宣言
================================================================================
main関数より後に関数の実体（定義）を記述する場合、
「このような関数が存在します」とコンパイラに事前に知らせる必要があります。
これにより、コンパイラはmain関数内でこれらの関数が使われていてもエラーを出しません。
*/

// 基本的な関数
void function_no_args_no_return(void);
void function_with_args_no_return(int a, int b);
int add(int a, int b);

// K&R形式の関数
int old_style_add(int a, int b);

// 修飾子が付いた関数
static void internal_function(void);
inline int multiply(int a, int b);

// ポインタを扱う関数
void increment(int* num_ptr);
int* get_global_variable_address(void);

// 構造体を扱う関数
typedef struct {
  char name[50];
  int age;
} Person;
Person create_person(const char* name, int age);
void celebrate_birthday(Person* p);

// 関数ポインタ
typedef int (*ArithmeticOperation)(int, int);  // 関数ポインタの型を定義
void perform_calc(int a, int b, ArithmeticOperation func);
int subtract(int a, int b);

// 再帰関数
int factorial(int n);

// 可変長引数を取る関数
int sum_all(int count, ...);

/*
================================================================================
 main 関数 : ここからプログラムが実行されます
================================================================================
*/
int main() {
  printf("--- C言語 関数定義の完全サンプル集 ---\n\n");

  // 1. 引数も戻り値もない関数
  printf("--- 1. 基本的な関数 ---\n");
  function_no_args_no_return();

  // 2. 引数があり、戻り値がない関数
  function_with_args_no_return(10, 20);

  // 3. 引数も戻り値もある関数 (ANSI C形式)
  int result_ansi = add(5, 3);
  printf("3. add(5, 3) の結果: %d\n", result_ansi);

  // 4. K&R形式の関数
  printf("\n--- 2. K&R形式 (旧式) ---\n");
  int result_kr = old_style_add(5, 3);
  printf("4. old_style_add(5, 3) の結果: %d\n", result_kr);

  // 5. static関数 (このファイル内なので呼び出せる)
  printf("\n--- 3. 修飾子付きの関数 ---\n");
  internal_function();

  // 6. inline関数
  int result_inline = multiply(7, 7);
  printf("6. multiply(7, 7) の結果: %d\n", result_inline);

  // 7. ポインタを引数に取る関数 (参照渡し)
  printf("\n--- 4. ポインタを扱う関数 ---\n");
  int my_value = 5;
  printf("7. ポインタ引数の関数 (参照渡し):\n");
  printf("   -> 呼び出し前の値: %d\n", my_value);
  increment(&my_value);  // 変数 my_value のアドレスを渡す
  printf("   -> 呼び出し後の値: %d\n", my_value);

  // 8. ポインタを返す関数
  printf("8. ポインタを返す関数:\n");
  int* p = get_global_variable_address();
  printf("   -> 受け取ったアドレスが指す値: %d\n", *p);
  *p = 200;  // ポインタ経由でグローバル変数を変更
  printf("   -> ポインタ経由で変更後のグローバル変数の値: %d\n",
         global_variable);

  // 9. 構造体を扱う関数
  printf("\n--- 5. 構造体を扱う関数 ---\n");
  Person alice = create_person("Alice", 30);
  printf("9. 構造体を返す/引数に取る関数:\n");
  printf("   -> 作成直後: %s, %d歳\n", alice.name, alice.age);
  celebrate_birthday(&alice);  // 構造体のアドレスを渡す
  printf("   -> 誕生日後: %s, %d歳\n", alice.name, alice.age);

  // 10. 関数ポインタ (コールバック関数)
  printf("\n--- 6. 関数ポインタ ---\n");
  printf("10. 関数ポインタを引数に渡して実行:\n");
  printf("   -> add関数を渡した場合: ");
  perform_calc(10, 5, add);
  printf("   -> subtract関数を渡した場合: ");
  perform_calc(10, 5, subtract);

  // 11. 再帰関数
  printf("\n--- 7. 再帰関数 ---\n");
  int fact_5 = factorial(5);
  printf("11. 5の階乗を計算: %d\n", fact_5);

  // 12. 可変長引数を取る関数
  printf("\n--- 8. 可変長引数 ---\n");
  int total = sum_all(4, 10, 20, 30, 40);
  printf("12. sum_all(4, 10, 20, 30, 40) の結果: %d\n", total);

  printf("\n--- 実行終了 ---\n");
  return 0;
}

/*
================================================================================
 1. 基本的な関数定義 (ANSI C 形式)
================================================================================
*/
void function_no_args_no_return(void) {
  printf("1. 引数も戻り値もない関数が呼ばれました。\n");
}

void function_with_args_no_return(int a, int b) {
  printf("2. 引数あり、戻り値なしの関数が呼ばれました。受け取った値: %d, %d\n",
         a, b);
}

int add(int a, int b) { return a + b; }

/*
================================================================================
 2. K&R (Kernighan & Ritchie) 形式の関数定義
================================================================================
*/
int old_style_add(a, b)
int a;
int b;
{
  return a + b;
}

/*
================================================================================
 3. static / inline 修飾子を付けた関数
================================================================================
*/
// `static`関数: このファイル内からのみ呼び出し可能
static void internal_function(void) {
  printf("5. このファイル内でのみ呼び出し可能なstatic関数です。\n");
}

// `inline`関数: コンパイラにインライン展開を推奨
inline int multiply(int a, int b) { return a * b; }

/*
================================================================================
 4. ポインタを扱う関数
================================================================================
*/
// ポインタを引数に取る関数 (参照渡し)
void increment(int* num_ptr) {
  (*num_ptr)++;  // ポインタが指す先の値をインクリメント
}

// ポインタを返す関数
int global_variable = 100;
int* get_global_variable_address(void) { return &global_variable; }

/*
================================================================================
 5. 構造体を引数に取る/返す関数
================================================================================
*/
// 構造体を値として返す
Person create_person(const char* name, int age) {
  Person new_person;
  strcpy(new_person.name, name);
  new_person.age = age;
  return new_person;
}
// 構造体ポインタを引数に取り、中身を変更する (参照渡し)
void celebrate_birthday(Person* p) {
  p->age++;  // アロー演算子でメンバにアクセス
}

/*
================================================================================
 6. 関数ポインタ (コールバック)
================================================================================
*/
// `add` と同じシグネチャ（引数と戻り値の型）を持つ関数
int subtract(int a, int b) { return a - b; }

// 関数ポインタを引数に取る関数
void perform_calc(int a, int b, ArithmeticOperation func) {
  int result = func(a, b);
  printf("計算結果: %d\n", result);
}

/*
================================================================================
 7. 再帰関数
================================================================================
*/
// nの階乗を計算する (n!)
int factorial(int n) {
  if (n <= 1) {
    return 1;  // 再帰の停止条件
  } else {
    return n * factorial(n - 1);  // 自分自身を呼び出す
  }
}

/*
================================================================================
 8. 可変長引数を取る関数
================================================================================
*/
int sum_all(int count, ...) {
  int total = 0;
  va_list args;
  va_start(args, count);
  for (int i = 0; i < count; ++i) {
    total += va_arg(args, int);
  }
  va_end(args);
  return total;
}