package org.kirhgoff.sandbox.tests;

/**
 * Created by Kirill Lastovirya (kirill.lastovirya@gmail.com) aka kirhgoff on 23/11/15.
 */
public class WTF1 {
  public static void main(String args[]) {
    int i,j,k,l=0;
    k = l++;
    j = ++k;
    i = j++;
    System.out.println(i);
  }
}
