#!/usr/bin/env make -f

all: DeltaBlue.class Mandelbrot.class Richards.class mandelbrot-c

DeltaBlue.class: DeltaBlue.java
	javac $^

Mandelbrot.class: Mandelbrot.java
	javac $^

Richards.class: Richards.java
	javac $^

mandelbrot-c: mandelbrot.c
	$(CC) -O3 $^ -o $@

clean:
	@-rm *.class
	@-rm mandelbrot-c

