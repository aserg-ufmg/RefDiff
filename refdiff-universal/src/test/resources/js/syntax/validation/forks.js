const forks = Object.freeze({
  // Without this fork, importing `shared/ReactSharedInternals` inside
  // the `react` package itself would not work due to a cyclical dependency.
  './packages/shared/ReactSharedInternals.js': (
    bundleType,
    entry,
    dependencies,
    _moduleType,
    bundle
  ) => {
  }
});