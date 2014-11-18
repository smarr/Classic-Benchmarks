package som;

import som.richards.RBObject;
import som.richards.RichardsBenchmarks;

/*
 * This version is a port of the SOM Richards benchmark to Java.
 * It is kept as close to the SOM version as possible, for cross-language
 * benchmarking.
 */

public class Richards extends Benchmark {

  public static void main(final String[] args) {
    new Richards().run(args);
  }

  @Override
  public Object benchmark() {
    RBObject.initialize();
    (new RichardsBenchmarks()).reBenchStart();
    return null;
  }
}
