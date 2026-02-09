#include <stdio.h>

void f1() {
  printf("Hello world\n");
}

void f2(int a) {
	f1();
	f1();
	f1();
}

int main() {
	f2(1);
	return 0;
}
