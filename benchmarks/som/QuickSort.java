package som;

public class QuickSort extends Sort {

  @Override
  public int dataSize() {
    return 800;
  }

  @Override
  public void sort(final int[] array) {
    sort(array, 0, dataSize() - 1);
  }

  void sort(final int[] array, final int low, final int high) {
    int pivot = array[(low + high) / 2];
    int i = low;
    int j = high;
    while (i <= j) {
      while (array[i] < pivot) { i++; }
      while (pivot < array[j]) { j--; }
      if (i <= j) {
        int tmp  = array[i];
        array[i] = array[j];
        array[j] = tmp;
        i++;
        j--;
      }
    }

    if (low < j)  { sort(array, low, j);  }
    if (i < high) { sort(array, i, high); }
  }

  public static void main(final String[] args) {
    new QuickSort().run(args);
  }
}
