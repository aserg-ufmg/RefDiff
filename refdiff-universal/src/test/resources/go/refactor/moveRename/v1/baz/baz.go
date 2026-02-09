package baz

import "fmt"

func baz() {
	for i := 1; i <= 3; i++ {
		fmt.Printf("iteration #%d\n", i)
	}
}
