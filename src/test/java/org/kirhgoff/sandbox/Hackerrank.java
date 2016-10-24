package org.kirhgoff.sandbox;

import org.kirhgoff.hackerrank.Solution;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by kirilllastovirya on 15/02/2016.
 */
public class Hackerrank {
  @Test
  public void testProcessQuery () {
    int [] array = new int [] {1,2,3,4,5,6,7,8};
    int [] query = new int [] {1, 2, 4};
    Solution.processQuery(query, array);
    assertThat(array).isEqualTo(new int [] {2,3,4,1,5,6,7,8});
  }
}
