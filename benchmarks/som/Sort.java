package som;

import java.util.Arrays;

public abstract class Sort extends Benchmark {

    protected int smallest;
    protected int largest;
    
    @Override
    public Object benchmark() {
        int[] array = randomArray(dataSize());
        sort(array);
        checkArray(array);
        return array;
    }
    
    void checkArray(int[] array) {
        if (array[0] != smallest || array[array.length - 1] != largest) {
        	error("Array is not sorted. smallest: " + smallest + " largest: " +
                  largest + " [0]: " + array[0] + " [l]: " + array[array.length - 1]);
        }
        
        for (int i = 2; i < array.length; i++) {
            if (array[i - 1] > array[i]) { 
                error("Array is not sorted. [" + i + " - 1]: " + array[i - 1] + " [" + i + "]: " + array[i]);
            }
        }
    }
    
    int[] randomArray(int size) {
        Random.initialize();
        int[] array = new int[size];
        Arrays.setAll(array, v -> Random.next());
        smallest = largest = array[0];
        for (int elm : array) {
            if (elm > largest)  { largest  = elm; };
            if (elm < smallest) { smallest = elm; };
        }
        return array;
    }
	
    public abstract int dataSize();
    public abstract void sort(int[] array);
}
