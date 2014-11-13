package som;

import java.util.Arrays;

public class Sieve extends Benchmark {

	@Override
	public Object benchmark() {
		boolean[] flags = new boolean[5000];
		int result = sieve(flags, 5000);
		if (result != 669) {
			error("Wrong result " + result + " should be: 669");
		}
		return null;
	}
	 	    
    int sieve(boolean[] flags, int size) {
        int primeCount = 0;
        Arrays.fill(flags, true);
        
        for (int i = 2; i <= size; i++) {
            if (flags[i - 1]) {
                primeCount++;
                int k = i + i;
                while (k <= size) {
                    flags[k - 1] = false;
                    k += i;
                }
            }
        }
        return primeCount;
    }
    
	public static void main(String[] args) {
		new Sieve().run(args);
	}
}
