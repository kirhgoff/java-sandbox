package org.kirhgoff.sandbox.concurrency;

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

    return new Runnable [] {new PingPongLocks(sb, ping, lock), new PingPongLocks(sb, pong, lock)};
  }

  private final StringBuilder sb;
  private final String text;
  private Lock lock;
  private int counter = 0;

  public PingPongLocks(StringBuilder sb, String text, Lock lock) {
    this.sb = sb;
    this.text = text;
    this.lock = lock;
  }

  @Override
  public void run() {
    while (counter < 3) {
      lock.lock();
      try {
        System.out.print("Appending " + text);
        sb.append(text);
        counter++;
      } finally {
        lock.unlock();
      }
    }
    System.out.print("Exiting " + text);
  }
}
