#!/usr/bin/env make -f
# We only need javac, often enough, it is going to be linked at /usr/bin/javac
JAVA_HOME?=/usr

JAVA_JAR=classic-benchmark.jar
CLASSIC_C=mandelbrot-c deltablue-c richards-c fannkuch-c

all:  $(CLASSIC_C) $(JAVA_JAR)
	 
%-c: benchmarks/%.c
	$(CC) -O3 $^ -o $@

classic-benchmark.jar:
	ant jar

fannkuch-c: benchmarks/fannkuchredux.c
	$(CC) -std=c11 -O3 $^ -o $@

clean:
	@-rm $(JAVA_JAR)
	@ant clean
	@-rm mandelbrot-c deltablue-c richards-c fannkuch-c

