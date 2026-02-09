// 1. Various forms of function declarations
function classicFunction(param1, param2) {
  let result = param1 + param2;
  console.log(`classicFunction result: ${result}`);
  return result;
}
classicFunction(5, 7);

// Anonymous function (function expression)
const anonymousFunction = function(a, b) {
  return a * b;
};
console.log(`anonymousFunction result: ${anonymousFunction(3, 4)}`);

// Arrow function (ES6)
const arrowFunctionSimple = (x, y) => x / y;
console.log(`arrowFunctionSimple result: ${arrowFunctionSimple(10, 2)}`);

const arrowFunctionSingleParam = param => console.log(`Single argument arrow function: ${param}`);
arrowFunctionSingleParam("Arrow Test");

const arrowFunctionNoParam = () => console.log("No argument arrow function");
arrowFunctionNoParam();

const arrowFunctionBlockBody = (val1, val2) => {
  let sum = val1 + val2;
  return `Arrow function with block body: ${sum}`;
};
console.log(arrowFunctionBlockBody(10, 20));

// 2. Higher-order functions and callbacks
function higherOrderFunction(callback) {
  console.log("Higher-order function executed.");
  callback("Message from higher-order function");
}
higherOrderFunction((msg) => console.log(`Callback executed: ${msg}`));

// 3. Nested functions (testing closures)
function outerFunction(outerVar) {
  let innerVar = "innerVariable"; // This was a string literal, now a variable name idea
  function innerFunction(innerParam) {
      console.log(`Outer variable: ${outerVar}, Inner variable: ${innerVar}, Inner argument: ${innerParam}`);
  }
  return innerFunction;
}
const closureFunc = outerFunction("Outer value");
closureFunc("Inner value");

// 4. Rest parameters (ES6)
function processArguments(firstArg, ...restArgs) {
  console.log(`First argument: ${firstArg}`);
  console.log(`Remaining arguments: ${restArgs.join(', ')}`);
  return restArgs.length;
}
processArguments("A", 1, 2, "B", true);

// 5. Default arguments (ES6)
function greet(name = "Guest") {
  console.log(`Hello, ${name}!`);
}
greet();
greet("Taro");

// 6. Asynchronous functions (async/await)
async function performAsyncOperation(success) {
  console.log("Starting asynchronous operation...");
  try {
      const result = await new Promise((resolve, reject) => {
          setTimeout(() => {
              if (success) {
                  resolve("Asynchronous operation succeeded!");
              } else {
                  reject("Asynchronous operation failed...");
              }
          }, 100);
      });
      console.log(`async/await result: ${result}`);
      return result;
  } catch (error) {
      console.error(`async/await error: ${error}`);
      throw error; // Re-throw the error so it can be caught by the caller
  } finally {
      console.log("finally block of asynchronous operation");
  }
}

performAsyncOperation(true);
performAsyncOperation(false).catch(() => console.log("Error handling complete")); // Catching async error

// 7. Generator (ES6)
function* idGenerator() {
  let id = 1;
  while (true) {
      yield id++;
  }
}
const generator = idGenerator();
console.log(`Generated ID (Gen1): ${generator.next().value}`);
console.log(`Generated ID (Gen2): ${generator.next().value}`);

function* fibonacciSequence() {
  let a = 0, b = 1;
  while (true) {
      yield a;
      [a, b] = [b, a + b]; // Array destructuring assignment
  }
}
const fibGen = fibonacciSequence();
console.log(`Fibonacci number (1): ${fibGen.next().value}`);
console.log(`Fibonacci number (2): ${fibGen.next().value}`);
console.log(`Fibonacci number (3): ${fibGen.next().value}`);

// 8. Error handling (try/catch within a function)
function functionWithErrorHandling(num) {
  try {
      if (num === 0) {
          throw new Error("Number is zero!");
      }
      console.log(`The number is ${num}.`);
      return num * 2;
  } catch (error) {
      console.error(`Error caught within function: ${error.message}`);
      return -1; // Alternative value on error
  } finally {
      console.log("try/catch block within function finished.");
  }
}
functionWithErrorHandling(10);
functionWithErrorHandling(0);

// 9. IIFE (Immediately Invoked Function Expression) (used within a function)
function functionWithIIFE() {
  (function() {
      console.log("This is an IIFE within the function.");
  })();

  const result = (() => {
      return "Arrow function IIFE result";
  })();
  console.log(result);
}
functionWithIIFE();

// 10. Labeled statements (example of use within a function - generally should be avoided)
function labeledLoopFunction() {
  outer: for (let i = 0; i < 2; i++) {
      inner: for (let j = 0; j < 2; j++) {
          if (i === 1 && j === 0) {
              console.log("Skipping inner loop, continuing to outer loop");
              continue outer;
          }
          if (i === 1 && j === 1) {
              console.log("Exiting outer loop");
              break outer;
          }
          console.log(`Labeled loop: i=${i}, j=${j}`);
      }
  }
}
labeledLoopFunction();