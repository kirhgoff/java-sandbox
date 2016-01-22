package org.kirhgoff.sandbox.tests;

/**
 * Created by Kirill Lastovirya (kirill.lastovirya@gmail.com) aka kirhgoff on 23/11/15.
 */
public class WTF2 {
  public static void main(String args[])
  {
    for(int i = 0; i < 3; i++)
    {
      for(int j = 3; j >= 0; j--)
      {
        if(i == j) break;
        System.out.println(i + " " + j);
      }
    }
  }
}
