#include <stdio.h>

int f1(int a, int b) {
	return a + b;
}

int main() {
    int c = f1(1, 1);
    printf("%d\n", c);
    return 0;
}
