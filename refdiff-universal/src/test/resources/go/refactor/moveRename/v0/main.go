package main

import "fmt"

func foo() {
	fmt.Println("Hello from foo")
}

func bar() {
	for i := 1; i <= 3; i++ {
		fmt.Printf("iteration #%d\n", i)
	}
}
