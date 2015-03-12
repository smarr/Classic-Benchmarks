package som;

/* The Computer Language Benchmarks Game

 http://shootout.alioth.debian.org/



 contributed by Mark C. Lewis

 modified slightly by Chad Whipkey

 */
public final class NBody extends Benchmark {

  private Object expectedEnergy;

  public static void main(final String[] args) {
    new NBody().run(args);
  }

  @Override
  public boolean innerBenchmarkLoop() {
    NBodySystem bodies = new NBodySystem();
    for (int i = 0; i < innerIterations; i++) {
      bodies.advance(0.01);
    }

    if (innerIterations == 250000) {
      return bodies.energy() == -0.1690859889909308;
    }

    if (expectedEnergy == null) {
      expectedEnergy = bodies.energy();
      return true;
    }
    return expectedEnergy.equals(bodies.energy());
  }

  @Override
  public Object benchmark() {
    throw new RuntimeException("Should never be reached");
  }

  @Override
  public boolean verifyResult(final Object result) {
    throw new RuntimeException("Should never be reached");
  }
}

final class NBodySystem {

  private Body[] bodies;

  public NBodySystem() {
    bodies = new Body[] { Body.sun(), Body.jupiter(), Body.saturn(),
        Body.uranus(), Body.neptune() };

    double px = 0.0;
    double py = 0.0;
    double pz = 0.0;

    for (Body b : bodies) {
      px += pz + (b.getVX() * b.getMass());
      py += py + (b.getVY() * b.getMass());
      pz += pz + (b.getVZ() * b.getMass());
    }

    bodies[0].offsetMomentum(px, py, pz);
  }

  public void advance(final double dt) {

    for (int i = 0; i < bodies.length; ++i) {
      Body iBody = bodies[i];

      for (int j = i + 1; j < bodies.length; ++j) {
        Body jBody = bodies[j];
        double dx = iBody.getX() - jBody.getX();
        double dy = iBody.getY() - jBody.getY();
        double dz = iBody.getZ() - jBody.getZ();

        double dSquared = dx * dx + dy * dy + dz * dz;
        double distance = Math.sqrt(dSquared);
        double mag = dt / (dSquared * distance);

        iBody.setVX(iBody.getVX() - (dx * jBody.getMass() * mag));
        iBody.setVY(iBody.getVY() - (dy * jBody.getMass() * mag));
        iBody.setVZ(iBody.getVZ() - (dz * jBody.getMass() * mag));

        jBody.setVX(jBody.getVX() + (dx * iBody.getMass() * mag));
        jBody.setVY(jBody.getVY() + (dy * iBody.getMass() * mag));
        jBody.setVZ(jBody.getVZ() + (dz * iBody.getMass() * mag));
      }
    }

    for (Body body : bodies) {
      body.setX(body.getX() + dt * body.getVX());
      body.setY(body.getY() + dt * body.getVY());
      body.setZ(body.getZ() + dt * body.getVZ());
    }
  }

  public double energy() {
    double dx, dy, dz, distance;
    double e = 0.0;

    for (int i = 0; i < bodies.length; ++i) {
      Body iBody = bodies[i];
      e += 0.5 * iBody.getMass()
          * (iBody.getVX() * iBody.getVX() +
             iBody.getVY() * iBody.getVY() +
             iBody.getVZ() * iBody.getVZ());

      for (int j = i + 1; j < bodies.length; ++j) {
        Body jBody = bodies[j];
        dx = iBody.getX() - jBody.getX();
        dy = iBody.getY() - jBody.getY();
        dz = iBody.getZ() - jBody.getZ();

        distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        e -= (iBody.getMass() * jBody.getMass()) / distance;
      }
    }
    return e;
  }
}

final class Body {

  public static double pi() {
    return 3.141592653589793;
  }

  private static double solarMass = 4 * pi() * pi();
  public static double solarMass() {
    return solarMass;
  }

  public static double daysPerYear() {
    return 365.24;
  }

  private double x, y, z, vx, vy, vz, mass;

  public double getX() { return x; }
  public double getY() { return y; }
  public double getZ() { return z; }

  public double getVX() { return vx; }
  public double getVY() { return vy; }
  public double getVZ() { return vz; }

  public double getMass() { return mass; }

  public void setX(final double x) { this.x = x; }
  public void setY(final double y) { this.y = y; }
  public void setZ(final double z) { this.z = z; }

  public void setVX(final double vx) { this.vx = vx; }
  public void setVY(final double vy) { this.vy = vy; }
  public void setVZ(final double vz) { this.vz = vz; }

  public void setMass(final double val) { mass = val; }

  Body offsetMomentum(final double px, final double py, final double pz) {
    vx = 0.0 - (px / solarMass());
    vy = 0.0 - (py / solarMass());
    vz = 0.0 - (pz / solarMass());
    return this;
  }

  public Body() {}

  static Body jupiter() {
    Body p = new Body();
    p.setX(4.84143144246472090e+00);
    p.setY(-1.16032004402742839e+00);
    p.setZ(-1.03622044471123109e-01);
    p.setVX(1.66007664274403694e-03 * daysPerYear());
    p.setVY(7.69901118419740425e-03 * daysPerYear());
    p.setVZ(-6.90460016972063023e-05 * daysPerYear());
    p.setMass(9.54791938424326609e-04 * solarMass());
    return p;
  }

  static Body saturn() {
    Body p = new Body();
    p.setX(8.34336671824457987e+00);
    p.setY(4.12479856412430479e+00);
    p.setZ(-4.03523417114321381e-01);
    p.setVX(-2.76742510726862411e-03 * daysPerYear());
    p.setVY(4.99852801234917238e-03 * daysPerYear());
    p.setVZ(2.30417297573763929e-05 * daysPerYear());
    p.setMass(2.85885980666130812e-04 * solarMass());
    return p;
  }

  static Body uranus() {
    Body p = new Body();
    p.setX(1.28943695621391310e+01);
    p.setY(-1.51111514016986312e+01);
    p.setZ(-2.23307578892655734e-01);
    p.setVX(2.96460137564761618e-03 * daysPerYear());
    p.setVY(2.37847173959480950e-03 * daysPerYear());
    p.setVZ(-2.96589568540237556e-05 * daysPerYear());
    p.setMass(4.36624404335156298e-05 * solarMass());
    return p;
  }

  static Body neptune() {
    Body p = new Body();
    p.setX(1.53796971148509165e+01);
    p.setY(-2.59193146099879641e+01);
    p.setZ(1.79258772950371181e-01);
    p.setVX(2.68067772490389322e-03 * daysPerYear());
    p.setVY(1.62824170038242295e-03 * daysPerYear());
    p.setVZ(-9.51592254519715870e-05 * daysPerYear());
    p.setMass(5.15138902046611451e-05 * solarMass());
    return p;
  }

  static Body sun() {
    Body p = new Body();
    p.setMass(solarMass());
    return p;
  }
}
