package main

import "fmt"

func extracted_method() {
	x := 10
	y := 20
	fmt.Println(x + y)
}

func foo() {
	extracted_method()
	fmt.Println("hello from foo")
}

func bar() {
	fmt.Println("hello from bar")
}
