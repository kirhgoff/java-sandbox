package org.kirhgoff.hackerrank;

/**
 * Created by kirilllastovirya on 26/03/2016.
 */
import java.io.*;
import java.util.*;

public class Palindrome {

  public static void main(String[] args) throws Exception {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    //Fail fast
    Integer fixturesCount = Integer.parseInt(reader.readLine());
    System.out.println(fixturesCount);
    for (int i = 0; i < fixturesCount; i ++) {
      String line = reader.readLine();
      System.out.println("line: " + line);
      int result = printRequiredIndex(line);
      System.out.println(result);
    }
  }

  public static int printRequiredIndex(String string) {
    System.out.println(string);
    int left = 0;
    int right = string.length() - 1;
    int currentResult = -1;
    //TODO optimization: convert to array
    while (left <= right) {
      char leftChar = string.charAt(left);
      char rightChar = string.charAt(right);
      System.out.println("left: " + left + " right: " + right + " leftChar: " + leftChar + " rightChar: " + rightChar);
      if (leftChar != rightChar) {
        if (rightChar != string.charAt(left + 1)) {
          currentResult = right;
          right -= 1;
          System.out.println("Skipping left one");
        } else {
          currentResult = left;
          left += 1;
          System.out.println("Skipping right one");
        }
      }
      left ++;
      right --;
    }
    return currentResult;
  }
}