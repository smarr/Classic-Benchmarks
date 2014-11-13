package som;

public class BubbleSort extends Sort {

	@Override
	public void sort(int[] array) {
        for (int i = array.length - 1; i >= 0; i--) {
        	for (int j = 0; j <= i - 1; j++) {
                int current = array[j];
                int next    = array[j + 1];
                if (current > next) {
                    array[j]     = next;
                    array[j + 1] = current;
                }
        	}
        }
	}
        
    @Override
    public int dataSize() { return 130; }
        
	public static void main(String[] args) {
		new BubbleSort().run(args);
	}
}
