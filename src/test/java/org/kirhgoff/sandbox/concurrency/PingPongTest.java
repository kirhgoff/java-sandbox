package org.kirhgoff.sandbox.concurrency;

import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author <a href="mailto:kirill.lastovirya@moex.com">Kirill Lastovirya</a>
 */
public class PingPongTest {
  @Test
  public void testPingPong() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(baos);
    runPingPong(out);
    Thread.sleep(500);
    out.flush();
    assertThat (baos.toString("UTF-8")).isEqualTo("Ping\nPong\nPing\nPong\nPing\nPong\n");
  }

  private void runPingPong(PrintStream out) {
    new Thread(new PingPong(out, "Ping")).start();
    new Thread(new PingPong(out, "Pong")).start();
  }

  private class PingPong implements Runnable {
    private final PrintStream out;
    private final String text;
    private int counter = 0;

    public PingPong(PrintStream out, String text) {
      this.out = out;
      this.text = text;
    }

    @Override
    public void run() {
      if (text.equals("Pong")) {
        synchronized (out) {
          try {
            out.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
      while (counter < 3) {
        synchronized (out) {
          try {

            System.out.println(text);
            out.println(text);
            counter ++;
            out.notify();
            out.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }
}
