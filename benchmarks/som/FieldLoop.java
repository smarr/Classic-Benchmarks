package som;

public class FieldLoop extends Benchmark {
  private int counter;

  @Override
  public Object benchmark() {
    counter = 0;
    int iter = 20000;

    while (iter > 0) {
      iter = iter - 1;
      counter = counter + 1;
      counter = counter + 1;
      counter = counter + 1;
      counter = counter + 1;
      counter = counter + 1;

      counter = counter + 1;
      counter = counter + 1;
      counter = counter + 1;
      counter = counter + 1;
      counter = counter + 1;

      counter = counter + 1;
      counter = counter + 1;
      counter = counter + 1;
      counter = counter + 1;
      counter = counter + 1;

      counter = counter + 1;
      counter = counter + 1;
      counter = counter + 1;
      counter = counter + 1;
      counter = counter + 1;

      counter = counter + 1;
      counter = counter + 1;
      counter = counter + 1;
      counter = counter + 1;
      counter = counter + 1;

      counter = counter + 1;
      counter = counter + 1;
      counter = counter + 1;
      counter = counter + 1;
      counter = counter + 1;
    }
    return counter;
  }

  public static void main(final String[] args) {
    new FieldLoop().run(args);
  }

  @Override
  public boolean verifyResult(final Object result) {
    return result.equals(600000);
  }
}
