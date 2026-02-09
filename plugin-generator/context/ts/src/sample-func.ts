/**
 * comprehensive_functions.ts
 * * A systematic enumeration of every syntactically valid way to define 
 * and type functions in TypeScript.
 */

// =============================================================================
// 1. STANDARD FUNCTION DECLARATIONS
// =============================================================================

/**
 * Basic named function declaration.
 * Hoisted to the top of the scope.
 */
function basicDeclaration(x: number, y: number): number {
    return x + y;
}

/**
 * Void return type (explicit).
 * Function performs an action but returns no value.
 */
function logMessage(message: string): void {
    console.log(message);
}

/**
 * Never return type.
 * Function that never completes (throws or loops forever).
 */
function throwError(msg: string): never {
    throw new Error(msg);
}

// =============================================================================
// 2. FUNCTION EXPRESSIONS
// =============================================================================

/**
 * Anonymous function expression assigned to a variable.
 * Not hoisted.
 */
const addExpression = function (x: number, y: number): number {
    return x + y;
};

/**
 * Named function expression (NFE).
 * Useful for self-reference (recursion) within the function body.
 */
const factorial = function fact(n: number): number {
    return n <= 1 ? 1 : n * fact(n - 1);
};

// =============================================================================
// 3. ARROW FUNCTIONS (LAMBDAS)
// =============================================================================

/**
 * Basic arrow function with explicit block body.
 */
const arrowBlock = (x: number): number => {
    return x * 2;
};

/**
 * Arrow function with implicit return (expression body).
 */
const arrowImplicit = (x: number): number => x * 2;

/**
 * Arrow function returning an object literal.
 * Requires parentheses () around the object to differentiate from a block.
 */
const createPoint = (x: number, y: number) => ({ x, y });

/**
 * Curried Arrow Function (High Order Function).
 * A function returning another function.
 */
const multiplier = (factor: number) => (value: number) => value * factor;

// =============================================================================
// 4. PARAMETER VARIATIONS
// =============================================================================

/**
 * Optional Parameters.
 * Marked with `?`. Must come after required parameters.
 */
function greet(name: string, greeting?: string): string {
    return `${greeting || 'Hello'}, ${name}`;
}

/**
 * Default Parameters.
 * Type is often inferred from the default value.
 */
function createUser(name: string, role: string = "guest"): void {
    console.log(name, role);
}

/**
 * Rest Parameters.
 * Collects standard arguments into an array.
 */
function sumAll(...numbers: number[]): number {
    return numbers.reduce((a, b) => a + b, 0);
}

/**
 * Destructured Object Parameters.
 * unpacking arguments directly in the signature.
 */
function renderConfig({ width, height }: { width: number; height: number }): void {
    console.log(width * height);
}

// =============================================================================
// 5. TYPESCRIPT SPECIFICS: GENERICS & POLYMORPHISM
// =============================================================================

/**
 * Generic Function Declaration.
 * T captures the type of the argument passed.
 */
function identity<T>(arg: T): T {
    return arg;
}

/**
 * Generic Arrow Function.
 * Note: In .tsx files, <T> might confuse the JSX parser, so <T,> is often used.
 */
const genericArrow = <T>(items: T[]): T | undefined => items[0];

/**
 * Generic with Constraints.
 * T must possess a `length` property.
 */
function logLength<T extends { length: number }>(arg: T): number {
    return arg.length;
}

// =============================================================================
// 6. TYPESCRIPT SPECIFICS: OVERLOADS
// =============================================================================

/**
 * Function Overloading.
 * Allows a function to accept different combinations of arguments.
 */

// Signature 1
function getTimestamp(date: Date): number;
// Signature 2
function getTimestamp(isoString: string): number;
// Implementation (Not visible to the caller via types, must handle all signatures)
function getTimestamp(value: Date | string): number {
    if (value instanceof Date) return value.getTime();
    return new Date(value).getTime();
}

// =============================================================================
// 7. ASYNCHRONOUS & GENERATOR FUNCTIONS
// =============================================================================

/**
 * Async Function.
 * Always returns a Promise.
 */
async function fetchData(url: string): Promise<string> {
    return "data";
}

/**
 * Generator Function.
 * Returns an IterableIterator. Uses `function*`.
 */
function* sequence(): Generator<number> {
    yield 1;
    yield 2;
}

/**
 * Async Generator Function.
 * Allows `await` inside `for await...of` loops.
 */
async function* asyncSequence(): AsyncGenerator<number> {
    await new Promise(r => setTimeout(r, 100));
    yield 1;
}

// =============================================================================
// 8. TYPE PREDICATES & ASSERTIONS
// =============================================================================

/**
 * User-Defined Type Guard (`is` keyword).
 * If true, TS narrows the type of `pet` to `Fish`.
 */
type Fish = { swim: () => void };
type Bird = { fly: () => void };

function isFish(pet: Fish | Bird): pet is Fish {
    return (pet as Fish).swim !== undefined;
}

/**
 * Assertion Functions (`asserts` keyword).
 * Throws if the condition isn't met; otherwise tells TS the condition is true.
 */
function assertIsString(val: any): asserts val is string {
    if (typeof val !== "string") {
        throw new Error("Not a string!");
    }
}

// =============================================================================
// 9. METHODS IN CLASSES & OBJECTS
// =============================================================================

class Calculator {
    // Parameter Property (Constructor shorthand)
    constructor(private initialValue: number) {}

    /**
     * Standard Instance Method.
     */
    add(n: number): number {
        return this.initialValue + n;
    }

    /**
     * Async Method.
     */
    async compute(): Promise<void> {}

    /**
     * Getter (Accessor).
     * Accessed as a property, not a function call.
     */
    get value(): number {
        return this.initialValue;
    }

    /**
     * Setter (Accessor).
     */
    set value(v: number) {
        this.initialValue = v;
    }

    /**
     * Static Method.
     * Called on the class, not the instance.
     */
    static description(): string {
        return "A Helper Class";
    }
}

const objLiteral = {
    // Method Shorthand
    shorthand(x: number) { return x; },
    
    // Standard property assignment (Arrow)
    arrowProp: (x: number) => x,
    
    // Standard property assignment (Function Expression)
    longhand: function(x: number) { return x; }
};

// =============================================================================
// 10. ADVANCED: 'THIS' TYPING
// =============================================================================

/**
 * Explicit `this` parameter.
 * TS uses the first parameter named `this` to type-check the context 
 * (it is erased during compilation).
 */
interface ClickHandler {
    content: string;
    onClick(this: ClickHandler): void;
}

function handleEvent(this: ClickHandler) {
    console.log(this.content); // TS knows 'this' has 'content'
}

// =============================================================================
// 11. TYPE DEFINITIONS (Signatures without Implementation)
// =============================================================================

/**
 * Type Alias for a Function.
 */
type MathOperation = (a: number, b: number) => number;

/**
 * Interface with Call Signature.
 * Allows adding properties to the function (hybrid types).
 */
interface SearchFunc {
    (source: string, subString: string): boolean; // Call signature
    history?: string[]; // Property on the function object
}

/**
 * Interface with Construct Signature (`new`).
 * Defines the shape of a class constructor.
 */
interface DateConstructor {
    new (value?: number): Date;
}

// =============================================================================
// 12. IMMEDIATELY INVOKED FUNCTION EXPRESSIONS (IIFE)
// =============================================================================

// Standard IIFE
(function () {
    console.log("I run immediately");
})();

// Async IIFE
(async () => {
    await Promise.resolve();
})();

// =============================================================================
// 13. DYNAMIC / UNSAFE (Edge Case)
// =============================================================================

/**
 * The Function Constructor.
 * Generally discouraged as it bypasses type safety and invokes eval().
 */
const dynamicFunc = new Function("a", "b", "return a + b");

// =============================================================================

// Export empty object to treat this file as a module
export {};