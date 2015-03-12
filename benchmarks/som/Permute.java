package som;

public class Permute extends Benchmark {
  private int count;
  private int[] v;

  @Override
  public Object benchmark() {
    count = 0;
    v     = new int[7];
    permute(6);
    return count;
  }

  void permute(final int n) {
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

  void swap(final int i, final int j) {
    int tmp = v[i];
    v[i] = v[j];
    v[j] = tmp;
  }

  public static void main(final String[] args) {
    new Permute().run(args);
  }

  @Override
  public boolean verifyResult(final Object result) {
    return assertEquals(8660, result);
  }
}
