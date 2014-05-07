/* The Computer Language Benchmarks Game
   http://shootout.alioth.debian.org/

   contributed by Isaac Gouy
   converted to Java by Oleg Mazurov
*/

public class fannkuchredux
{
   public static int fannkuch(int n) {
      int[] perm = new int[n];
      int[] perm1 = new int[n];
      int[] count = new int[n];
      int maxFlipsCount = 0;
      int permCount = 0;
      int checksum = 0;

      for(int i=0; i<n; i++) perm1[i] = i;
      int r = n;

      while (true) {

         while (r != 1){ count[r-1] = r; r--; }

         for(int i=0; i<n; i++) perm[i] = perm1[i];
         int flipsCount = 0;
         int k;

         while ( !((k=perm[0]) == 0) ) {
            int k2 = (k+1) >> 1;
            for(int i=0; i<k2; i++) {
               int temp = perm[i]; perm[i] = perm[k-i]; perm[k-i] = temp;
            }
            flipsCount++;
         }

         maxFlipsCount = Math.max(maxFlipsCount, flipsCount);
         checksum += permCount%2 == 0 ? flipsCount : -flipsCount;

         // Use incremental change to generate another permutation
         while (true) {
            if (r == n) {
	             return checksum;
	          }
            int perm0 = perm1[0];
            int i = 0;
            while (i < r) {
               int j = i + 1;
               perm1[i] = perm1[j];
               i = j;
            }
            perm1[r] = perm0;

            count[r] = count[r] - 1;
            if (count[r] > 0) break;
            r++;
         }

         permCount++;
      }
   }

   public static void main(String[] args){
      int numIterations = Integer.valueOf(args[0]);
      int warmUp        = Integer.valueOf(args[1]);
      int problemSize   = Integer.valueOf(args[2]);

      if (fannkuch(9) != 8629) {
        System.err.println("fannkuch(9) failed, gave unexpected result.");
        System.exit(1);
      }

      for (int i = 0; i < warmUp; i++) {
        fannkuch(problemSize);
      }

      int result = 0;
      for (int i = 0; i < numIterations; i++) {
        long start = System.nanoTime();
        result += fannkuch(problemSize);
        long end = System.nanoTime();
        long microseconds = (end - start) / 1000;
        System.out.println("Fannkuch: iterations=1 runtime: " + microseconds + "us");
      }
   }
}
