function foo() {
	console.log('hello');
}

function bar() {
	for (var i = 0; i < 3; i++) {
		foo();
	}
}