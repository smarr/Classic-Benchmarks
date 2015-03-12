package som;

import java.util.Arrays;


public class Fannkuch extends Benchmark {

  private int[]   timesRotated;
  private int[]   perm;
  private boolean atEnd;

  private int pfannkuchen(final int[] anArray) {
    int first;
    int complement;
    int k = 0;
    int a;
    int b;

    while (!((first = anArray[0]) == 1)) {
      k++;
      complement = first - 1;

      for (int i = 0; i < first / 2; i++) {
        a = anArray[i];
        b = anArray[complement - i];
        anArray[i] = b;
        anArray[complement - i] = a;
      }
    }
    return k;
  }

  private void initialize(final int size) {
    perm = new int[size];
    Arrays.setAll(perm, v -> v + 1);

    timesRotated = new int[size];
    atEnd = false;
  }

  public void makeNext() {
    int temp;
    int remainder;

    // Generate the next permutation
    for (int r = 1; r < perm.length; r++) {
      // Rotate the first r items to the left.
      temp = perm[0];
      for (int i = 0; i <= r - 1; i++) { perm[i] = perm[i + 1]; };

      perm[r] = temp;

      timesRotated[r] = (timesRotated[r] + 1) % (r + 1);

      remainder = timesRotated[r];

      if (remainder != 0) { return; }

      // After r rotations, the first r items are in their original positions.
      // Go on rotating the first r+1 items.
    }

    // We are past the final permutation.
    atEnd = true;
  }

  public int maxPfannkuchen() {
    int max = 0;
    int[] permutation;

    while (!atEnd()) {
      permutation = next();
      max = Math.max(max, pfannkuchen(permutation));
    }
    return max;
  }

  public boolean atEnd() {
    return atEnd;
  }

  public int[] next() {
    int[] result = perm.clone();
    makeNext();
    return result;
  }

  @Override
  public boolean innerBenchmarkLoop() {
    initialize(innerIterations);
    int result = maxPfannkuchen();
    return result == expectedResult(innerIterations);
  }

  @Override
  public Object benchmark() {
    throw new RuntimeException("Should never be reached");
  }

  public static void main(final String[] args) {
    new Fannkuch().run(args);
  }

  @Override
  public boolean verifyResult(final Object result) {
    throw new RuntimeException("Should never be reached");
  }

  private static int[] results;
  private static int expectedResult(final int problemSize) {
    if (results == null) {
      results = new int[]{ 0, 1, 2, 4, 7, 10, 16, 22, 30, 38, 51, 65};
    }
    return results[problemSize - 1];
  }
}
