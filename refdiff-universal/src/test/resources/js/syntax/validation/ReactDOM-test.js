div[name] = function () {
  if (input) {
    input.blur();
    expect(document.activeElement.tagName).toBe('BODY');
    log.push('input2 inserted');
  }
  return mutator.apply(this, arguments);
};