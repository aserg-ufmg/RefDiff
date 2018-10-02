(function() {
  foo(() => {
    function bar(x, y) {
      return x + y;
    }
  });
})();
