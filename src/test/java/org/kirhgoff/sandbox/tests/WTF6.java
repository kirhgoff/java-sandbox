package org.kirhgoff.sandbox.tests;

/**
 * Created by Kirill Lastovirya (kirill.lastovirya@gmail.com) aka kirhgoff on 04/12/15.
 */
public class WTF6 {
  public void method(Object o) {
    System.out.println("Object");
  }
  public void method(java.io.FileNotFoundException f) {
    System.out.println("FileNotFoundException");
  }
  public void method(java.io.IOException i) {
    System.out.println("IOException");
  }

  public static void main(String args[]) {
    WTF6 test = new WTF6();
    test.method(null);
  }
}
