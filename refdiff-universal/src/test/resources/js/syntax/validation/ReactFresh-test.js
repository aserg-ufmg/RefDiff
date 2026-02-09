  it('can remount on signature change within a class', async () => {
    if (__DEV__) {
      await testRemountingWithWrapper(Hello => {
        const child = <Hello />;
        return class Wrapper extends React.PureComponent {
          render() {
            return child;
          }
        };
      });
    }
  });