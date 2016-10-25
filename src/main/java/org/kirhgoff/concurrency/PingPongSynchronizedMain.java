package org.kirhgoff.concurrency;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:kirill.lastovirya@moex.com">Kirill Lastovirya</a>
 */
public class PingPongSynchronizedMain {

  public static void main(String[] args) {
    final Object lock = new Object();
    final AtomicBoolean isPing = new AtomicBoolean(true);
    Stream.of("Pong", "Ping")
        .map(name -> new PingPong(name, lock, isPing))
        .forEach(pp -> new Thread(pp).start());
  }

  private static class PingPong implements Runnable {
    private final String text;
    private final Object lock;
    private final AtomicBoolean isPing;

    PingPong(String text, Object lock, AtomicBoolean isPing) {
      this.text = text;
      this.lock = lock;
      this.isPing = isPing;
    }

    @Override
    public void run() {
      int counter = 3;
      //Pong should wait
      synchronized (lock) {
        try {
          while (isPing() && iAmPong()) {
            lock.wait();
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }

      while (counter-- > 0) {
        synchronized (lock){
          System.out.println(text);

          isPing.set(iAmPong()); //next one
          lock.notifyAll();
          try {
            while((isPing() && iAmPong()) || (!isPing() && !iAmPong())) {
              lock.wait();
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      }

      synchronized (lock) {
        if (!iAmPong()) {
          isPing.set(false);
          lock.notifyAll();
        }
      }
    }

    private boolean isPing() {
      return isPing.get();
    }

    private boolean iAmPong() {
      return text.equals("Pong");
    }
  }
}