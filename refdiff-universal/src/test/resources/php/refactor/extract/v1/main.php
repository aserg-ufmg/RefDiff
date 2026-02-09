<?php

function extracted_method() {
  $x = 10;
  $y = 20;
  echo ($x + $y) . PHP_EOL;
}

function foo() {
  extracted_method();
  echo "hello from foo" . PHP_EOL;
}

function bar() {
  echo "hello from bar" . PHP_EOL;
}
