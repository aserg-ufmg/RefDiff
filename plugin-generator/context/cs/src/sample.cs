using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using System.Threading.Tasks;

namespace CSharpFunctionSyntax
{
    /// <summary>
    /// A systematic enumeration of syntactically valid function definitions in C#.
    /// Covers: C# 1.0 through C# 12+.
    /// </summary>
    public unsafe class FunctionCompendium
    {
        // ========================================================================
        // 1. STANDARD METHOD DEFINITIONS
        // ========================================================================

        // 1.1 Standard Block Body
        public void StandardVoid()
        {
            Console.WriteLine("Classic block body");
        }

        // 1.2 Expression-Bodied Member (C# 6.0+)
        public int GetInteger() => 42;

        // 1.3 Static Method (Belongs to type, not instance)
        public static string StaticMethod() => "I am static";

        // 1.4 The "Main" Entry Point (Special Function)
        // Can be void, int, Task, or Task<int>. Can have string[] args or not.
        public static void Main(string[] args) { }

        // ========================================================================
        // 2. PARAMETER MODIFIERS & SIGNATURE VARIATIONS
        // ========================================================================

        // 2.1 Ref, Out, In, and Params
        public void ParameterModifiers(
            int valueType,          // Pass by value
            ref int reference,      // Input/Output by reference
            out int output,         // Output only (must be assigned)
            in int readOnlyRef,     // Read-only reference (cannot be modified)
            params int[] variadic   // Variable number of arguments
        )
        {
            output = 100;
        }

        // 2.2 Optional Arguments (Default Values)
        public void OptionalArgs(string name = "Guest", int retries = 3) { }

        // 2.3 ScopedRef (C# 11 - restricts lifetime of ref struct)
        public void ScopedParameter(scoped ref Span<int> span) { }

        // ========================================================================
        // 3. ASYNCHRONY & ITERATORS
        // ========================================================================

        // 3.1 Async Task
        public async Task<int> DoWorkAsync() => await Task.FromResult(1);

        // 3.2 Async Void (Generally avoid, but valid for event handlers)
        public async void FireAndForget() => await Task.Delay(10);

        // 3.3 ValueTask (Memory efficient async)
        public async ValueTask<int> LightWeightAsync() => 1;

        // 3.4 Iterator (Generator function)
        public IEnumerable<int> NumberGenerator()
        {
            yield return 1;
            yield return 2;
        }

        // 3.5 Async Iterator (IAsyncEnumerable)
        public async IAsyncEnumerable<int> AsyncStream()
        {
            await Task.Delay(1);
            yield return 1;
        }

        // ========================================================================
        // 4. LOCAL FUNCTIONS (Nested within other methods)
        // ========================================================================

        public void LocalFunctionHost()
        {
            int closureVar = 10;

            // 4.1 Standard Local Function (Can access scope)
            int Add(int x) => x + closureVar;

            // 4.2 Static Local Function (Cannot access scope variables - Performance optimization)
            static int Multiply(int x, int y) => x * y;

            // 4.3 Local Function with Attributes (C# 9.0)
            [System.Diagnostics.CodeAnalysis.DoesNotReturn]
            void Fail() => throw new Exception();

            // 4.4 Generic Local Function
            T Identity<T>(T input) => input;
        }

        // ========================================================================
        // 5. LAMBDAS & DELEGATES
        // ========================================================================

        public void LambdaPatterns()
        {
            // 5.1 Standard Expression Lambda
            Func<int, int> square = x => x * x;

            // 5.2 Statement Lambda (Block body)
            Action<string> logger = msg =>
            {
                string log = $"Log: {msg}";
                Console.WriteLine(log);
            };

            // 5.3 Anonymous Method (Old C# 2.0 Syntax, rarely used now)
            Action oldSchool = delegate { Console.WriteLine("Old"); };

            // 5.4 Natural Type Lambda (var inference - C# 10)
            var natural = (int x) => x + 1;

            // 5.5 Explicit Return Type in Lambda (C# 10)
            var explicitRet = object (bool b) => b ? 1 : "two";

            // 5.6 Lambda with Attributes (C# 10)
            var attrLambda = [Obsolete] (int x) => x;

            // 5.7 Static Lambda (No closure capture allowed - C# 9)
            Func<int, int> staticLambda = static x => x * 2;
        }

        // ========================================================================
        // 6. SPECIAL MEMBER FUNCTIONS (Constructors, Properties, etc.)
        // ========================================================================

        // 6.1 Instance Constructor
        public FunctionCompendium() { }

        // 6.2 Static Constructor (Called once per type, no modifiers allowed)
        static FunctionCompendium() { }

        // 6.3 Finalizer (Destructor)
        ~FunctionCompendium() { }

        // 6.4 Property Accessors (Technically methods: get_X, set_X)
        public int Prop
        {
            get => 0;
            set { }
            init { } // C# 9 Init-only setter
        }

        // 6.5 Indexer
        public string this[int index]
        {
            get => "Value";
            set { }
        }

        // 6.6 Custom Event Add/Remove accessors
        public event EventHandler MyEvent
        {
            add { }
            remove { }
        }

        // ========================================================================
        // 7. OPERATOR OVERLOADING & CONVERSIONS
        // ========================================================================

        // 7.1 Binary Operator
        public static FunctionCompendium operator +(FunctionCompendium a, FunctionCompendium b) => a;

        // 7.2 Unary Operator
        public static FunctionCompendium operator ++(FunctionCompendium a) => a;

        // 7.3 Implicit Conversion (FunctionCompendium -> int)
        public static implicit operator int(FunctionCompendium c) => 0;

        // 7.4 Explicit Conversion (int -> FunctionCompendium)
        public static explicit operator FunctionCompendium(int i) => new FunctionCompendium();

        // 7.5 True/False Operators (required for short-circuiting logic in custom types)
        public static bool operator true(FunctionCompendium c) => true;
        public static bool operator false(FunctionCompendium c) => false;

        // ========================================================================
        // 8. GENERICS & CONSTRAINTS
        // ========================================================================

        // 8.1 Generic Method with Constraints
        public T GenericFunc<T>(T input) where T : class, new()
        {
            return new T();
        }

        // ========================================================================
        // 9. UNSAFE CONTEXT & FUNCTION POINTERS
        // ========================================================================

        // 9.1 Unsafe Method
        public unsafe void UnsafeOp(int* ptr) { *ptr = 10; }

        // 9.2 C# 9 Function Pointers (delegate*) - High performance, no GC overhead
        public void FunctionPointerDemo()
        {
            // Defines a pointer to a function taking int, returning void
            delegate*<int, void> ptr = &StaticHelper;
            ptr(10);

            // Unmanaged calling convention
            delegate* unmanaged[Cdecl]<int, void> unmanagedPtr = null; 
        }

        private static void StaticHelper(int x) { }
    }

    // ========================================================================
    // 10. INHERITANCE POLYMORPHISM (Abstract/Virtual)
    // ========================================================================

    public abstract class AbstractBase
    {
        // 10.1 Abstract Method (No body, implementation required in derived)
        public abstract void AbstractMethod();

        // 10.2 Virtual Method (Default body, overridable)
        public virtual void VirtualMethod() { }
    }

    public class DerivedClass : AbstractBase
    {
        // 10.3 Override
        public override void AbstractMethod() { }

        // 10.4 Sealed Override (Cannot be overridden further)
        public sealed override void VirtualMethod() { }

        // 10.5 Method Hiding ('new' keyword)
        public new void ToString() { }
    }

    // ========================================================================
    // 11. INTERFACE METHODS (Including C# 8/11 features)
    // ========================================================================

    public interface IAdvancedInterface
    {
        // 11.1 Standard Declaration
        void DoThings();

        // 11.2 Default Interface Method (Has implementation)
        void DefaultMethod() => Console.WriteLine("Default implementation");

        // 11.3 Static Abstract Member (C# 11 - e.g., for generic math)
        static abstract IAdvancedInterface Create();

        // 11.4 Static Virtual Member
        static virtual void Log() => Console.WriteLine("Interface Log");
    }

    // ========================================================================
    // 12. EXTENSION METHODS & PARTIALS
    // ========================================================================

    // Must be in a top-level static class
    public static class ExtensionContainer
    {
        // 12.1 Extension Method
        public static void Extend(this string str) { }
    }

    public partial class PartialClass
    {
        // 12.2 Partial Method Declaration
        // If not implemented in another partial file, calls to this are removed by compiler.
        partial void PartialHook();

        // 12.3 Partial Method with Implementation Requirement (must have accessibility modifiers)
        public partial void RequiredPartial();
    }

    public partial class PartialClass
    {
        // Implementation of 12.3
        public partial void RequiredPartial() { }
    }

    // ========================================================================
    // 13. PRIMARY CONSTRUCTORS (C# 12)
    // ========================================================================

    // 13.1 Class definition acts as the constructor function
    public class PrimaryCtor(int id, string name)
    {
        public int Id => id; // captured 'id' is available throughout the class
    }
}