#include <stdio.h>

int f1() {
	return 1;
}

int f2() {
	return f1();
}

int main() {
	int a = f2();
	printf("%d", a);
	return 0;
}
