/**
 * @file function_declarations.h
 * @brief C言語における、ほぼ全ての関数「宣言」の形式を網羅したヘッダファイル
 *
 * このファイルは、ヘッダファイル (.h) に記述される様々な関数宣言の
 * パターンを学習・参照するために作成されました。
 */

// =============================================================================
//  インクルードガード (必須)
// -----------------------------------------------------------------------------
#ifndef FUNCTION_DECLARATIONS_H
#define FUNCTION_DECLARATIONS_H

#include <stdarg.h>       // 可変長引数リストを扱うために必要
#include <stdbool.h>      // bool型のために必要 (C99)
#include <stddef.h>       // size_t 型のために必要
#include <stdnoreturn.h>  // noreturn のために必要 (C11)

// =============================================================================
//  セクション 1: 基本的な関数宣言 (プロトタイプ宣言)
// -----------------------------------------------------------------------------
// 現代のC言語における最も標準的な形式です。
void basic_func_no_args_no_return(void);
void basic_func_with_args(int id, const char* name);
int basic_func_no_args_with_return(void);
char* basic_func_with_args_and_return(int buffer_size);

// =============================================================================
//  セクション 2: ポインタと配列を用いた関数宣言
// -----------------------------------------------------------------------------

// 2.1 ポインタ
// ------------------------------------------------
// ポインタを引数に取る (値の書き換えや大きなデータの効率的な受け渡し)
void pointer_arg_func(int* output_value);

// ポインタを返す (動的確保したメモリや静的変数のアドレスを返す)
double* pointer_return_func(void);

// 関数ポインタを引数に取る (コールバック関数)
void callback_func(void (*on_complete)(int status));

// 関数ポインタを返す
int (*get_arithmetic_operation(char operator))(int, int);

// 2.2 配列 (重要: 関数の引数としての配列は、実際にはポインタとして扱われます)
// ------------------------------------------------
// サイズを指定した配列 (コンパイラへのヒント。実際はポインタ)
void array_func_fixed_size(int scores[10]);

// サイズを省略した配列 (一般的な形式)
void array_func_unspecified_size(int scores[]);

// 多次元配列 (最初の次元以外はサイズ指定が必須)
void multi_dim_array_func(int matrix[][5], int rows);

// (C99) ポインタがNULLでなく、指定した要素数以上を指すことを示す
void array_func_with_static(int data[static 10]);

// (C99) 可変長配列 (Variable Length Array, VLA) を引数に取る
void vla_func(int rows, int cols, int matrix[rows][cols]);

// =============================================================================
//  セクション 3: 複合型 (構造体・共用体・列挙型) を用いた関数宣言
// -----------------------------------------------------------------------------
// ユーザー定義の複合型を引数や戻り値として使います。

// 構造体の定義
typedef struct {
  double x;
  double y;
} Point;

// 共用体の定義
typedef union {
  int i;
  float f;
} Number;

// 列挙型の定義
typedef enum { STATUS_OK, STATUS_ERROR } StatusCode;

// 構造体を値で返す
Point create_point(double x, double y);

// 構造体を値で受け取る (コピーが発生)
void print_point_by_value(Point p);

// 構造体ポインタで受け取る (効率的)
void move_point_by_pointer(Point* p, double dx, double dy);

// 列挙型を引数と戻り値に使う
bool is_status_ok(StatusCode code);

// =============================================================================
//  セクション 4: 修飾子 (Qualifier) を用いた関数宣言
// -----------------------------------------------------------------------------
// const: 引数のポインタが指す先を変更しないことを保証する
void const_arg_func(const char* message);

// const: 戻り値のポインタが指す先が変更不可であることを示す
const char* const_return_func(void);

// volatile: 引数が最適化によって消されないことを示す (ハードウェアレジスタなど)
void volatile_func(volatile int* hardware_register);

// restrict (C99): 複数のポインタが互いに重複しないメモリ領域を指すことを示す
void restrict_func(char* restrict dest, const char* restrict src, size_t n);

// =============================================================================
//  セクション 5: 特殊な関数 (ヘッダファイルに「定義」を記述するケース)
// -----------------------------------------------------------------------------

// 5.1 inline / static inline 関数
// ------------------------------------------------
// リンカエラーを避けるため、ヘッダに定義する場合は `static inline` が安全です。
static inline int get_max(int a, int b) { return a > b ? a : b; }

// 5.2 可変長引数関数
// ------------------------------------------------
// 固定引数が少なくとも1つ必要で、末尾に `...` を記述します。
int printf_like_func(const char* format, ...);

// =============================================================================
//  セクション 6: 標準キーワードとコンパイラ拡張
// -----------------------------------------------------------------------------

// 6.1 標準キーワード (C11)
// ------------------------------------------------
// (C11) この関数は呼び出し元に戻らないこと(exitするなど)をコンパイラに伝える
noreturn void function_that_never_returns(void);

// 6.2 コンパイラ拡張機能 (GCC/Clang)
// ------------------------------------------------
// この関数が非推奨であることを伝え、使用時に警告を出す
__attribute__((deprecated("Use new_api_function() instead."))) void
old_api_function(void);

// printfのような書式文字列をチェックする
__attribute__((format(printf, 1, 2))) void my_printf(const char* format, ...);

// =============================================================================
//  セクション 7: 古い形式 (K&R C / 非推奨)
// -----------------------------------------------------------------------------
// プロトタイプ宣言が登場する前の古い形式。引数の型チェックが行われないため、
// 現代のコードでは絶対に使用すべきではありません。

// 引数リストが空。これは「引数の情報がない」という意味で、(void)とは異なる。
// どんな引数でも渡せてしまい危険。
int old_style_declaration();

#endif  // FUNCTION_DECLARATIONS_H