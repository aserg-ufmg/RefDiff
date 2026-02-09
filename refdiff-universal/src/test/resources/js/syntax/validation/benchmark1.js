  window.render = function render() {
    ReactDOM.render(
      React.createElement(App, {
        stories: window.stories,
      }),
      app
    );
  };