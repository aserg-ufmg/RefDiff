  const stream = new ReadableStream(
    {
      type: 'bytes',
      start: (controller): ?Promise<void> => {
        startWork(request);
      },
      pull: (controller): ?Promise<void> => {
        startFlowing(request, controller);
      },
      cancel: (reason): ?Promise<void> => {
        stopFlowing(request);
        abort(request, reason);
      },
    },
    // $FlowFixMe[prop-missing] size() methods are not allowed on byte streams.
    {highWaterMark: 0},
  );