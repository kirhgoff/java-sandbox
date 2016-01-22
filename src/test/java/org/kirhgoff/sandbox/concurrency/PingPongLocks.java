package org.kirhgoff.sandbox.concurrency;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author <a href="mailto:kirill.lastovirya@moex.com">Kirill Lastovirya</a>
 */
public class PingPongLocks implements Runnable{
  public static String PING;
  public static String PONG;

  public static Runnable [] create (StringBuilder sb, String ping, String pong) {
    PING = ping;
    PONG = pong;
    Lock lock = new ReentrantLock();
    Condition pingCondition = lock.newCondition();
    Condition pongCondition = lock.newCondition();

    return new Runnable [] {
        new PingPongLocks(sb, ping, lock, pingCondition, pongCondition),
        new PingPongLocks(sb, pong, lock, pongCondition, pingCondition)
    };
  }

  private final StringBuilder sb;
  private final String text;
  private final Lock lock;
  private final Condition thisCondition;
  private final Condition otherCondition;
  private int counter = 0;

  public PingPongLocks(
      StringBuilder sb,
      String text,
      Lock lock,
      Condition thisCondition,
      Condition otherCondition
  )
  {
    this.sb = sb;
    this.text = text;
    this.lock = lock;
    this.thisCondition = thisCondition;
    this.otherCondition = otherCondition;
  }

  @Override
  public void run() {
    try {
      while (counter < 3) {
        if (text.startsWith(PONG)) {
          while (sb.toString().isEmpty()) {
            System.out.print("Waiting for not " + text);
            otherCondition.wait();
          }
        }

        lock.lock();
        try {
          System.out.print("Appending " + text);
          sb.append(text);
          counter++;
          otherCondition.signal();
          thisCondition.await();
        } finally {
          lock.unlock();
        }
      }

      if (text.startsWith(PING)) {
        System.out.print("Notify from " + text);
        lock.lock();
        try {
          otherCondition.signal();
        } finally {
          lock.unlock();
        }
      }

      System.out.print("Exiting " + text);
    } catch(InterruptedException e) {
      e.printStackTrace();
    }
  }
}
