module.exports = async params => {
  return logPromise(
    run(params),
    theme`Checking out "next" from NPM {version ${params.version}}`
  );
};