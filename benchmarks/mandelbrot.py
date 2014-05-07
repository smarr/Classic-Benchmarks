## Transliteration of mandelbrot.c imported from the JRuby Repository
## 2014-04-21 Stefan Marr

# Copyright © 2004-2013 Brent Fulgham
#
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
#   * Redistributions of source code must retain the above copyright notice,
#     this list of conditions and the following disclaimer.
#
#   * Redistributions in binary form must reproduce the above copyright notice,
#     this list of conditions and the following disclaimer in the documentation
#     and/or other materials provided with the distribution.
#
#   * Neither the name of "The Computer Language Benchmarks Game" nor the name
#     of "The Computer Language Shootout Benchmarks" nor the names of its
#     contributors may be used to endorse or promote products derived from this
#     software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
# The Computer Language Benchmarks Game
# http://benchmarksgame.alioth.debian.org
#
#  contributed by Karl von Laudermann
#  modified by Jeremy Echols
#  modified by Detlef Reichl
#  modified by Joseph LaFata
#  modified by Peter Zotov
#
# http://benchmarksgame.alioth.debian.org/u64q/program.php?test=mandelbrot&lang=yarv&id=3
import time
import sys


def mandelbrot(size):
    sum = 0
    byte_acc = 0
    bit_num = 0

    for y in xrange(0, size):
        ci = (2.0 * y / size) - 1.0

        for x in xrange(0, size):
            zr = 0.0
            zrzr = zr
            zi = 0.0
            zizi = zi
            cr = (2.0 * x / size) - 1.5
            escape = 1

            for z in xrange(0, 50):
                tr = zrzr - zizi + cr
                ti = 2.0 * zr * zi + ci
                zr = tr
                zi = ti;
                # preserve recalculation
                zrzr = zr * zr
                zizi = zi*zi
                if zrzr + zizi > 4.0:
                    escape = 0
                    break

            byte_acc = (byte_acc << 1) | escape
            bit_num += 1

            # Code is very similar for these cases, but using separate blocks
            # ensures we skip the shifting when it's unnecessary, which is most cases.
            if bit_num == 8:
                sum ^= byte_acc
                byte_acc = 0
                bit_num = 0
            elif x == size - 1:
                byte_acc <<= (8 - bit_num)
                sum ^= byte_acc
                byte_acc = 0
                bit_num = 0
    return sum


def sample():
    return mandelbrot(750) == 192


def microseconds():
    return time.time() * 1000 * 1000


if not sample():
    print "Sanity check failed! Mandelbrot gives wrong result"
    sys.exit(1)

iterations   = 100
warmup       = 0
problem_size = 1000

if len(sys.argv) > 1:
    iterations = int(sys.argv[1])

if len(sys.argv) > 2:
    warmup = int(sys.argv[2])

if len(sys.argv) > 3:
    problem_size = int(sys.argv[3])

print "Overall iterations: %d." % iterations
print "Warmup  iterations: %d." % warmup
print "Problem size:       %d." % problem_size

for i in xrange(0, warmup):
    mandelbrot(problem_size)

for i in xrange(0, iterations):
    start = microseconds()
    mandelbrot(problem_size)
    elapsed = microseconds() - start
    print "Mandelbrot: iterations=1 runtime: %dus" % elapsed
