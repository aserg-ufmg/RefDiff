it('does not warn for numeric keys in entry iterable as a child', () => {
  const iterable = {
    '@@iterator': function () {
      let i = 0;
      return {
        next: function () {
          const done = ++i > 2;
          return {value: done ? undefined : [i, <Component />], done: done};
        },
      };
    },
  };
  iterable.entries = iterable['@@iterator'];

  ReactTestUtils.renderIntoDocument(<Component>{iterable}</Component>);
});