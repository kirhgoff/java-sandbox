package org.kirhgoff.sandbox.concurrency;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author <a href="mailto:kirill.lastovirya@moex.com">Kirill Lastovirya</a>
 */
public class PingPongTest {

  @Test
  public void testPingPongSynchronized() throws Exception {
    checkImplementation(PingPongSynchronized::create);
  }

  @Test(enabled = false)
  public void testPingPongLocks() throws Exception {
    checkImplementation(PingPongLocks::create);
  }


  private void checkImplementation(TriFunction<StringBuilder, String, Runnable[]> factory) {
    StringBuilder sb = new StringBuilder();

    Runnable[] runnables = factory.apply (sb, "Ping\n", "Pong\n");

    List<Thread> threads = Arrays.stream(runnables)
      .map(Thread::new)
      .peek(Thread::start)
      .collect(Collectors.toList());

    threads.forEach(th -> {
      try {
        th.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });

    assertThat(sb.toString()).isEqualTo("Ping\nPong\nPing\nPong\nPing\nPong\n");
  }

}
