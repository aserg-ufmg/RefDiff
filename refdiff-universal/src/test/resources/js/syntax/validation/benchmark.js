  class AbstractButton3 extends React.Component {
    render() {
      if (this.props.x === 3) {
        return React.createElement(Link2, {x: 2});
      }
      if (this.props.x === 20) {
        return React.createElement(
          'button',
          {
            className: '_5n7z _4jy0 _4jy4 _517h _51sy _42ft',
            onClick: function () {},
            label: null,
            type: 'submit',
            value: '1',
          },
          undefined,
          'Discard Changes',
          undefined
        );
      }
    }
  }