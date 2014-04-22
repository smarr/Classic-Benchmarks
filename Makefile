#!/usr/bin/env make -f

all: DeltaBlue.class Mandelbrot.class mandelbrot-c

DeltaBlue.class: DeltaBlue.java
	javac DeltaBlue.java

Mandelbrot.class: Mandelbrot.java
	javac Mandelbrot.java

mandelbrot-c: mandelbrot.c
	$(CC) -O3 $^ -o $@

clean:
	@-rm *.class
	@-rm mandelbrot-c

