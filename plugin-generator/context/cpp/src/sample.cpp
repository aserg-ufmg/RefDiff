/**
 * comprehensive_functions.cpp
 * * A systematic enumeration of syntactically valid function definitions in
 * C++. Covers C++98 through C++23 standards.
 * * Sections:
 * 1. Free Functions & Return Type Deductions
 * 2. Parameter Variations
 * 3. Specifiers & Modifiers (Storage, Linkage, Compile-time)
 * 4. Templates & Concepts (C++20)
 * 5. Class Member Functions (The Big Section)
 * 6. Lambdas & Closures
 * 7. Coroutines (C++20)
 * 8. Edge Cases (Function-try-blocks, etc.)
 */

#include <concepts>
#include <coroutine>
#include <exception>
#include <iostream>
#include <tuple>
#include <vector>

// ============================================================
// SECTION 1: FREE FUNCTIONS & RETURN TYPE DEDUCTIONS
// ============================================================

// 1.1 Classic C-style definition
int classic_add(int a, int b) { return a + b; }

// 1.2 Trailing return type (C++11)
// Essential when return type depends on arguments or for alignment.
auto trailing_return(int a, float b) -> float { return a + b; }

// 1.3 Automatic Return Type Deduction (C++14)
// Compiler deduces type from the return statement.
auto deduced_return(double x) { return x * 2.0; }

// 1.4 decltype(auto) (C++14)
// Preserves reference categories (perfect forwarding for return types).
int global_val = 10;
decltype(auto) return_ref() {
  return (global_val);  // Returns int& because of parentheses
}

// ============================================================
// SECTION 2: PARAMETER VARIATIONS
// ============================================================

// 2.1 Default Arguments
void with_defaults(int a, int b = 10, int c = 20) {
  // b and c are optional
}

// 2.2 Unnamed Parameters
// Valid if the parameter is not used in the body.
void unnamed_param(int /* unused */) {
  // logic ignoring input
}

// 2.3 C-style Variadic Functions (Ellipsis)
// Not typesafe, generally discouraged, but syntactically valid.
#include <cstdarg>
void c_variadic(int count, ...) {
  va_list args;
  va_start(args, count);
  va_end(args);
}

// 2.4 Attributes on parameters (C++17/20)
void with_attributes([[maybe_unused]] int x) {
  // Suppresses warnings if x is not used
}

// ============================================================
// SECTION 3: SPECIFIERS & MODIFIERS
// ============================================================

// 3.1 Static (Internal Linkage)
// Only visible within this translation unit.
static void internal_function() {}

// 3.2 Inline
// Hint to compiler to embed code; changes linkage rules (ODR).
inline void inline_func() {}

// 3.3 Noexcept Specifier
// Guarantees function will not throw.
void no_throw() noexcept {}

// 3.4 Constexpr (C++11/14)
// Function can be evaluated at compile-time if inputs are constant expressions.
constexpr int compute_const(int x) { return x * x; }

// 3.5 Consteval (C++20)
// "Immediate Function" - MUST run at compile time.
consteval int immediate_func(int x) { return x + 1; }

// 3.6 [[noreturn]] Attribute
// Indicates control flow will not return to caller (e.g., exit, throw).
[[noreturn]] void terminate_now() { std::terminate(); }

// ============================================================
// SECTION 4: TEMPLATES & CONCEPTS
// ============================================================

// 4.1 Basic Function Template
template <typename T>
T generic_identity(T x) {
  return x;
}

// 4.2 Explicit Specialization
// (Usually declared in header, defined in cpp, but here is the definition
// syntax)
template <>
int generic_identity<int>(int x) {
  return x + 100;  // Special logic for int
}

// 4.3 Variadic Templates (C++11)
// Recursive parameter unpacking.
template <typename T, typename... Args>
void variadic_template(T first, Args... args) {
  // Expansion logic usually goes here
}

// 4.4 Abbreviated Function Templates (C++20)
// Using 'auto' in parameters generates a template implicitly.
void abbreviated_template(auto x, auto y) {
  // x and y can be different types
}

// 4.5 Constrained Templates (C++20 Concepts)
// 'requires' clause limits what types T can be.
template <typename T>
  requires std::integral<T>
T math_operation(T a) {
  return a * 2;
}

// 4.6 Trailing Requires Clause (C++20)
template <typename T>
void trailing_requires(T a)
  requires std::floating_point<T>
{}

// ============================================================
// SECTION 5: CLASS MEMBER FUNCTIONS
// ============================================================

class ComplexFuncs {
 public:
  // 5.1 Standard Member Function
  void normal_method() {}

  // 5.2 Const Member Function
  // Cannot modify member variables (unless mutable).
  void read_only() const {}

  // 5.3 Static Member Function
  // No 'this' pointer.
  static void static_method() {}

  // 5.4 Virtual Function
  virtual void polymorphic() {}

  // 5.5 Pure Virtual Function
  // Makes class abstract.
  virtual void abstract() = 0;

  // 5.6 Final Override (C++11)
  // Prevents further overriding in derived classes.
  virtual void last_impl() final {}

  // --- Reference Qualifiers (C++11) ---
  // These differentiate based on whether 'this' is an lvalue or rvalue.

  // 5.7 L-value reference qualifier
  // Only callable on l-values: obj.lvalue_only()
  void lvalue_only() & {}

  // 5.8 R-value reference qualifier
  // Only callable on r-values: ComplexFuncs().rvalue_only()
  void rvalue_only() && {}

  // 5.9 Const L-value ref qualifier
  void const_lvalue() const& {}

  // --- Special Member Functions ---

  // 5.10 Constructor (with Member Initializer List)
  ComplexFuncs() {}

  // 5.11 Explicit Constructor (Prevents implicit conversion)
  explicit ComplexFuncs(int x) {}

  // 5.12 Deleted Function
  // Syntactically valid definition that prohibits usage.
  ComplexFuncs(const ComplexFuncs&) = delete;

  // 5.13 Defaulted Function
  // Instructs compiler to generate default implementation.
  ComplexFuncs(ComplexFuncs&&) = default;

  // 5.14 Destructor (Virtual recommended for base classes)
  virtual ~ComplexFuncs() {}

  // 5.15 Conversion Operator
  // Allows object to be treated as int.
  operator int() const { return 0; }

  // 5.16 Explicit Conversion Operator (C++11)
  // Requires static_cast<bool>(obj)
  explicit operator bool() const { return true; }

  // 5.17 Deduced this (C++23)
  // Explicit object parameter. Unifies const/non-const/ref overloads.
  // Note: Compiler support is very recent (e.g., MSVC, Clang 18+, GCC 14+).
  template <typename Self>
  void explicit_object(this Self&& self) {
    // self is equivalent to *this, but with correct value category
  }
};

// 5.18 Out-of-line Definition
// Defining a member outside the class body.
void ComplexFuncs::normal_method() {
  // Implementation
}

// ============================================================
// SECTION 6: LAMBDAS (CLOSURES)
// ============================================================

void lambda_examples() {
  // 6.1 Basic Lambda
  auto basic = []() { return 1; };

  // 6.2 Lambda with Parameters and Trailing Return
  auto params = [](int x) -> double { return x / 2.0; };

  // 6.3 Mutable Lambda
  // Allows modification of captured values by value.
  int val = 0;
  auto mut = [val]() mutable {
    val++;  // Only changes local copy inside lambda
  };

  // 6.4 Generic Lambda (C++14)
  auto generic = [](auto x, auto y) { return x + y; };

  // 6.5 Immediate Lambda (C++20)
  // Usable in constant expressions.
  auto compile_time = []() consteval { return 42; };

  // 6.6 Template Lambda (C++20)
  // Explicit template parameters for lambdas (useful for accessing T inside).
  auto tpl_lambda = []<typename T>(T x) { T::static_method(); };

  // 6.7 Recursive Lambda (C++23 via Deducing This)
  // Allows lambda to call itself without std::function overhead.
  auto fib = [](this auto self, int n) -> int {
    if (n <= 1) return n;
    return self(n - 1) + self(n - 2);
  };
}

// ============================================================
// SECTION 7: COROUTINES (C++20)
// ============================================================

// Minimal scaffolding to make a valid coroutine definition.
struct Task {
  struct promise_type {
    Task get_return_object() { return {}; }
    std::suspend_never initial_suspend() { return {}; }
    std::suspend_never final_suspend() noexcept { return {}; }
    void return_void() {}
    void unhandled_exception() {}
  };
};

// 7.1 Coroutine Definition
// A function becomes a coroutine if it uses co_await, co_yield, or co_return.
Task simple_coroutine() { co_return; }

// ============================================================
// SECTION 8: EDGE CASES
// ============================================================

// 8.1 Function-try-block
// Catches exceptions thrown in the function body OR constructor initializer
// lists.
void function_try_block() try {
  throw std::runtime_error("Error");
} catch (...) {
  // Handle exception
}

// 8.2 Function Pointer Type Definition
// Not a function definition itself, but defines the type signature.
using FuncPtr = int (*)(int, int);

int main() {
  // Call a few to suppress "unused" warnings
  classic_add(1, 2);
  deduced_return(5.5);

  // ComplexFuncs is abstract due to pure virtual, cannot instantiate directly
  // unless we derive from it.

  return 0;
}