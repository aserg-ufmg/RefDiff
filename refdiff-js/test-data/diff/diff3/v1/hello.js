function foo() {
	var y = 'world!'
	console.log('hello ' + y);
}

function bar() {
	for (var i = 0; i < 3; i++) {
		foo();
	}
}
