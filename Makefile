#!/usr/bin/env make -f

all: DeltaBlue.class Mandelbrot.class Richards.class \
	 mandelbrot-c deltablue-c richards-c fannkuch-c

DeltaBlue.class: benchmarks/DeltaBlue.java
	javac -d . $^

Mandelbrot.class: benchmarks/Mandelbrot.java
	javac -d . $^

Richards.class: benchmarks/Richards.java
	javac -d . $^

mandelbrot-c: benchmarks/mandelbrot.c
	$(CC) -O3 $^ -o $@

deltablue-c: benchmarks/deltablue.c
	$(CC) -O3 $^ -o $@

richards-c: benchmarks/richards.c
	$(CC) -O3 $^ -o $@

fannkuch-c: benchmarks/fannkuchredux.c
	$(CC) -O3 $^ -o $@

clean:
	@-rm *.class
	@-rm mandelbrot-c deltablue-c richards-c fannkuch-c

