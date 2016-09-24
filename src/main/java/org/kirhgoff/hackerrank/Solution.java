package org.kirhgoff.hackerrank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Solution {

  public static void main(String[] args) {
    try {
      process();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void process() throws IOException {
    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

    int [] info = parseLine(input.readLine());
    int arraySize = info[0];
    int queriesCount = info[1];

    int [] inputArray = parseLine(input.readLine());
    if (arraySize != inputArray.length)
      throw new IllegalArgumentException("Inconsistent input: array size");

    int [][] queries = new int [queriesCount][3];
    for (int i = 0; i < queriesCount; i++) {
      queries[i] = parseLine(input.readLine());
    }

    for (int i = 0; i < queriesCount; i++) {
      processQuery(queries[i], inputArray);
    }

    printResults(inputArray);
  }

  private static void printResults(int[] inputArray) {
    //Print results
    System.out.println(Math.abs (inputArray[0] - inputArray[inputArray.length - 1]));
    for (int i = 0; i < inputArray.length; i++) {
      if (i != 0) System.out.print(" " + inputArray[i]);
      else System.out.print(inputArray[i]);
    }
    System.out.println();
  }

  public static void processQuery(int[] query, int[] inputArray) {
    int command = query[0];
    int start = query[1] - 1;
    int end = query[2] - 1;

    if (command == 1) moveHead(inputArray, start, end);
    else if (command == 2) moveTail(inputArray, start, end);
    else throw new IllegalArgumentException("Incorrect query number");
  }

  private static int [] parseLine(String str) {
    StringTokenizer st = new StringTokenizer(str.trim(), " ");
    List<Integer> list = new LinkedList<>();
    while(st.hasMoreTokens()) {
      list.add(Integer.parseInt(st.nextToken().trim()));
    }
    int counter = 0;
    int [] result = new int [list.size()];
    for (Integer value : list) {
      result[counter] = value;
      counter ++;
    }
    return result;
  }

  private static void moveHead(int[] array, int i, int j) {
    if (i == 0) return;
    int [] temp = Arrays.copyOfRange(array, 0, i);
    System.arraycopy(array, i, array, 0, j - i + 1);
    System.arraycopy(temp, 0, array, j - i + 1, temp.length);
  }

  private static void moveTail(int[] array, int i, int j) {
    if (j == array.length - 1) return;
    int [] temp = Arrays.copyOfRange(array, j + 1, array.length);
    System.arraycopy(array, i, array, array.length - (j - i + 1), (j - i + 1));
    System.arraycopy(temp, 0, array, i, temp.length);
  }

}