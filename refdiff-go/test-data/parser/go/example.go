package hello

import (
	"fmt"
	"net/http"
)

type Handler interface {
    Handle(w http.ResponseWriter, r *http.Request)
}

type MyHandle struct {
    Name string
}

func (h *MyHandle) Handle(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintln(w, "hello")
}

func main() {
    handle := MyHandle{Name: "test"}
	http.HandleFunc("/hello", handle.Handle)
}