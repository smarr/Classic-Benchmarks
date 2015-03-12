package som;

import java.util.Arrays;

public class Bounce extends Benchmark {

  static class Ball {
    private int x;
    private int y;
    private int xVel;
    private int yVel;

    public Ball() {
      x = Random.next() % 500;
      y = Random.next() % 500;
      xVel = (Random.next() % 300) - 150;
      yVel = (Random.next() % 300) - 150;
    }

    public boolean bounce() {
      int xLimit = 500;
      int yLimit = 500;
      boolean bounced = false;

      x += xVel;
      y += yVel;
      if (x > xLimit) { x = xLimit; xVel = 0 - Math.abs(xVel); bounced = true; }
      if (x < 0)      { x = 0;      xVel = Math.abs(xVel);     bounced = true; }
      if (y > yLimit) { y = yLimit; yVel = 0 - Math.abs(yVel); bounced = true; }
      if (y < 0)      { y = 0;      yVel = Math.abs(yVel);     bounced = true; }
      return bounced;
    }
  }

  @Override
  public Object benchmark() {
    Random.initialize();

    int ballCount = 100;
    int bounces   = 0;
    Ball[] balls  = new Ball[ballCount];

    Arrays.setAll(balls, v -> new Ball());

    for (int i = 1; i <= 50; i++) {
      for (Ball ball : balls) {
        if (ball.bounce()) {
          bounces++;
        }
      }
    }
    return bounces;
  }

  @Override
  public boolean verifyResult(final Object result) {
    return assertEquals(1331, result);
  }

  public static void main(final String[] args) {
    new Bounce().run(args);
  }
}
