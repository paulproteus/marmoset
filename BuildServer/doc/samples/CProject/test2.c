/*
 * A test executable.
 * Returns 0 on success, non-zero on failure.
 */

#include <stdio.h>
#include "add.h"

int main(int argc, char **argv) {
	int result = add(4, -1);
	int exit_code;

	if (result == 3) {
		fprintf(stdout, "Test passed\n");
		exit_code = 0;
	} else {
		fprintf(stdout, "Test failed (expected 3, got %d)\n", result);
		exit_code = 1;
	}

	return exit_code;
}
