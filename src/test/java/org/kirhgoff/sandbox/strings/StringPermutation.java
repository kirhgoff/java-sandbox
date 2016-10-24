package org.kirhgoff.sandbox.strings;

import com.google.common.base.Joiner;
import org.assertj.core.util.Lists;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

/**
 * Created by kirilllastovirya on 20/10/2016.
 */
public class StringPermutation {
  @Test
  public void testPermutate0() throws Exception {
    assertThat(permutate(new ArrayList<>()))
        .isEqualTo(Lists.emptyList());

    ArrayList<String> listWithNull = new ArrayList<String>() {{
      add(null);
    }};

    assertThat(permutate(listWithNull))
        .isEqualTo(listWithNull);

    try {
      permutate(null);
      fail("Should throw NPE");
    } catch (NullPointerException e) {}
  }


  @Test
  public void testPermutate1() throws Exception {
    assertThat(permutate(Arrays.asList("0")))
        .containsExactly("0");
  }

  @Test
  public void testPermutate2() throws Exception {
    assertThat(permutate(Arrays.asList("0", "1")))
        .containsExactly("01", "10");
  }

  @Test
  public void testPermutate3() throws Exception {
    assertThat(permutate(Arrays.asList("0", "1", "2")))
        .containsExactly("012", "021", "102", "120", "201", "210");
  }

  private List<String> permutate(List<String> source) {
    if (source == null) throw new NullPointerException();
    if (source.size() <= 1) return source;

    ArrayList<String> list = new ArrayList<>(source);
    Collections.sort(list);

    List<String> result = new ArrayList<>();
    result.add(Joiner.on("").join(list));

    while(true) {
      //find largest k : a[k] < a[k+1]
      int largestK = -1;
      for (int i = list.size() - 1; i > 0; i--) {
        if (larger(list.get(i), list.get(i - 1))) {
          largestK = i - 1;
          break;
        }
      }
      if (largestK == -1) break;

      //find largest p: a[largestK] < a[p]
      int largestP = largestK + 1;
      for (int i = list.size() - 1; i > largestK; i--) {
        if (larger(list.get(i), list.get(largestK))) { //TODO not optimal
          largestP = i;
          break;
        }
      }

      //swap largestK and largestP
      swap(list, largestK, largestP);

      //reverse collection from largestK + 1
      for (int i = largestK + 1, j = list.size() - 1; i < j; i ++, j --) {
        swap(list, i, j);
      }
      result.add(Joiner.on("").join(list));
    }
    return result;
  }

  private void swap(ArrayList<String> list, int largestK, int largestP) {
    String first = list.get(largestK);
    String second = list.get(largestP);
    list.set(largestK, second);
    list.set(largestP, first);
  }

  private boolean larger(String first, String second) {
    return first.compareTo(second) > 0;
  }


}
