package som;


public class TreeSort extends Sort {

  private static class TreeNode {
    private TreeNode left;
    private TreeNode right;
    private int value;

    public TreeNode(final int value) {
      this.value = value;
    }

    public boolean check() {
      return (left  == null || (left.getValue()  <  value && left.check())) &&
             (right == null || (right.getValue() >= value && right.check()));
    }

    public int getValue() {
      return value;
    }

    public void insert(final int n) {
      if (n < value) {
        if (left == null) {
          left = new TreeNode(n);
        } else {
          left.insert(n);
        }
      } else {
        if (right == null) {
          right = new TreeNode(n);
        } else {
          right.insert(n);
        }
      }
    }
  }

  @Override
  public Object benchmark() {
    int[] array = randomArray(1000);
    TreeNode tree = null;

    for (int i = 0; i < array.length; i++) {
      if (i == 0) {
        tree = new TreeNode(array[i]);
      } else {
        tree.insert(array[i]);
      }
    }

    if (!tree.check()) {
      error("Invalid result, tree not sorted");
    }
    return null;
  }

  @Override
  public int dataSize() {
    throw new RuntimeException("Should not be executed");
  }

  @Override
  public void sort(final int[] array) {
    throw new RuntimeException("Should not be executed");
  }

  public static void main(final String[] args) {
    new TreeSort().run(args);
  }
}
