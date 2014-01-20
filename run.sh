#!/bin/sh
echo "Compile DeltaBlue (Java)"
javac DeltaBlue.java

echo "Compile Richards (Java)"
javac Richards.java

echo "Run DeltaBlue (Java) (10 times, 1 warmup run, problem size 1000)"
java DeltaBlue 10 1 1000

echo "Run DeltaBlue (PyPy) (10 times, 1 warmup run, problem size 1000)"
pypy deltablue.py 10 1 1000

echo "Run Richards (Java) (10 times, 1 warmup run, problem size 1000)"
java Richards 10 1 1000

echo "Run Richards (PyPy) (10 times, 1 warmup run, problem size 1000)"
pypy richards.py 10 1 1000

