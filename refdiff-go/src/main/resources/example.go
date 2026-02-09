package testdata

import (
	"fmt"
	"image"
)

type Test struct {
	Name string
	Year int
	test *Test
}

func myfunc(i int, s string, err error, pt image.Point, x []float64) {}

func (t *Test) Foo() {
	fmt.Println("test")
}

func Bar(bla int) int {
	bla = bla + 1
	return bla
}
