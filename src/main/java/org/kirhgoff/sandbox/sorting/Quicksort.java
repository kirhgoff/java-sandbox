package org.kirhgoff.sandbox.sorting;

/**
 * Created by kirilllastovirya on 24/09/2016.
 */
public class Quicksort {

  public static int[] sort(int[] array) {
    return sortInternal(array, 0, array.length - 1);
  }

  private static int[] sortInternal(
      int[] array,
      int initialLeftIndex,
      int initialRightIndex)
  {
    if (initialLeftIndex >= initialRightIndex) {
      return array;
    }

    int leftIndex = initialLeftIndex;
    int rightIndex = initialRightIndex;

    int middleIndex = (rightIndex - leftIndex)/2;
    int middleValue = array[middleIndex];

    while(leftIndex < middleIndex || rightIndex > middleIndex) {
      while (leftIndex < middleIndex && array[leftIndex] < middleValue) {
        leftIndex++;
      }

      while (rightIndex > middleIndex && array[rightIndex] >= middleValue) {
        rightIndex--;
      }

      swap(array, leftIndex, rightIndex);

      if (leftIndex < middleIndex) leftIndex++;
      if (rightIndex > middleIndex) rightIndex--;
    }

    sortInternal(array, leftIndex, middleIndex);
    sortInternal(array, middleIndex, rightIndex);

    return array;
  }

  private static void swap(int [] array, int leftIndex, int rightIndex) {
    int temp = array[leftIndex];
    array[leftIndex] = array[rightIndex];
    array[rightIndex] = temp;
  }
}
