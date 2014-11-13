package som;

public class Permute extends Benchmark {
	private int count;
	private int[] v;

	@Override
	public Object benchmark() {
        count = 0;
        v     = new int[7];
        permute(6);
        if (count != 8660) {
        	error("Wrong result: " + count + " should be: 8660");
        }
        return null;
	}
    
    void permute(int n) {
        count++;
        if (n != 0) {
            permute(n - 1);
            for (int i = n; i >= 1; i--) {
            	swap(n, i);
                permute(n - 1);
                swap(n, i);
            }
        }
    }
    
    void swap(int i, int j) {
        int tmp = v[i];
        v[i] = v[j];
        v[j] = tmp;
    }
	
	public static void main(String[] args) {
		new Permute().run(args);
	}
}
