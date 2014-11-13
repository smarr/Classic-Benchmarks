package som;

public class Random {
	private int seed = 74755;
	
	private int getNext() {
		seed = ((seed * 1309) + 13849) & 65535;
		return seed;
	}
	
	private static Random random = new Random();
	
	public static int next() {
		return random.getNext();
	}
	
	public static void initialize() {
		random = new Random();
	}
	
	public static Random random() {
		return random;
	}
	
	
	public static void main(String[] args) {
		System.out.println("Testing random number generator ...");
		
		try {
			if (next() != 22896) { throw new RuntimeException(); }
			if (next() != 34761) { throw new RuntimeException(); }
			if (next() != 34014) { throw new RuntimeException(); }
			if (next() != 39231) { throw new RuntimeException(); }
			if (next() != 52540) { throw new RuntimeException(); }
			if (next() != 41445) { throw new RuntimeException(); }
			if (next() !=  1546) { throw new RuntimeException(); }
			if (next() !=  5947) { throw new RuntimeException(); }
			if (next() != 65224) { throw new RuntimeException(); }
		} catch (RuntimeException e) {
			System.err.println("FAILED");
			return;
		}
	}
}
