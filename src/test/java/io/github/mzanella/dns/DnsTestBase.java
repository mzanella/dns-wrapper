package io.github.mzanella.dns;

import io.github.mzanella.dns.simulator.MockDNSServer;
import java.io.IOException;
import java.net.UnknownHostException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

public class DnsTestBase {
  protected static MockDNSServer mockDNSServer;

//  @BeforeAll
  public static void setup() throws IOException {
    mockDNSServer = new MockDNSServer();
    mockDNSServer.start();
  }

  @AfterEach
  public void reset() {
    mockDNSServer.resetCount();
  }

  @AfterAll
  public static void cleanup() throws UnknownHostException, InterruptedException {
    mockDNSServer.close();
    mockDNSServer.join();
  }
}
