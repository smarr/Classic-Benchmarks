#!/usr/bin/env make -f
# We only need javac, often enough, it is going to be linked at /usr/bin/javac
JAVA_HOME?=/usr

all: DeltaBlue.class Mandelbrot.class Richards.class fannkuchredux.class \
	 mandelbrot-c deltablue-c richards-c fannkuch-c

%.class: benchmarks/%.java
	$(JAVA_HOME)/bin/javac -d . $^

%-c: benchmarks/%.c
	$(CC) -O3 $^ -o $@

fannkuch-c: benchmarks/fannkuchredux.c
	$(CC) -std=c11 -O3 $^ -o $@

clean:
	@-rm *.class
	@-rm mandelbrot-c deltablue-c richards-c fannkuch-c

