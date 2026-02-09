#include <stdio.h>

void f1(char a[], int b) {
	printf("%s %d\n", a, b);
}

int main() {
	f1("abc", 1);
	return 0;
}
