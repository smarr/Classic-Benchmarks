package som;

public class IntegerLoop extends Benchmark {

  @Override
  public Object benchmark() {
    int bounds = 20000;
    int a = 0;
    for (int i = -bounds; i <= bounds; i++) {
      a = i - i;
    }
    return a;
  }

  public static void main(final String[] args){
    new IntegerLoop().run(args);
  }

  @Override
  public boolean verifyResult(final Object result) {
    return result.equals(0);
  }
}
