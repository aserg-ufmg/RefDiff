module.exports = async () => {
  const params = commandLineArgs(paramDefinitions);

  const channel = params.releaseChannel;
  if (
    channel !== 'experimental' &&
    channel !== 'stable' &&
    channel !== 'rc' &&
    channel !== 'latest'
  ) {
    console.error(
      theme.error`Invalid release channel (-r) "${channel}". Must be "stable", "experimental", "rc", or "latest".`
    );
    process.exit(1);
  }

  if (params.commit === null) {
    console.error(theme.error`A --commit param must be specified.`);
    process.exit(1);
  }

  return params;
};