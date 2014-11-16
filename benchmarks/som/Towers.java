package som;

public class Towers extends Benchmark {

  static class TowersDisk {
    private int size;
    private TowersDisk next;

    public TowersDisk(final int size) {
      this.size = size;
    }

    public int  getSize()          { return size;  }
    public void setSize(final int value) { size = value; }

    public TowersDisk getNext()           { return next;  }
    public void setNext(final TowersDisk value) { next = value; }
  }

  private TowersDisk[] piles;
  private int movesDone;

  void pushDisk(final TowersDisk disk, final int pile) {
    TowersDisk top = piles[pile];
    if (!(top == null) && (disk.getSize() >= top.getSize())) {
      error("Cannot put a big disk on a smaller one");
    }

    disk.setNext(top);
    piles[pile] = disk;
  }

  TowersDisk popDiskFrom(final int pile) {
    TowersDisk top;

    top = piles[pile];
    if (top == null) {
      error("Attempting to remove a disk from an empty pile");
    }

    piles[pile] = top.getNext();
    top.setNext(null);
    return top;
  }

  void moveTopDisk(final int fromPile, final int toPile) {
    pushDisk(popDiskFrom(fromPile), toPile);
    movesDone++;
  }

  void buildTowerAt(final int pile, final int disks) {
    for (int i = disks; i >= 0; i--) {
      pushDisk(new TowersDisk(i), pile);
    }
  }

  void moveDisks(final int disks, final int fromPile, final int toPile) {
    if (disks == 1) {
      moveTopDisk(fromPile, toPile);
    } else {
      int otherPile;
      otherPile = (6 - fromPile) - toPile;
      moveDisks(disks - 1, fromPile, otherPile);
      moveTopDisk(fromPile, toPile);
      moveDisks(disks - 1, otherPile, toPile);
    }
  }

  @Override
  public Object benchmark() {
    piles = new TowersDisk[4];
    buildTowerAt(1, 13);
    movesDone = 0;
    moveDisks(13, 1, 2);
    if (movesDone != 8191) {
      error("Error in result: " + movesDone + " should be: 8191");
    }
    return null;
  }

  public static void main(final String[] args) {
    new Towers().run(args);
  }
}
