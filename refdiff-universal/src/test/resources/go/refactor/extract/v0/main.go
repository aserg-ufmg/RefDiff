package main

import "fmt"

func foo() {
	x := 10
	y := 20
	fmt.Println(x + y)
	fmt.Println("hello from foo")
}

func bar() {
	fmt.Println("hello from bar")
}
