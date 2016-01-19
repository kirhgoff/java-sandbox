package org.kirhgoff.sandbox.concurrency;

/**
 * @author <a href="mailto:kirill.lastovirya@moex.com">Kirill Lastovirya</a>
 */
class PingPongSynchronized implements Runnable{
  public static String PING;
  public static String PONG;

  public static Runnable [] create (StringBuilder sb, String ping, String pong) {
    PING = ping;
    PONG = pong;

    return new Runnable [] {new PingPongSynchronized(sb, ping), new PingPongSynchronized(sb, pong)};
  }

  private final StringBuilder sb;
  private final String text;
  private int counter = 0;

  public PingPongSynchronized(StringBuilder sb, String text) {
    this.sb = sb;
    this.text = text;
  }

  @Override
  public void run() {
    while (counter < 3) {
      try {
        synchronized (sb) {
          if (text.startsWith(PONG)) {
            while (sb.toString().isEmpty()) {
              System.out.print("Waiting for not " + text);
              sb.wait();
            }
          }
        }

        synchronized (sb) {
          System.out.print("Appending " + text);
          sb.append(text);
          counter++;
          sb.notifyAll();
          sb.wait();
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    if (text.startsWith(PING)) {
      System.out.print("Notify from " + text);
      synchronized (sb) {
        sb.notifyAll();
      }
    }
    System.out.print("Exiting " + text);
  }
}
