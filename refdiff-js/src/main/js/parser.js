load('esprima.js');

function parse(script) {
	return esprima.parseModule(script, {range: true});
}
