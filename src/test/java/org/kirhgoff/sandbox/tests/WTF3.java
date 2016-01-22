package org.kirhgoff.sandbox.tests;

/**
 * Created by Kirill Lastovirya (kirill.lastovirya@gmail.com) aka kirhgoff on 23/11/15.
 */
public class WTF3
{
  public void foo()
  {
    assert false; /* Line 5 */
    assert false; /* Line 6 */
  }
  public void bar()
  {
    while(true)
    {
      assert false; /* Line 12 */
    }
    //assert false;  /* Line 14 */
  }
}