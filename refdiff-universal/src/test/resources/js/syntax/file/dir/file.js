// 1. Variable declarations (var, let, const)
var globalVar = "This is a global variable.";
let blockScopedLet = 100;
const constantValue = true;

// 2. Data types (primitive types and object types)
let stringType = "Hello, World!";
let numberType = 123.45;
let booleanType = false;
let nullType = null;
let undefinedType = undefined;
let symbolType = Symbol('unique');
let bigIntType = 9007199254740991n;

let objectType = {
    key1: "value1",
    key2: 200,
    nestedObject: {
        nestedKey: "nestedValue"
    }
};

let arrayType = [1, "two", true, null, { prop: "value" }];

// 3. Operators
let sum = 10 + 20;
let difference = 30 - 5;
let product = 6 * 7;
let quotient = 49 / 7;
let remainder = 10 % 3;
let exponentiation = 2 ** 3; // ES7

let logicalAnd = true && false;
let logicalOr = true || false;
let logicalNot = !true;

let comparison = (sum > difference) && (product <= quotient);
let strictEquality = (10 === "10");
let looseEquality = (10 == "10");

let ternaryOperator = (sum > 0) ? "Positive" : "Negative";

// 4. Control flow (if/else, switch, for, while, do/while)
if (blockScopedLet > 50) {
    console.log("blockScopedLet is greater than 50.");
} else if (blockScopedLet === 100) {
    console.log("blockScopedLet is 100.");
} else {
    console.log("blockScopedLet is 50 or less.");
}

switch (stringType) {
    case "Hello, World!":
        console.log("String matched.");
        break;
    case "Goodbye":
        console.log("It's a different string.");
        break;
    default:
        console.log("Did not match any string.");
}

for (let i = 0; i < arrayType.length; i++) {
    console.log(`Array element ${i}: ${arrayType[i]}`);
}

for (const prop in objectType) {
    console.log(`Object property ${prop}: ${objectType[prop]}`);
}

for (const value of arrayType) { // ES6
    console.log(`Each element of the array: ${value}`);
}

let count = 0;
while (count < 3) {
    console.log(`while loop: ${count}`);
    count++;
}

let doWhileCount = 0;
do {
    console.log(`do/while loop: ${doWhileCount}`);
    doWhileCount++;
} while (doWhileCount < 2);

// 5. Function declaration (only simple ones in file_level_tests)
function simpleFileFunction(param) {
    return `File-level function: ${param}`;
}
console.log(simpleFileFunction("Test"));


// 6. Object literal extensions (ES6)
const propName = "dynamicProp";
const extendedObject = {
    method() { // Shorthand method definition
        console.log("Method of extended object");
    },
    [propName]: "Dynamic property", // Computed property name
    constantValue // Shorthand property value
};
extendedObject.method();
console.log(extendedObject.dynamicProp);
console.log(extendedObject.constantValue);

// 7. Destructuring assignment (ES6)
const [first, second, ...rest] = arrayType;
console.log(`Destructuring assignment: ${first}, ${second}, ${rest}`);

const { key1, nestedObject: { nestedKey } } = objectType;
console.log(`Object destructuring assignment: ${key1}, ${nestedKey}`);

// 8. Spread syntax (ES6)
const newArray = [...arrayType, 4, 5];
console.log(`Spread syntax array: ${newArray}`);

const newObject = { ...objectType, newProp: "New property" };
console.log(`Spread syntax object:`, newObject);

// 9. Template literals (ES6)
const templateString = `The sum is ${sum} and the product is ${product}.`;
console.log(templateString);

// 10. Modules (import/export) - The parser needs to recognize these
// export const moduleVariable = "This is an exported variable.";
// export function moduleFunction() {
//     console.log("Exported function.");
// }
// export class ModuleClass {
//     constructor() {
//         console.log("Exported class.");
//     }
// }
// import { moduleVariable, moduleFunction, ModuleClass } from './anotherModule.js';

// 11. Asynchronous operations (Promise, async/await) - Top-level await is only valid in module scope, but the syntax itself can be parsed
async function handleFileAsyncOperations() {
    try {
        const result = await new Promise(resolve => setTimeout(() => resolve("File-level async success!"), 50));
        console.log(`File-level async result: ${result}`);
    } catch (error) {
        console.error(`File-level async error: ${error}`);
    }
}
handleFileAsyncOperations();

// 12. Error handling (try/catch/finally, throw)
function mightThrowFileError(shouldThrow) {
    if (shouldThrow) {
        throw new Error("Intentional error at file level!");
    }
    return "No error at file level.";
}

try {
    console.log(mightThrowFileError(false));
    // console.log(mightThrowFileError(true)); // Commented out for error testing
} catch (e) {
    console.error(`Error caught at file level: ${e.message}`);
} finally {
    console.log("File-level try/catch/finally block finished.");
}

// 13. Map and Set (ES6)
const fileMap = new Map();
fileMap.set('id', 123);
console.log(`File-level Map size: ${fileMap.size}`);

const fileSet = new Set();
fileSet.add('item1');
console.log(`File-level Set size: ${fileSet.size}`);

// 14. WeakMap and WeakSet (ES6)
const fileWeakMapKey = {};
const fileWeakMap = new WeakMap();
fileWeakMap.set(fileWeakMapKey, "File-level WeakMap value");

const fileWeakSetObj = {};
const fileWeakSet = new WeakSet();
fileWeakSet.add(fileWeakSetObj);

// 15. Proxy and Reflect (ES6)
const fileTarget = { a: 1 };
const fileHandler = { get: (t, p) => Reflect.get(t, p) * 10 };
const fileProxy = new Proxy(fileTarget, fileHandler);
console.log(`File-level Proxy: ${fileProxy.a}`);

// 16. Symbols (ES6)
const FILE_SYMBOL = Symbol('fileSymbol');
const fileObjWithSymbol = {
    [FILE_SYMBOL]: "File-level symbol"
};
console.log(fileObjWithSymbol[FILE_SYMBOL]);

// 17. Nullish coalescing operator (??) (ES2020)
const fileNullVal = null;
const fileResult = fileNullVal ?? "File-level default";
console.log(`File-level Nullish coalescing: ${fileResult}`);

// 18. Optional chaining (?.) (ES2020)
const fileConfig = { settings: { theme: "dark" } };
console.log(fileConfig?.settings?.theme);

// 19. import() dynamic import (ES2020) - Should be supported syntactically, but evaluated at runtime
// if (false) { // Set to false to prevent execution
//     import('./dynamic_module.js')
//         .then(module => {
//             module.runDynamic();
//         })
//         .catch(err => {
//             console.error("File-level dynamic import error:", err);
//         });
// }

// 20. Top-level await (ES2022) - Only valid when run as a module. May error in script mode.
// (async () => {
//     const fileFetchedData = await Promise.resolve("File-level fetched data");
//     console.log('File-level Fetched data:', fileFetchedData);
// })(); // "File-level fetched data"

// 21. Regexp match indices (ES2022)
const fileRegex = /(abc)(def)/d;
const fileStr = 'abcdef';
const fileMatch = fileStr.match(fileRegex);
if (fileMatch) {
    console.log(`File-level RegExp match indices:`, fileMatch.indices);
}

// 22. Array.prototype.at() (ES2022)
const fileArr = [1, 2, 3];
console.log(`File-level Array.at(-1): ${fileArr.at(-1)}`);

// 23. Object.hasOwn() (ES2022)
const fileObj = { a: 1 };
console.log(`File-level Object.hasOwn('a'): ${Object.hasOwn(fileObj, 'a')}`);

// 24. IIFE (Immediately Invoked Function Expression)
(function() {
    console.log("This is a file-level IIFE.");
})();