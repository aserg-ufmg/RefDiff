#include <stdio.h>

void f1(int b, char a[]) {
	printf("%s %d\n", a, b);
}

int main() {
	f1(1, "abc");
	return 0;
}
