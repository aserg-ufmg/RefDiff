<?php

function foo() {
  $sum = 5 + 3;
  $result = $sum; // Inlined from calculate_sum
  echo "The sum is {$result}\n";
}

function bar() {
  echo "I am bar\n";
}

foo();
bar();
