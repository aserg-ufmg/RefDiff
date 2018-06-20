#include <stdio.h>
#include "header.h"

void f1() {
	f2();
}

void f3() {
	f2();
}

int main() {
	f1();
	f3();
	return 0;
}
