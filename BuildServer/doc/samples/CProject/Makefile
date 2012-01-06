# The Makefile that compiles the student solution
# and builds the test executables.

CFLAGS = -Wall

all : test1 test2

test1 : add.o test1.o
	gcc -o $@ test1.o add.o

test2 : add.o test2.o
	gcc -o $@ test2.o add.o

clean :
	rm -f *.o test{1,2}
