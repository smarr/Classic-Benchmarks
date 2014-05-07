/*
 * The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * contributed by Ledrug Katz
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>

#include "harness.h"

/* this depends highly on the platform.  It might be faster to use
   char type on 32-bit systems; it might be faster to use unsigned. */

typedef int elem;

elem s[16], t[16];

int flip(int max_n)
{
   register int i;
   register elem *x, *y, c;

   for (x = t, y = s, i = max_n; i--; )
      *x++ = *y++;
   i = 1;
   do {
      for (x = t, y = t + t[0]; x < y; )
         c = *x, *x++ = *y, *y-- = c;
      i++;
   } while (t[t[0]]);
   return i;
}

static inline void rotate(int n)
{
   elem c;
   register int i;
   c = s[0];
   for (i = 1; i <= n; i++) s[i-1] = s[i];
   s[n] = c;
}

/* Tompkin-Paige iterative perm generation */
int tk(int n)
{
   int checksum = 0;
   int odd = 0;
   int maxflips = 0;
   int i = 0, f;
   elem c[16] = {0};

   while (i < n) {
      rotate(i);
      if (c[i] >= i) {
         c[i++] = 0;
         continue;
      }

      c[i]++;
      i = 1;
      odd = ~odd;
      if (*s) {
         f = s[s[0]] ? flip(n) : 1;
         if (f > maxflips) maxflips = f;
         checksum += odd ? -f : f;
      }
   }
   return checksum;
}

int fannkuch(int max_n) {
  for (int i = 0; i < max_n; i++) {
    s[i] = i;
  }
  return tk(max_n);
}

void sample() {
  int r = fannkuch(9);
  if (r != 8629) {
    printf("Fannkuch(9) failed, delivers unexpected result: %d\n", r);
    abort();
  }
}

int main(int argc, char **argv)
{
   int iterations = 100;
   int warmup     = 0;
   int problem_size = 9;

   parse_argv(argc, argv, &iterations, &warmup, &problem_size);

   if (problem_size < 3 || problem_size > 15) {
      fprintf(stderr, "problem_size range: must be 3 <= n <= 12\n");
      exit(1);
   }

   sample();

   int result = 0;
   while (iterations > 0) {
     unsigned long start = microseconds();
     result += fannkuch(problem_size);
     unsigned long elapsed = microseconds() - start;
     printf("Fannkuch: iterations=1 runtime: %lu%s\n", elapsed, "us");
     iterations--;
   }

   return 0;
}
