<?php
function foo() {
  echo "Hello from foo\n";
}

function bar() {
  foreach ([1, 2, 3] as $i) {
    echo "iteration #{$i}\n";
  }
}
?>