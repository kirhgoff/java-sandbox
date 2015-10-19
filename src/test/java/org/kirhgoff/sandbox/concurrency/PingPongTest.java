package org.kirhgoff.sandbox.concurrency;

import org.testng.annotations.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author <a href="mailto:kirill.lastovirya@moex.com">Kirill Lastovirya</a>
 */
public class PingPongTest {
  @Test
  public void testPingPong() throws Exception {
    StringBuilder sb = new StringBuilder();
    runPingPong(sb);
    Thread.sleep(500);
    assertThat(sb.toString()).isEqualTo("Ping\nPong\nPing\nPong\nPing\nPong\n");
  }

  private void runPingPong(StringBuilder out) throws InterruptedException {
    new Thread(new PingPong(out, "Pong\n")).start();
    new Thread(new PingPong(out, "Ping\n")).start();
  }

  private class PingPong implements Runnable {
    private final StringBuilder sb;
    private final String text;
    private int counter = 0;

    public PingPong(StringBuilder sb, String text) {
      this.sb = sb;
      this.text = text;
    }

    @Override
    public void run() {
      while (counter < 3) {
        try {
          synchronized (sb) {
            if (text.startsWith("Pong")) {
              while (!sb.toString().endsWith(text)) {
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
    }
  }
}
