package org.kirhgoff.sandbox.tests;

import java.util.function.Function;

/**
 * Created by Kirill Lastovirya (kirill.lastovirya@gmail.com) aka kirhgoff on 04/12/15.
 */
class X {
  public static void main(String[] a) {
    try {
      short s = 1000;
      byte b = (byte)s;
      System.out.println("b = " + b);
      }
    catch (Exception e) { System.out.println("TROUBLE!"); }
    }
  }