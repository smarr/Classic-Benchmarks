package som;

public class IntegerLoop extends Benchmark {

  @Override
  public Object benchmark() {
    int bounds = 20000;
    for (int i = -bounds; i <= bounds; i++) {
      int a = i - i;
    }
    return null;
  }

  public static void main(String[] args){
    new IntegerLoop().run(args);
  }
}
