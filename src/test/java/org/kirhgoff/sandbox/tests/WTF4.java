package org.kirhgoff.sandbox.tests;

/**
 * Created by Kirill Lastovirya (kirill.lastovirya@gmail.com) aka kirhgoff on 23/11/15.
 */
public class WTF4 {
  class Test1
  {
    public int value;
    public int hashCode() { return 42; }
  }
  class Test2
  {
    public int value;
    public int hashcode() { return (int)(value^5); }
  }
}