package io.github.mzanella.dns;

import io.github.mzanella.dns.simulator.MockDNSServer;
import java.io.IOException;
import java.net.UnknownHostException;
import org.junit.jupiter.api.AfterEach;

public class DnsTestBase {
  protected MockDNSServer mockDNSServer;

//  @BeforeAll
  public void setup() throws IOException {
    mockDNSServer = new MockDNSServer();
    mockDNSServer.start();
  }

  @AfterEach
  public void cleanup() throws UnknownHostException, InterruptedException {
    mockDNSServer.close();
  }
}
