#    The Computer Language Benchmarks Game
#    http://shootout.alioth.debian.org/

#    contributed by Isaac Gouy
#    converted to Java by Oleg Mazurov
#    converted to Python by Buck Golemon
#    modified by Justin Peel

def fannkuch(n):
    maxFlipsCount = 0
    permSign = True
    checksum = 0

    perm1 = list(range(n))
    count = perm1[:]
    rxrange = range(2, n - 1)
    nm = n - 1
    while 1:
        k = perm1[0]
        if k:
            perm = perm1[:]
            flipsCount = 1
            kk = perm[k]
            while kk:
                perm[:k+1] = perm[k::-1]
                flipsCount += 1
                k = kk
                kk = perm[kk]
            if maxFlipsCount < flipsCount:
                maxFlipsCount = flipsCount
            checksum += flipsCount if permSign else -flipsCount

        # Use incremental change to generate another permutation
        if permSign:
            perm1[0],perm1[1] = perm1[1],perm1[0]
            permSign = False
        else:
            perm1[1],perm1[2] = perm1[2],perm1[1]
            permSign = True
            for r in rxrange:
                if count[r]:
                    break
                count[r] = r
                perm0 = perm1[0]
                perm1[:r+1] = perm1[1:r+2]
                perm1[r+1] = perm0
            else:
                r = nm
                if not count[r]:
                    return checksum
            count[r] -= 1

import sys, time

def microseconds():
    return time.time() * 1000 * 1000



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
    fannkuch(problem_size)

if fannkuch(9) != 8629:
    print "Sanity check failed! Fannkuch gives wrong result"
    sys.exit(1)

for i in xrange(0, iterations):
    start = microseconds()
    fannkuch(problem_size)
    elapsed = microseconds() - start
    print "Fannkuch: iterations=1 runtime: %dus" % elapsed
