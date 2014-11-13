#!/usr/bin/env make -f
# We only need javac, often enough, it is going to be linked at /usr/bin/javac
JAVA_HOME?=/usr

CLASSIC_JAVA=DeltaBlue.class Mandelbrot.class Richards.class fannkuchredux.class
CLASSIC_C=mandelbrot-c deltablue-c richards-c fannkuch-c

SOM_JAVA= som \
            som/Benchmark.class som/BubbleSort.class  som/Permute.class   \
            som/Random.class    som/Storage.class     som/WhileLoop.class \
			som/FieldLoop.class som/Queens.class      som/Sieve.class     \
			som/Bounce.class    som/IntegerLoop.class som/QuickSort.class \
			som/Sort.class      som/Towers.class

all:  $(CLASSIC_C) $(CLASSIC_JAVA) $(SOM_JAVA)
	 

%.class: benchmarks/%.java
	$(JAVA_HOME)/bin/javac -d . $^

som/%.class: benchmarks/som/%.java
	$(JAVA_HOME)/bin/javac -cp benchmarks -d . $^

som:
	mkdir som

%-c: benchmarks/%.c
	$(CC) -O3 $^ -o $@

fannkuch-c: benchmarks/fannkuchredux.c
	$(CC) -std=c11 -O3 $^ -o $@

clean:
	@-rm *.class
	@-rm mandelbrot-c deltablue-c richards-c fannkuch-c

