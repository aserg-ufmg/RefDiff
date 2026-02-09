package main

func calculateSum(a, b int) int {
	return a + b
}

func foo() {
	result := calculateSum(5, 3)
	println("The sum is", result)
}

func bar() {
	println("I am bar")
}
