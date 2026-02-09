HTMLElement.prototype.focus = function () {
  focusedElement = this;
  inputFocusedAfterMount = !!this.parentNode;
};