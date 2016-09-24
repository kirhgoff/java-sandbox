package org.kirhgoff.sandbox.sorting;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by kirilllastovirya on 24/09/2016.
 */
public class QuicksortTest {
  @Test
  public void testSorting() {
    assertThat(Quicksort.sort(new int[]{})).containsExactly();
    assertThat(Quicksort.sort(new int[]{1})).containsExactly(1);
    assertThat(Quicksort.sort(new int[]{2, 1})).containsExactly(1, 2);
    assertThat(Quicksort.sort(new int[]{0, 666, 0})).containsExactly(0, 0, 666);
    assertThat(Quicksort.sort(new int[]{2, 2, 1, 4})).containsExactly(1, 2, 2, 4);

  }
}