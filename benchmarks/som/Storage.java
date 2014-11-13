package som;

import java.util.Arrays;

public class Storage extends Benchmark {
	
	int count;

	@Override
	public Object benchmark() {
		Random.initialize();
		count = 0;
		
		buildTreeDepth(7);
		
		if (count != 5461) {
			error("Wrong result: " + count + " should be 5461");
		}
		return null;
	}
	
	private Object buildTreeDepth(int depth) {
		count++;
		if (depth == 1) {
			return new Object[Random.next() % 10 + 1];
		} else {
			Object[] arr = new Object[4];
			Arrays.setAll(arr, v -> buildTreeDepth(depth - 1));
			return arr;
		}
	}

	public static void main(String[] args) {
		new Storage().run(args);
	}
}
