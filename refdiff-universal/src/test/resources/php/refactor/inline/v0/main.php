<?php

function calculate_sum($a, $b) {
  $sum = $a + $b;
  return $sum;
}

function foo() {
  $result = calculate_sum(5, 3);
  echo "The sum is {$result}\n";
}

function bar() {
  echo "I am bar\n";
}

foo();
bar();

?>
