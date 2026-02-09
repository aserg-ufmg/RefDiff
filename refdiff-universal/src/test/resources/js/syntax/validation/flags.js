Module._load = function (request, parent) {
  if (request === 'ReactNativeInternalFeatureFlags') {
    return DynamicFeatureFlagsNative;
  } else if (request === 'ReactFeatureFlags') {
    return DynamicFeatureFlagsWWW;
  }

  return originalLoad.apply(this, arguments);
};